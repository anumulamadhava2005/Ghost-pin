package com.ghostpin.app.domain.model

sealed class SimulationConfig(open val updateIntervalMs: Long = 1000L) {
    data class Fixed(
        val location: MockLocation,
        val durationMinutes: Long? = null, // null = Until Stopped
        override val updateIntervalMs: Long = 1000L
    ) : SimulationConfig(updateIntervalMs)

    data class RouteJourney(
        val journey: Journey,
        override val updateIntervalMs: Long = 1000L
    ) : SimulationConfig(updateIntervalMs)
}
