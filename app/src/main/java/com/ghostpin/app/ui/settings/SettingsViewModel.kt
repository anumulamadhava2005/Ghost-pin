package com.ghostpin.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ghostpin.app.domain.repository.SettingsRepository
import com.ghostpin.app.engine.MockLocationController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val updateIntervalMs: Long = 1000L,
    val defaultDurationMinutes: Long? = null,
    val isMockAppConfigured: Boolean = true
)

class SettingsViewModel(
    private val mockLocationController: MockLocationController,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isMockConfigured = MutableStateFlow(true)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.updateIntervalMs,
        settingsRepository.defaultDurationMinutes,
        _isMockConfigured
    ) { interval, duration, configured ->
        SettingsUiState(
            updateIntervalMs = interval,
            defaultDurationMinutes = duration,
            isMockAppConfigured = configured
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    init {
        checkConfiguration()
    }

    fun checkConfiguration() {
        _isMockConfigured.value = mockLocationController.checkMockLocationPermission()
    }

    fun setUpdateInterval(intervalMs: Long) {
        viewModelScope.launch {
            settingsRepository.setUpdateIntervalMs(intervalMs)
        }
    }

    class Factory(
        private val mockLocationController: MockLocationController,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(mockLocationController, settingsRepository) as T
        }
    }
}
