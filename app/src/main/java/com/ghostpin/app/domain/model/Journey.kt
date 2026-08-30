package com.ghostpin.app.domain.model

data class Journey(
    val id: Long = 0,
    val name: String,
    val waypoints: List<JourneyWaypoint>,
    val travelSpeedKmh: Double = 40.0, // Default 40 km/h driving speed
    val loopRoute: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculates total great-circle distance of all legs in kilometers.
     */
    fun calculateTotalDistanceKm(): Double {
        if (waypoints.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until waypoints.size - 1) {
            total += calculateDistanceKm(
                waypoints[i].latitude, waypoints[i].longitude,
                waypoints[i + 1].latitude, waypoints[i + 1].longitude
            )
        }
        if (loopRoute && waypoints.size > 2) {
            total += calculateDistanceKm(
                waypoints.last().latitude, waypoints.last().longitude,
                waypoints.first().latitude, waypoints.first().longitude
            )
        }
        return total
    }

    /**
     * Calculates total estimated duration of the entire journey in minutes (stays + travel).
     */
    fun calculateTotalDurationMinutes(): Long {
        if (waypoints.isEmpty()) return 0L
        var totalMinutes = waypoints.sumOf { it.stayDurationMinutes }

        for (i in 0 until waypoints.size - 1) {
            val legMinutes = waypoints[i].travelTimeToNextMinutes ?: run {
                val distKm = calculateDistanceKm(
                    waypoints[i].latitude, waypoints[i].longitude,
                    waypoints[i + 1].latitude, waypoints[i + 1].longitude
                )
                val hours = if (travelSpeedKmh > 0) distKm / travelSpeedKmh else 0.0
                (hours * 60).toLong().coerceAtLeast(1L)
            }
            totalMinutes += legMinutes
        }

        if (loopRoute && waypoints.size > 2) {
            val returnMinutes = waypoints.last().travelTimeToNextMinutes ?: run {
                val distKm = calculateDistanceKm(
                    waypoints.last().latitude, waypoints.last().longitude,
                    waypoints.first().latitude, waypoints.first().longitude
                )
                val hours = if (travelSpeedKmh > 0) distKm / travelSpeedKmh else 0.0
                (hours * 60).toLong().coerceAtLeast(1L)
            }
            totalMinutes += returnMinutes
        }

        return totalMinutes
    }

    companion object {
        fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371.0 // Radius of earth in km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            return r * c
        }
    }
}
