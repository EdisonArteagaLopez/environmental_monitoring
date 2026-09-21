package com.example.environmental_monitoring.di

import android.content.Context
import com.example.environmental_monitoring.data.local.AppDatabase
import com.example.environmental_monitoring.data.repository.SensorRepository
import com.example.environmental_monitoring.data.settings.SettingsRepository

/**
 * Contenedor manual de dependencias (sin Hilt) para mantener el proyecto simple.
 * Se accede vía [AppContainer.get].
 */
class AppContainer private constructor(context: Context) {

    private val database = AppDatabase.getInstance(context)

    val settingsRepository = SettingsRepository(context)

    val sensorRepository = SensorRepository(
        readingDao = database.sensorReadingDao(),
        thresholdDao = database.deviceThresholdDao(),
        alarmDao = database.alarmEventDao()
    )

    companion object {
        @Volatile
        private var INSTANCE: AppContainer? = null

        fun get(context: Context): AppContainer =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppContainer(context.applicationContext).also { INSTANCE = it }
            }
    }
}
