package com.ghostpin.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ghostpin.app.data.local.entity.SavedJourneyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyDao {

    @Query("SELECT * FROM saved_journeys ORDER BY createdAt DESC")
    fun getAllJourneys(): Flow<List<SavedJourneyEntity>>

    @Query("SELECT * FROM saved_journeys WHERE id = :id LIMIT 1")
    suspend fun getJourneyById(id: Long): SavedJourneyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: SavedJourneyEntity): Long

    @Update
    suspend fun updateJourney(journey: SavedJourneyEntity)

    @Delete
    suspend fun deleteJourney(journey: SavedJourneyEntity)

    @Query("DELETE FROM saved_journeys WHERE id = :id")
    suspend fun deleteJourneyById(id: Long)
}
