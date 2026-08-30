package com.ghostpin.app.engine

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ghostpin.app.GhostPinApplication
import com.ghostpin.app.MainActivity
import com.ghostpin.app.R
import com.ghostpin.app.domain.model.Journey
import com.ghostpin.app.domain.model.JourneyWaypoint
import com.ghostpin.app.domain.model.MockLocation
import com.ghostpin.app.domain.model.MockState
import com.ghostpin.app.domain.model.SimulationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MockLocationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var tickerJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val CHANNEL_ID = "ghostpin_simulation_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_FIXED = "com.ghostpin.app.ACTION_START_FIXED"
        const val ACTION_START_JOURNEY = "com.ghostpin.app.ACTION_START_JOURNEY"
        const val ACTION_STOP = "com.ghostpin.app.ACTION_STOP"

        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
        const val EXTRA_UPDATE_INTERVAL_MS = "extra_update_interval_ms"

        const val EXTRA_JOURNEY_JSON = "extra_journey_json"

        fun startService(
            context: Context,
            location: MockLocation,
            durationMinutes: Long? = null,
            updateIntervalMs: Long = 1000L
        ) {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_START_FIXED
                putExtra(EXTRA_LATITUDE, location.latitude)
                putExtra(EXTRA_LONGITUDE, location.longitude)
                putExtra(EXTRA_NAME, location.name)
                putExtra(EXTRA_DURATION_MINUTES, durationMinutes ?: -1L)
                putExtra(EXTRA_UPDATE_INTERVAL_MS, updateIntervalMs)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun startJourneyService(
            context: Context,
            journey: Journey,
            updateIntervalMs: Long = 1000L
        ) {
            val journeyObj = JSONObject().apply {
                put("id", journey.id)
                put("name", journey.name)
                put("travelSpeedKmh", journey.travelSpeedKmh)
                put("loopRoute", journey.loopRoute)
                val wpArray = JSONArray()
                journey.waypoints.forEach { wp ->
                    val wpObj = JSONObject().apply {
                        put("id", wp.id)
                        put("name", wp.name)
                        put("latitude", wp.latitude)
                        put("longitude", wp.longitude)
                        put("stayDurationMinutes", wp.stayDurationMinutes)
                        put("travelTimeToNextMinutes", wp.travelTimeToNextMinutes)
                    }
                    wpArray.put(wpObj)
                }
                put("waypoints", wpArray)
            }

            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_START_JOURNEY
                putExtra(EXTRA_JOURNEY_JSON, journeyObj.toString())
                putExtra(EXTRA_UPDATE_INTERVAL_MS, updateIntervalMs)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MockLocationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        val app = application as GhostPinApplication
        serviceScope.launch {
            app.mockLocationController.mockState.collectLatest { state ->
                when (state) {
                    is MockState.Running -> {
                        startTicker(state)
                    }
                    is MockState.Disabled, is MockState.Error -> {
                        tickerJob?.cancel()
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                    MockState.Configuring -> {}
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as GhostPinApplication
        when (intent?.action) {
            ACTION_START_FIXED -> {
                val lat = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
                val lng = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
                val name = intent.getStringExtra(EXTRA_NAME) ?: ""
                val durExtra = intent.getLongExtra(EXTRA_DURATION_MINUTES, -1L)
                val durationMinutes = if (durExtra > 0) durExtra else null
                val updateIntervalMs = intent.getLongExtra(EXTRA_UPDATE_INTERVAL_MS, 1000L)

                val config = SimulationConfig.Fixed(
                    location = MockLocation(lat, lng, name),
                    durationMinutes = durationMinutes,
                    updateIntervalMs = updateIntervalMs
                )

                startForegroundNotification("Starting fixed location simulation...")
                val result = app.mockLocationController.start(config)
                if (result.isFailure) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }

            ACTION_START_JOURNEY -> {
                val journeyJson = intent.getStringExtra(EXTRA_JOURNEY_JSON)
                val updateIntervalMs = intent.getLongExtra(EXTRA_UPDATE_INTERVAL_MS, 1000L)

                if (journeyJson != null) {
                    val journey = deserializeJourney(journeyJson)
                    val config = SimulationConfig.RouteJourney(
                        journey = journey,
                        updateIntervalMs = updateIntervalMs
                    )

                    startForegroundNotification("Starting journey: ${journey.name}...")
                    val result = app.mockLocationController.start(config)
                    if (result.isFailure) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
            }

            ACTION_STOP -> {
                app.mockLocationController.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundNotification(initialText: String) {
        val initialNotification = buildNotification(
            title = "GhostPin Active",
            locationText = initialText,
            subText = "Zero tracking • On-device simulation"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }
    }

    private fun startTicker(state: MockState.Running) {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val jState = state.journeyState

                val (title, locText, subText) = if (jState != null) {
                    val title = "Journey: ${jState.journeyName} (${jState.currentStopIndex}/${jState.totalStops})"
                    val locText = if (jState.isStaying) {
                        val m = jState.phaseRemainingSeconds / 60
                        val s = jState.phaseRemainingSeconds % 60
                        "At ${jState.currentStopName} • Staying for %02d:%02d".format(m, s)
                    } else {
                        val m = jState.phaseRemainingSeconds / 60
                        val s = jState.phaseRemainingSeconds % 60
                        "Heading to ${jState.nextStopName} (%.0f km/h) • %02d:%02d left".format(jState.speedKmh, m, s)
                    }
                    val subText = "Current coords: %.4f, %.4f".format(state.location.latitude, state.location.longitude)
                    Triple(title, locText, subText)
                } else {
                    val title = "GhostPin • Simulated Location Active"
                    val locText = if (state.location.name.isNotBlank()) {
                        "${state.location.name} (%.4f, %.4f)".format(state.location.latitude, state.location.longitude)
                    } else {
                        "%.4f, %.4f".format(state.location.latitude, state.location.longitude)
                    }
                    val subText = if (state.expiresAt != null) {
                        val remainingMs = (state.expiresAt - now).coerceAtLeast(0)
                        val m = remainingMs / 1000 / 60
                        val s = (remainingMs / 1000) % 60
                        "Auto-stops in %02d:%02d".format(m, s)
                    } else {
                        val elapsedMs = now - state.startedAt
                        val m = elapsedMs / 1000 / 60
                        val s = (elapsedMs / 1000) % 60
                        "Running for %02d:%02d".format(m, s)
                    }
                    Triple(title, locText, subText)
                }

                val notification = buildNotification(title, locText, subText)
                notificationManager.notify(NOTIFICATION_ID, notification)

                delay(1000L)
            }
        }
    }

    private fun buildNotification(title: String, locationText: String, subText: String): Notification {
        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MockLocationService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(title)
            .setContentText("$locationText • $subText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$locationText\n$subText\nZero tracking • On-device simulation")
            )
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "STOP FAKE LOCATION",
                stopPendingIntent
            )
            .build()
    }

    private fun deserializeJourney(json: String): Journey {
        val obj = JSONObject(json)
        val id = obj.optLong("id", 0L)
        val name = obj.getString("name")
        val speed = obj.optDouble("travelSpeedKmh", 40.0)
        val loop = obj.optBoolean("loopRoute", false)
        val wpArray = obj.getJSONArray("waypoints")
        val waypoints = mutableListOf<JourneyWaypoint>()

        for (i in 0 until wpArray.length()) {
            val wpObj = wpArray.getJSONObject(i)
            waypoints.add(
                JourneyWaypoint(
                    id = wpObj.optString("id"),
                    name = wpObj.getString("name"),
                    latitude = wpObj.getDouble("latitude"),
                    longitude = wpObj.getDouble("longitude"),
                    stayDurationMinutes = wpObj.optLong("stayDurationMinutes", 10L),
                    travelTimeToNextMinutes = if (wpObj.has("travelTimeToNextMinutes") && !wpObj.isNull("travelTimeToNextMinutes")) {
                        wpObj.getLong("travelTimeToNextMinutes")
                    } else null
                )
            )
        }

        return Journey(
            id = id,
            name = name,
            waypoints = waypoints,
            travelSpeedKmh = speed,
            loopRoute = loop
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tickerJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
