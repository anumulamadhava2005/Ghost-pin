package com.ghostpin.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val defaultDurationMinutes: Flow<Long?>
    val updateIntervalMs: Flow<Long>
    val lastSelectedLatitude: Flow<Double>
    val lastSelectedLongitude: Flow<Double>
    val lastSelectedName: Flow<String>

    suspend fun setDefaultDurationMinutes(minutes: Long?)
    suspend fun setUpdateIntervalMs(intervalMs: Long)
    suspend fun saveLastSelectedLocation(latitude: Double, longitude: Double, name: String)
}
