package com.ghostpin.app.ui.journey

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ghostpin.app.domain.model.Journey
import com.ghostpin.app.domain.model.JourneyWaypoint
import com.ghostpin.app.domain.model.MockLocation
import com.ghostpin.app.domain.repository.JourneyRepository
import com.ghostpin.app.domain.repository.LocationRepository
import com.ghostpin.app.engine.MockLocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class JourneyUiState(
    val builderName: String = "Custom Journey",
    val builderWaypoints: List<JourneyWaypoint> = emptyList(),
    val travelSpeedKmh: Double = 40.0,
    val loopRoute: Boolean = false,
    val isAddStopDialogVisible: Boolean = false,
    val errorMessage: String? = null
)

class JourneyViewModel(
    private val journeyRepository: JourneyRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    val savedJourneys: StateFlow<List<Journey>> = journeyRepository.getAllJourneys()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(
        JourneyUiState(
            builderWaypoints = listOf(
                JourneyWaypoint(name = "Home", latitude = 13.0850, longitude = 80.2100, stayDurationMinutes = 10L),
                JourneyWaypoint(name = "Coffee Shop", latitude = 13.0600, longitude = 80.2400, stayDurationMinutes = 20L),
                JourneyWaypoint(name = "Office", latitude = 12.9890, longitude = 80.2470, stayDurationMinutes = 120L)
            )
        )
    )
    val uiState: StateFlow<JourneyUiState> = _uiState.asStateFlow()

    fun updateJourneyName(name: String) {
        _uiState.value = _uiState.value.copy(builderName = name)
    }

    fun setSpeed(speedKmh: Double) {
        _uiState.value = _uiState.value.copy(travelSpeedKmh = speedKmh)
    }

    fun setLoop(loop: Boolean) {
        _uiState.value = _uiState.value.copy(loopRoute = loop)
    }

    fun addWaypoint(name: String, lat: Double, lng: Double, stayMinutes: Long = 10L, travelMinutes: Long? = null) {
        val currentList = _uiState.value.builderWaypoints.toMutableList()
        currentList.add(
            JourneyWaypoint(
                name = name,
                latitude = lat,
                longitude = lng,
                stayDurationMinutes = stayMinutes,
                travelTimeToNextMinutes = travelMinutes
            )
        )
        _uiState.value = _uiState.value.copy(
            builderWaypoints = currentList,
            isAddStopDialogVisible = false
        )
    }

    fun removeWaypoint(index: Int) {
        val currentList = _uiState.value.builderWaypoints.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _uiState.value = _uiState.value.copy(builderWaypoints = currentList)
        }
    }

    fun updateWaypointStay(index: Int, stayMinutes: Long) {
        val currentList = _uiState.value.builderWaypoints.toMutableList()
        if (index in currentList.indices) {
            val wp = currentList[index]
            currentList[index] = wp.copy(stayDurationMinutes = stayMinutes)
            _uiState.value = _uiState.value.copy(builderWaypoints = currentList)
        }
    }

    fun updateWaypointTravelTime(index: Int, travelMinutes: Long?) {
        val currentList = _uiState.value.builderWaypoints.toMutableList()
        if (index in currentList.indices) {
            val wp = currentList[index]
            currentList[index] = wp.copy(travelTimeToNextMinutes = travelMinutes)
            _uiState.value = _uiState.value.copy(builderWaypoints = currentList)
        }
    }

    fun loadJourneyIntoBuilder(journey: Journey) {
        _uiState.value = _uiState.value.copy(
            builderName = journey.name,
            builderWaypoints = journey.waypoints,
            travelSpeedKmh = journey.travelSpeedKmh,
            loopRoute = journey.loopRoute
        )
    }

    fun clearBuilder() {
        _uiState.value = _uiState.value.copy(
            builderName = "New Journey",
            builderWaypoints = emptyList()
        )
    }

    fun setAddStopDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isAddStopDialogVisible = visible)
    }

    fun saveCurrentJourney(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.builderWaypoints.isEmpty()) return

        val journey = Journey(
            name = state.builderName.ifBlank { "Custom Journey" },
            waypoints = state.builderWaypoints,
            travelSpeedKmh = state.travelSpeedKmh,
            loopRoute = state.loopRoute
        )

        viewModelScope.launch {
            journeyRepository.insertJourney(journey)
            onSuccess()
        }
    }

    fun startJourneySimulation(context: Context, journey: Journey? = null) {
        val targetJourney = journey ?: run {
            val state = _uiState.value
            Journey(
                name = state.builderName.ifBlank { "Custom Journey" },
                waypoints = state.builderWaypoints,
                travelSpeedKmh = state.travelSpeedKmh,
                loopRoute = state.loopRoute
            )
        }

        if (targetJourney.waypoints.isNotEmpty()) {
            MockLocationService.startJourneyService(
                context = context,
                journey = targetJourney
            )
        }
    }

    fun deleteJourney(journey: Journey) {
        viewModelScope.launch {
            journeyRepository.deleteJourney(journey)
        }
    }

    fun getCurrentBuilderJourney(): Journey {
        val state = _uiState.value
        return Journey(
            name = state.builderName,
            waypoints = state.builderWaypoints,
            travelSpeedKmh = state.travelSpeedKmh,
            loopRoute = state.loopRoute
        )
    }

    class Factory(
        private val journeyRepository: JourneyRepository,
        private val locationRepository: LocationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return JourneyViewModel(journeyRepository, locationRepository) as T
        }
    }
}
