package com.ghostpin.app.domain.repository

import com.ghostpin.app.domain.model.SavedLocation
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getAllSavedLocations(): Flow<List<SavedLocation>>
    suspend fun getSavedLocationById(id: Long): SavedLocation?
    suspend fun insertSavedLocation(location: SavedLocation): Long
    suspend fun deleteSavedLocation(location: SavedLocation)
    suspend fun deleteSavedLocationById(id: Long)
}
