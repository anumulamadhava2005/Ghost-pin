package com.ghostpin.app

import com.ghostpin.app.domain.model.Journey
import com.ghostpin.app.domain.model.JourneyRunState
import com.ghostpin.app.domain.model.JourneyWaypoint
import com.ghostpin.app.domain.model.MockLocation
import com.ghostpin.app.domain.model.MockState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneyTest {

    @Test
    fun testJourneyDistanceCalculation() {
        val wp1 = JourneyWaypoint(name = "Chennai", latitude = 13.0827, longitude = 80.2707, stayDurationMinutes = 15L)
        val wp2 = JourneyWaypoint(name = "Bengaluru", latitude = 12.9716, longitude = 77.5946, stayDurationMinutes = 30L)

        val journey = Journey(
            name = "Chennai to Bengaluru",
            waypoints = listOf(wp1, wp2),
            travelSpeedKmh = 60.0
        )

        val distanceKm = journey.calculateTotalDistanceKm()
        // Approximate distance between Chennai and Bengaluru is ~290 km
        assertTrue("Distance should be around 290km, was $distanceKm", distanceKm in 280.0..310.0)

        val durationMinutes = journey.calculateTotalDurationMinutes()
        // Stays: 15 + 30 = 45m. Travel: ~290km at 60km/h = ~290min. Total ~335min.
        assertTrue("Duration should be around 335min, was $durationMinutes", durationMinutes in 300..380)
    }

    @Test
    fun testJourneyRunStateTransitions() {
        val runState = JourneyRunState(
            journeyName = "Office Route",
            currentStopIndex = 1,
            totalStops = 3,
            currentStopName = "Home",
            nextStopName = "Coffee",
            isStaying = true,
            legProgressFraction = 0.0f,
            phaseRemainingSeconds = 600L,
            speedKmh = 0.0
        )

        val runningMockState = MockState.Running(
            location = MockLocation(13.0827, 80.2707, "Home"),
            startedAt = 1000L,
            expiresAt = null,
            journeyState = runState
        )

        assertTrue(runningMockState.journeyState != null)
        assertEquals("Office Route", runningMockState.journeyState?.journeyName)
        assertTrue(runningMockState.journeyState?.isStaying == true)
        assertEquals(1, runningMockState.journeyState?.currentStopIndex)
        assertEquals(3, runningMockState.journeyState?.totalStops)
    }

    @Test
    fun testCustomTravelTimeOverride() {
        val wp1 = JourneyWaypoint(
            name = "Point A",
            latitude = 13.0827,
            longitude = 80.2707,
            stayDurationMinutes = 5L,
            travelTimeToNextMinutes = 20L
        )
        val wp2 = JourneyWaypoint(
            name = "Point B",
            latitude = 13.0850,
            longitude = 80.2750,
            stayDurationMinutes = 10L
        )

        val journey = Journey(
            name = "Quick Trip",
            waypoints = listOf(wp1, wp2),
            travelSpeedKmh = 40.0
        )

        val totalMinutes = journey.calculateTotalDurationMinutes()
        // Stay 5 + Travel override 20 + Stay 10 = 35 minutes
        assertEquals(35L, totalMinutes)
    }
}
