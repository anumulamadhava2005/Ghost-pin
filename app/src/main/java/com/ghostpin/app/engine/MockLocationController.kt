package com.ghostpin.app.engine

import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.SystemClock
import com.ghostpin.app.domain.model.Journey
import com.ghostpin.app.domain.model.JourneyRunState
import com.ghostpin.app.domain.model.JourneyWaypoint
import com.ghostpin.app.domain.model.MockLocation
import com.ghostpin.app.domain.model.MockState
import com.ghostpin.app.domain.model.SimulationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MockLocationController(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _mockState = MutableStateFlow<MockState>(MockState.Disabled)
    val mockState: StateFlow<MockState> = _mockState.asStateFlow()

    private var mockJob: Job? = null
    private var isProviderAdded = false

    companion object {
        const val PROVIDER_NAME = LocationManager.GPS_PROVIDER
    }

    /**
     * Checks if the app is selected as the mock location app in Developer Options.
     */
    fun checkMockLocationPermission(): Boolean {
        return try {
            initTestProviderInternal()
            cleanupTestProviderInternal()
            true
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            true
        }
    }

    @Synchronized
    fun start(config: SimulationConfig): Result<Unit> {
        _mockState.value = MockState.Configuring

        return try {
            initTestProviderInternal()
            mockJob?.cancel()

            when (config) {
                is SimulationConfig.Fixed -> startFixedSimulation(config)
                is SimulationConfig.RouteJourney -> startJourneySimulation(config)
            }
            Result.success(Unit)
        } catch (e: SecurityException) {
            val errorMsg = "GhostPin is not selected as the Mock Location app. Enable Developer Options and select GhostPin."
            _mockState.value = MockState.Error(errorMsg)
            Result.failure(e)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Failed to start mock location engine"
            _mockState.value = MockState.Error(errorMsg)
            Result.failure(e)
        }
    }

    private fun startFixedSimulation(config: SimulationConfig.Fixed) {
        val startedAt = System.currentTimeMillis()
        val expiresAt = config.durationMinutes?.let { startedAt + (it * 60 * 1000) }

        _mockState.value = MockState.Running(
            location = config.location,
            startedAt = startedAt,
            expiresAt = expiresAt,
            journeyState = null
        )

        mockJob = scope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                if (expiresAt != null && now >= expiresAt) {
                    stop()
                    break
                }

                try {
                    emitLocation(config.location.latitude, config.location.longitude, 0f, 0f)
                } catch (e: Exception) {
                    _mockState.value = MockState.Error(e.message ?: "Failed to inject mock location")
                    break
                }

                delay(config.updateIntervalMs)
            }
        }
    }

    private fun startJourneySimulation(config: SimulationConfig.RouteJourney) {
        val journey = config.journey
        if (journey.waypoints.isEmpty()) {
            _mockState.value = MockState.Error("Journey contains no waypoints")
            return
        }

        val startedAt = System.currentTimeMillis()
        val totalDurationMs = journey.calculateTotalDurationMinutes() * 60 * 1000L
        val expiresAt = if (journey.loopRoute) null else startedAt + totalDurationMs

        mockJob = scope.launch {
            val waypoints = journey.waypoints
            var currentBearing = 0.0f

            do {
                for (i in waypoints.indices) {
                    if (!isActive) break

                    val currentWp = waypoints[i]
                    val nextWp = if (i < waypoints.size - 1) {
                        waypoints[i + 1]
                    } else if (journey.loopRoute && waypoints.size > 1) {
                        waypoints.first()
                    } else null

                    // 1. STAY PHASE AT WAYPOINT
                    val staySeconds = currentWp.stayDurationMinutes * 60L
                    var stayRemaining = staySeconds

                    while (stayRemaining > 0 && isActive) {
                        val currentLoc = MockLocation(
                            latitude = currentWp.latitude,
                            longitude = currentWp.longitude,
                            name = currentWp.name
                        )

                        _mockState.value = MockState.Running(
                            location = currentLoc,
                            startedAt = startedAt,
                            expiresAt = expiresAt,
                            journeyState = JourneyRunState(
                                journeyName = journey.name,
                                currentStopIndex = i + 1,
                                totalStops = waypoints.size,
                                currentStopName = currentWp.name,
                                nextStopName = nextWp?.name,
                                isStaying = true,
                                legProgressFraction = 0.0f,
                                phaseRemainingSeconds = stayRemaining,
                                speedKmh = 0.0
                            )
                        )

                        emitLocation(currentWp.latitude, currentWp.longitude, 0f, currentBearing)

                        val stepSec = (config.updateIntervalMs / 1000.0).coerceAtLeast(1.0).toLong()
                        delay(config.updateIntervalMs)
                        stayRemaining = (stayRemaining - stepSec).coerceAtLeast(0)
                    }

                    if (!isActive || nextWp == null) break

                    // 2. TRAVEL PHASE TO NEXT WAYPOINT
                    val distKm = Journey.calculateDistanceKm(
                        currentWp.latitude, currentWp.longitude,
                        nextWp.latitude, nextWp.longitude
                    )
                    currentBearing = calculateBearing(
                        currentWp.latitude, currentWp.longitude,
                        nextWp.latitude, nextWp.longitude
                    )

                    val travelSeconds = currentWp.travelTimeToNextMinutes?.let { it * 60L } ?: run {
                        val hours = if (journey.travelSpeedKmh > 0) distKm / journey.travelSpeedKmh else 0.0
                        (hours * 3600).toLong().coerceAtLeast(5L)
                    }

                    val speedMps = if (travelSeconds > 0) ((distKm * 1000) / travelSeconds).toFloat() else 0f
                    val actualSpeedKmh = speedMps * 3.6

                    val travelStartMillis = System.currentTimeMillis()
                    val travelEndMillis = travelStartMillis + (travelSeconds * 1000L)

                    while (System.currentTimeMillis() < travelEndMillis && isActive) {
                        val now = System.currentTimeMillis()
                        val fraction = ((now - travelStartMillis).toDouble() / (travelSeconds * 1000.0)).coerceIn(0.0, 1.0).toFloat()
                        val remainingSec = ((travelEndMillis - now) / 1000L).coerceAtLeast(0L)

                        // Interpolate coordinates
                        val curLat = currentWp.latitude + fraction * (nextWp.latitude - currentWp.latitude)
                        val curLng = currentWp.longitude + fraction * (nextWp.longitude - currentWp.longitude)

                        val currentLoc = MockLocation(
                            latitude = curLat,
                            longitude = curLng,
                            name = "En route to ${nextWp.name}"
                        )

                        _mockState.value = MockState.Running(
                            location = currentLoc,
                            startedAt = startedAt,
                            expiresAt = expiresAt,
                            journeyState = JourneyRunState(
                                journeyName = journey.name,
                                currentStopIndex = i + 1,
                                totalStops = waypoints.size,
                                currentStopName = currentWp.name,
                                nextStopName = nextWp.name,
                                isStaying = false,
                                legProgressFraction = fraction,
                                phaseRemainingSeconds = remainingSec,
                                speedKmh = actualSpeedKmh
                            )
                        )

                        emitLocation(curLat, curLng, speedMps, currentBearing)
                        delay(config.updateIntervalMs)
                    }

                    // Snap to exact next waypoint
                    emitLocation(nextWp.latitude, nextWp.longitude, 0f, currentBearing)
                }
            } while (journey.loopRoute && isActive)

            stop()
        }
    }

    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = atan2(y, x)
        return ((Math.toDegrees(theta) + 360.0) % 360.0).toFloat()
    }

    @Synchronized
    fun stop() {
        mockJob?.cancel()
        mockJob = null
        cleanupTestProviderInternal()
        _mockState.value = MockState.Disabled
    }

    private fun initTestProviderInternal() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationManager.addTestProvider(
                    PROVIDER_NAME,
                    false, // requiresNetwork
                    false, // requiresSatellite
                    false, // requiresCell
                    false, // hasMonetaryCost
                    true,  // supportsAltitude
                    true,  // supportsSpeed
                    true,  // supportsBearing
                    ProviderProperties.POWER_USAGE_LOW,
                    ProviderProperties.ACCURACY_FINE
                )
            } else {
                @Suppress("DEPRECATION")
                locationManager.addTestProvider(
                    PROVIDER_NAME,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    true,
                    Criteria.POWER_LOW,
                    Criteria.ACCURACY_FINE
                )
            }
            locationManager.setTestProviderEnabled(PROVIDER_NAME, true)
            isProviderAdded = true
        } catch (e: IllegalArgumentException) {
            try {
                locationManager.setTestProviderEnabled(PROVIDER_NAME, true)
                isProviderAdded = true
            } catch (ignored: Exception) {}
        }
    }

    private fun emitLocation(lat: Double, lng: Double, speedMps: Float, bearingDegrees: Float) {
        val location = Location(PROVIDER_NAME).apply {
            latitude = lat
            longitude = lng
            altitude = 0.0
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            accuracy = 5.0f
            bearing = bearingDegrees
            speed = speedMps

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bearingAccuracyDegrees = 0.1f
                verticalAccuracyMeters = 1.0f
                speedAccuracyMetersPerSecond = 0.1f
            }
        }
        locationManager.setTestProviderLocation(PROVIDER_NAME, location)
    }

    private fun cleanupTestProviderInternal() {
        try {
            if (isProviderAdded) {
                locationManager.setTestProviderEnabled(PROVIDER_NAME, false)
                locationManager.removeTestProvider(PROVIDER_NAME)
                isProviderAdded = false
            }
        } catch (ignored: Exception) {
            isProviderAdded = false
        }
    }
}
