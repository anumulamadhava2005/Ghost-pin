package com.ghostpin.app.data.repository

import com.ghostpin.app.data.local.dao.LocationDao
import com.ghostpin.app.data.local.entity.SavedLocationEntity
import com.ghostpin.app.domain.model.SavedLocation
import com.ghostpin.app.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocationRepositoryImpl(
    private val locationDao: LocationDao
) : LocationRepository {

    override fun getAllSavedLocations(): Flow<List<SavedLocation>> {
        return locationDao.getAllLocations().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getSavedLocationById(id: Long): SavedLocation? {
        return locationDao.getLocationById(id)?.toDomain()
    }

    override suspend fun insertSavedLocation(location: SavedLocation): Long {
        return locationDao.insertLocation(SavedLocationEntity.fromDomain(location))
    }

    override suspend fun deleteSavedLocation(location: SavedLocation) {
        locationDao.deleteLocation(SavedLocationEntity.fromDomain(location))
    }

    override suspend fun deleteSavedLocationById(id: Long) {
        locationDao.deleteLocationById(id)
    }
}
