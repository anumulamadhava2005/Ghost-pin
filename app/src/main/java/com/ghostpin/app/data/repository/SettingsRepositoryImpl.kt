package com.ghostpin.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ghostpin.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ghostpin_settings")

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val DEFAULT_DURATION_MINUTES = longPreferencesKey("default_duration_minutes")
        val UPDATE_INTERVAL_MS = longPreferencesKey("update_interval_ms")
        val LAST_SELECTED_LATITUDE = doublePreferencesKey("last_selected_latitude")
        val LAST_SELECTED_LONGITUDE = doublePreferencesKey("last_selected_longitude")
        val LAST_SELECTED_NAME = stringPreferencesKey("last_selected_name")
    }

    override val defaultDurationMinutes: Flow<Long?> = context.dataStore.data.map { preferences ->
        val duration = preferences[PreferencesKeys.DEFAULT_DURATION_MINUTES] ?: -1L
        if (duration > 0) duration else null
    }

    override val updateIntervalMs: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.UPDATE_INTERVAL_MS] ?: 1000L
    }

    override val lastSelectedLatitude: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_SELECTED_LATITUDE] ?: 13.0827 // Default Chennai
    }

    override val lastSelectedLongitude: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_SELECTED_LONGITUDE] ?: 80.2707 // Default Chennai
    }

    override val lastSelectedName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_SELECTED_NAME] ?: "Chennai"
    }

    override suspend fun setDefaultDurationMinutes(minutes: Long?) {
        context.dataStore.edit { preferences ->
            if (minutes != null && minutes > 0) {
                preferences[PreferencesKeys.DEFAULT_DURATION_MINUTES] = minutes
            } else {
                preferences.remove(PreferencesKeys.DEFAULT_DURATION_MINUTES)
            }
        }
    }

    override suspend fun setUpdateIntervalMs(intervalMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UPDATE_INTERVAL_MS] = intervalMs
        }
    }

    override suspend fun saveLastSelectedLocation(latitude: Double, longitude: Double, name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SELECTED_LATITUDE] = latitude
            preferences[PreferencesKeys.LAST_SELECTED_LONGITUDE] = longitude
            preferences[PreferencesKeys.LAST_SELECTED_NAME] = name
        }
    }
}
