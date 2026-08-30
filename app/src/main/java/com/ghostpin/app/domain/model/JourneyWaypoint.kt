package com.ghostpin.app.domain.model

import java.util.UUID

data class JourneyWaypoint(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val stayDurationMinutes: Long = 10L,
    val travelTimeToNextMinutes: Long? = null // null means calculate dynamically from speed
)
