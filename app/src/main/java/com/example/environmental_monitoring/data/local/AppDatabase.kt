package com.example.environmental_monitoring.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        SensorReadingEntity::class,
        DeviceThresholdEntity::class,
        AlarmEventEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sensorReadingDao(): SensorReadingDao
    abstract fun deviceThresholdDao(): DeviceThresholdDao
    abstract fun alarmEventDao(): AlarmEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "environmental_monitoring.db"
                ).build().also { INSTANCE = it }
            }
    }
}
