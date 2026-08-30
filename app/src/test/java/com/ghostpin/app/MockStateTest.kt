package com.ghostpin.app

import com.ghostpin.app.domain.model.MockLocation
import com.ghostpin.app.domain.model.MockState
import com.ghostpin.app.domain.model.SavedLocation
import com.ghostpin.app.domain.model.SimulationConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockStateTest {

    @Test
    fun testMockStateTransitions() {
        val disabled: MockState = MockState.Disabled
        assertTrue(disabled is MockState.Disabled)

        val configuring: MockState = MockState.Configuring
        assertTrue(configuring is MockState.Configuring)

        val location = MockLocation(13.0827, 80.2707, "Chennai")
        val running: MockState = MockState.Running(
            location = location,
            startedAt = 1000L,
            expiresAt = 61000L
        )

        assertTrue(running is MockState.Running)
        val runningState = running as MockState.Running
        assertEquals("Chennai", runningState.location.name)
        assertEquals(13.0827, runningState.location.latitude, 0.0001)
        assertEquals(80.2707, runningState.location.longitude, 0.0001)
        assertEquals(61000L, runningState.expiresAt)

        val error: MockState = MockState.Error("SecurityException: Mock app not set")
        assertTrue(error is MockState.Error)
        assertEquals("SecurityException: Mock app not set", (error as MockState.Error).message)
    }

    @Test
    fun testSimulationConfig() {
        val config = SimulationConfig.Fixed(
            location = MockLocation(12.9716, 77.5946, "Bengaluru"),
            durationMinutes = 30L,
            updateIntervalMs = 2000L
        )
        assertEquals(30L, config.durationMinutes)
        assertEquals(2000L, config.updateIntervalMs)
        assertEquals("Bengaluru", config.location.name)
    }

    @Test
    fun testSavedLocationModel() {
        val saved = SavedLocation(
            id = 1L,
            name = "Home",
            latitude = 13.0827,
            longitude = 80.2707
        )
        assertEquals(1L, saved.id)
        assertEquals("Home", saved.name)
        assertEquals(13.0827, saved.latitude, 0.0001)
        assertEquals(80.2707, saved.longitude, 0.0001)
    }
}
