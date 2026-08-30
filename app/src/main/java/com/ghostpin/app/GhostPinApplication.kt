package com.ghostpin.app

import android.app.Application
import android.preference.PreferenceManager
import com.ghostpin.app.data.local.AppDatabase
import com.ghostpin.app.data.repository.JourneyRepositoryImpl
import com.ghostpin.app.data.repository.LocationRepositoryImpl
import com.ghostpin.app.data.repository.SettingsRepositoryImpl
import com.ghostpin.app.domain.repository.JourneyRepository
import com.ghostpin.app.domain.repository.LocationRepository
import com.ghostpin.app.domain.repository.SettingsRepository
import com.ghostpin.app.engine.MockLocationController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.osmdroid.config.Configuration

class GhostPinApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val locationRepository: LocationRepository by lazy {
        LocationRepositoryImpl(database.locationDao())
    }
    val journeyRepository: JourneyRepository by lazy {
        JourneyRepositoryImpl(database.journeyDao())
    }
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(this)
    }

    val mockLocationController: MockLocationController by lazy {
        MockLocationController(this, applicationScope)
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize osmdroid configuration for open-source native map rendering
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = packageName
    }
}
