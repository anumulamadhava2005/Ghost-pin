package com.ghostpin.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ghostpin.app.data.local.dao.JourneyDao
import com.ghostpin.app.data.local.dao.LocationDao
import com.ghostpin.app.data.local.entity.SavedJourneyEntity
import com.ghostpin.app.data.local.entity.SavedLocationEntity
import com.ghostpin.app.domain.model.Journey
import com.ghostpin.app.domain.model.JourneyWaypoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SavedLocationEntity::class, SavedJourneyEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao
    abstract fun journeyDao(): JourneyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ghostpin_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialPresets(database.locationDao(), database.journeyDao())
                    }
                }
            }

            suspend fun populateInitialPresets(locDao: LocationDao, journeyDao: JourneyDao) {
                // Populate initial starter locations (clean names without emojis)
                locDao.insertLocation(
                    SavedLocationEntity(
                        name = "Home (Chennai)",
                        latitude = 13.0827,
                        longitude = 80.2707
                    )
                )
                locDao.insertLocation(
                    SavedLocationEntity(
                        name = "College (Vellore)",
                        latitude = 12.9165,
                        longitude = 79.1325
                    )
                )
                locDao.insertLocation(
                    SavedLocationEntity(
                        name = "Office (Bengaluru)",
                        latitude = 12.9716,
                        longitude = 77.5946
                    )
                )
                locDao.insertLocation(
                    SavedLocationEntity(
                        name = "Cafe (Hyderabad)",
                        latitude = 17.3850,
                        longitude = 78.4867
                    )
                )

                // Populate initial starter journey
                val sampleJourney = Journey(
                    name = "City Commute (Chennai)",
                    waypoints = listOf(
                        JourneyWaypoint(
                            name = "Home (Anna Nagar)",
                            latitude = 13.0850,
                            longitude = 80.2100,
                            stayDurationMinutes = 5L
                        ),
                        JourneyWaypoint(
                            name = "Cafe (Nungambakkam)",
                            latitude = 13.0600,
                            longitude = 80.2400,
                            stayDurationMinutes = 15L
                        ),
                        JourneyWaypoint(
                            name = "Tech Park (Tidel)",
                            latitude = 12.9890,
                            longitude = 80.2470,
                            stayDurationMinutes = 60L
                        )
                    ),
                    travelSpeedKmh = 35.0,
                    loopRoute = false
                )
                journeyDao.insertJourney(SavedJourneyEntity.fromDomain(sampleJourney))
            }
        }
    }
}
