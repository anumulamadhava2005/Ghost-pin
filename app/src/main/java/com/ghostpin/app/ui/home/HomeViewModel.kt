package com.ghostpin.app.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ghostpin.app.domain.model.MockLocation
import com.ghostpin.app.domain.model.MockState
import com.ghostpin.app.domain.model.SavedLocation
import com.ghostpin.app.domain.repository.LocationRepository
import com.ghostpin.app.domain.repository.SettingsRepository
import com.ghostpin.app.engine.MockLocationController
import com.ghostpin.app.engine.MockLocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val mockState: MockState = MockState.Disabled,
    val selectedLocation: MockLocation = MockLocation(13.0827, 80.2707, "Chennai"),
    val selectedDurationMinutes: Long? = null,
    val isMockAppConfigured: Boolean = true,
    val quickPresets: List<SavedLocation> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val mockLocationController: MockLocationController,
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedLocation = MutableStateFlow(MockLocation(13.0827, 80.2707, "Chennai"))
    private val _selectedDuration = MutableStateFlow<Long?>(null)
    private val _isMockAppConfigured = MutableStateFlow(true)

    val uiState: StateFlow<HomeUiState> = combine(
        mockLocationController.mockState,
        _selectedLocation,
        _selectedDuration,
        _isMockAppConfigured,
        locationRepository.getAllSavedLocations()
    ) { mockState, location, duration, isConfigured, presets ->
        HomeUiState(
            mockState = mockState,
            selectedLocation = location,
            selectedDurationMinutes = duration,
            isMockAppConfigured = isConfigured,
            quickPresets = presets.take(4),
            errorMessage = (mockState as? MockState.Error)?.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        // Load initial persisted location preferences
        viewModelScope.launch {
            combine(
                settingsRepository.lastSelectedLatitude,
                settingsRepository.lastSelectedLongitude,
                settingsRepository.lastSelectedName
            ) { lat, lng, name ->
                MockLocation(lat, lng, name)
            }.collect { loc ->
                _selectedLocation.value = loc
            }
        }

        viewModelScope.launch {
            settingsRepository.defaultDurationMinutes.collect { duration ->
                _selectedDuration.value = duration
            }
        }

        checkMockAppConfiguration()
    }

    fun checkMockAppConfiguration() {
        _isMockAppConfigured.value = mockLocationController.checkMockLocationPermission()
    }

    fun setLocation(latitude: Double, longitude: Double, name: String) {
        _selectedLocation.value = MockLocation(latitude, longitude, name)
        viewModelScope.launch {
            settingsRepository.saveLastSelectedLocation(latitude, longitude, name)
        }
    }

    fun setDuration(minutes: Long?) {
        _selectedDuration.value = minutes
        viewModelScope.launch {
            settingsRepository.setDefaultDurationMinutes(minutes)
        }
    }

    fun startSimulation(context: Context) {
        val currentLoc = _selectedLocation.value
        val duration = _selectedDuration.value
        MockLocationService.startService(
            context = context,
            location = currentLoc,
            durationMinutes = duration
        )
    }

    fun stopSimulation(context: Context) {
        MockLocationService.stopService(context)
    }

    fun quickActivatePreset(context: Context, preset: SavedLocation) {
        setLocation(preset.latitude, preset.longitude, preset.name)
        MockLocationService.startService(
            context = context,
            location = MockLocation(preset.latitude, preset.longitude, preset.name),
            durationMinutes = _selectedDuration.value
        )
    }

    class Factory(
        private val mockLocationController: MockLocationController,
        private val locationRepository: LocationRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(mockLocationController, locationRepository, settingsRepository) as T
        }
    }
}
