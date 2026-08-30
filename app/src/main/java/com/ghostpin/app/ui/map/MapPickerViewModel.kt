package com.ghostpin.app.ui.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ghostpin.app.domain.model.SavedLocation
import com.ghostpin.app.domain.repository.LocationRepository
import com.ghostpin.app.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class PlaceSearchResult(
    val displayName: String,
    val latitude: Double,
    val longitude: Double
)

data class MapPickerUiState(
    val selectedLatitude: Double = 13.0827,
    val selectedLongitude: Double = 80.2707,
    val locationName: String = "Chennai, Tamil Nadu",
    val isMapDragging: Boolean = false,
    val isGeocoding: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<PlaceSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val isManualCoordDialogVisible: Boolean = false,
    val errorMessage: String? = null
)

class MapPickerViewModel(
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapPickerUiState())
    val uiState: StateFlow<MapPickerUiState> = _uiState.asStateFlow()

    private var geocodeJob: Job? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val lat = settingsRepository.lastSelectedLatitude.first()
            val lng = settingsRepository.lastSelectedLongitude.first()
            val name = settingsRepository.lastSelectedName.first()
            _uiState.value = _uiState.value.copy(
                selectedLatitude = lat,
                selectedLongitude = lng,
                locationName = name
            )
        }
    }

    fun onMapMoveStarted() {
        _uiState.value = _uiState.value.copy(
            isMapDragging = true
        )
    }

    fun onMapMoving(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            selectedLatitude = lat,
            selectedLongitude = lng
        )
    }

    fun onMapMoveEnded(context: Context, lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            selectedLatitude = lat,
            selectedLongitude = lng,
            isMapDragging = false
        )
        reverseGeocode(context, lat, lng)
    }

    fun updateCoordinatesManual(lat: Double, lng: Double, name: String? = null) {
        _uiState.value = _uiState.value.copy(
            selectedLatitude = lat,
            selectedLongitude = lng,
            locationName = name ?: _uiState.value.locationName
        )
    }

    fun setSearchQuery(context: Context, query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()

        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // Debounce search
            _uiState.value = _uiState.value.copy(isSearching = true)
            val results = searchPlaces(context, query)
            _uiState.value = _uiState.value.copy(
                searchResults = results,
                isSearching = false
            )
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList(),
            isSearching = false
        )
    }

    fun selectSearchResult(result: PlaceSearchResult) {
        _uiState.value = _uiState.value.copy(
            selectedLatitude = result.latitude,
            selectedLongitude = result.longitude,
            locationName = result.displayName.substringBefore(",").trim().ifBlank { result.displayName },
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    fun setManualDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isManualCoordDialogVisible = visible)
    }

    fun applyLocation(): Boolean {
        val lat = _uiState.value.selectedLatitude
        val lng = _uiState.value.selectedLongitude
        val name = _uiState.value.locationName.ifBlank { "Custom Pin (%.4f, %.4f)".format(lat, lng) }

        viewModelScope.launch {
            settingsRepository.saveLastSelectedLocation(lat, lng, name)
        }
        return true
    }

    fun saveToPresets(name: String? = null, onSuccess: () -> Unit) {
        val lat = _uiState.value.selectedLatitude
        val lng = _uiState.value.selectedLongitude
        val presetName = name?.ifBlank { null } ?: _uiState.value.locationName.ifBlank {
            "Pin (%.4f, %.4f)".format(lat, lng)
        }

        viewModelScope.launch {
            locationRepository.insertSavedLocation(
                SavedLocation(
                    name = presetName,
                    latitude = lat,
                    longitude = lng
                )
            )
            settingsRepository.saveLastSelectedLocation(lat, lng, presetName)
            onSuccess()
        }
    }

    fun getCurrentDeviceLocation(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: SecurityException) {
            null
        }
    }

    private fun reverseGeocode(context: Context, lat: Double, lng: Double) {
        geocodeJob?.cancel()
        geocodeJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeocoding = true)
            val addressName = withContext(Dispatchers.IO) {
                try {
                    if (Geocoder.isPresent()) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            var resolved: String? = null
                            geocoder.getFromLocation(lat, lng, 1) { addresses ->
                                addresses.firstOrNull()?.let {
                                    resolved = formatAddress(it)
                                }
                            }
                            delay(300)
                            if (resolved != null) return@withContext resolved
                        } else {
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(lat, lng, 1)
                            addresses?.firstOrNull()?.let {
                                return@withContext formatAddress(it)
                            }
                        }
                    }
                } catch (ignored: Exception) {}

                // Fallback to OSM Nominatim API
                fetchNominatimReverse(lat, lng)
            }

            val finalName = addressName ?: "Location (%.4f, %.4f)".format(lat, lng)
            _uiState.value = _uiState.value.copy(
                locationName = finalName,
                isGeocoding = false
            )
        }
    }

    private fun formatAddress(address: Address): String {
        val feature = address.featureName
        val thoroughfare = address.thoroughfare
        val subLocality = address.subLocality
        val locality = address.locality ?: address.adminArea

        val parts = listOfNotNull(feature ?: thoroughfare, subLocality, locality).distinct()
        return if (parts.isNotEmpty()) parts.joinToString(", ") else address.getAddressLine(0) ?: "Custom Location"
    }

    private suspend fun searchPlaces(context: Context, query: String): List<PlaceSearchResult> = withContext(Dispatchers.IO) {
        val list = mutableListOf<PlaceSearchResult>()
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 5)
                addresses?.forEach { addr ->
                    val name = addr.getAddressLine(0) ?: "${addr.locality ?: addr.adminArea ?: query}"
                    list.add(PlaceSearchResult(name, addr.latitude, addr.longitude))
                }
            }
        } catch (ignored: Exception) {}

        if (list.isEmpty()) {
            list.addAll(fetchNominatimSearch(query))
        }
        list
    }

    private fun fetchNominatimReverse(lat: Double, lng: Double): String? {
        return try {
            val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng&zoom=16")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "GhostPin-Android/1.0")
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().readText()
                val json = org.json.JSONObject(text)
                json.optString("display_name", null)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchNominatimSearch(query: String): List<PlaceSearchResult> {
        val list = mutableListOf<PlaceSearchResult>()
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://nominatim.openstreetmap.org/search?format=json&q=$encoded&limit=5")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "GhostPin-Android/1.0")
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().readText()
                val array = JSONArray(text)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val displayName = obj.getString("display_name")
                    val lat = obj.getDouble("lat")
                    val lon = obj.getDouble("lon")
                    list.add(PlaceSearchResult(displayName, lat, lon))
                }
            }
        } catch (e: Exception) {}
        return list
    }

    class Factory(
        private val locationRepository: LocationRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MapPickerViewModel(locationRepository, settingsRepository) as T
        }
    }
}
