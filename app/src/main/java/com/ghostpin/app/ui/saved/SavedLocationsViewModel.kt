package com.ghostpin.app.ui.saved

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ghostpin.app.domain.model.MockLocation
import com.ghostpin.app.domain.model.SavedLocation
import com.ghostpin.app.domain.repository.LocationRepository
import com.ghostpin.app.domain.repository.SettingsRepository
import com.ghostpin.app.engine.MockLocationService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedLocationsViewModel(
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val savedLocations: StateFlow<List<SavedLocation>> = locationRepository.getAllSavedLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun activateLocation(context: Context, location: SavedLocation) {
        viewModelScope.launch {
            settingsRepository.saveLastSelectedLocation(
                location.latitude,
                location.longitude,
                location.name
            )
            MockLocationService.startService(
                context = context,
                location = MockLocation(location.latitude, location.longitude, location.name)
            )
        }
    }

    fun deleteLocation(location: SavedLocation) {
        viewModelScope.launch {
            locationRepository.deleteSavedLocation(location)
        }
    }

    fun addLocation(name: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            locationRepository.insertSavedLocation(
                SavedLocation(
                    name = name,
                    latitude = lat,
                    longitude = lng
                )
            )
        }
    }

    class Factory(
        private val locationRepository: LocationRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SavedLocationsViewModel(locationRepository, settingsRepository) as T
        }
    }
}
