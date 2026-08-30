package com.ghostpin.app.domain.repository

import com.ghostpin.app.domain.model.Journey
import kotlinx.coroutines.flow.Flow

interface JourneyRepository {
    fun getAllJourneys(): Flow<List<Journey>>
    suspend fun getJourneyById(id: Long): Journey?
    suspend fun insertJourney(journey: Journey): Long
    suspend fun updateJourney(journey: Journey)
    suspend fun deleteJourney(journey: Journey)
    suspend fun deleteJourneyById(id: Long)
}
