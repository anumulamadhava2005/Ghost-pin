package com.ghostpin.app.domain.model

data class JourneyRunState(
    val journeyName: String,
    val currentStopIndex: Int,
    val totalStops: Int,
    val currentStopName: String,
    val nextStopName: String?,
    val isStaying: Boolean, // true = staying at current stop, false = moving to next stop
    val legProgressFraction: Float, // 0.0 to 1.0
    val phaseRemainingSeconds: Long,
    val speedKmh: Double
)

sealed interface MockState {
    data object Disabled : MockState
    data object Configuring : MockState
    data class Running(
        val location: MockLocation,
        val startedAt: Long = System.currentTimeMillis(),
        val expiresAt: Long? = null,
        val journeyState: JourneyRunState? = null
    ) : MockState
    data class Error(val message: String) : MockState
}
