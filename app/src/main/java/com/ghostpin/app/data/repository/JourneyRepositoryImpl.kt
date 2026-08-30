package com.ghostpin.app.data.repository

import com.ghostpin.app.data.local.dao.JourneyDao
import com.ghostpin.app.data.local.entity.SavedJourneyEntity
import com.ghostpin.app.domain.model.Journey
import com.ghostpin.app.domain.repository.JourneyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JourneyRepositoryImpl(
    private val journeyDao: JourneyDao
) : JourneyRepository {

    override fun getAllJourneys(): Flow<List<Journey>> {
        return journeyDao.getAllJourneys().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getJourneyById(id: Long): Journey? {
        return journeyDao.getJourneyById(id)?.toDomain()
    }

    override suspend fun insertJourney(journey: Journey): Long {
        return journeyDao.insertJourney(SavedJourneyEntity.fromDomain(journey))
    }

    override suspend fun updateJourney(journey: Journey) {
        journeyDao.updateJourney(SavedJourneyEntity.fromDomain(journey))
    }

    override suspend fun deleteJourney(journey: Journey) {
        journeyDao.deleteJourney(SavedJourneyEntity.fromDomain(journey))
    }

    override suspend fun deleteJourneyById(id: Long) {
        journeyDao.deleteJourneyById(id)
    }
}
