package com.example.environmental_monitoring.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Una lectura individual de un sensor (temperatura/humedad) recibida vía TTN.
 */
@Entity(tableName = "sensor_readings")
data class SensorReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: String,
    val seq: Int?,
    val temperature: Double,
    val humidity: Double,
    val timestamp: Long
)

/**
 * Umbrales de alarma configurables por dispositivo.
 */
@Entity(tableName = "device_thresholds")
data class DeviceThresholdEntity(
    @PrimaryKey val deviceId: String,
    val displayName: String = deviceId,
    val minTemp: Double = 10.0,
    val maxTemp: Double = 35.0,
    val minHum: Double = 20.0,
    val maxHum: Double = 80.0,
    val alarmsEnabled: Boolean = true
)

enum class AlarmType {
    TEMP_HIGH, TEMP_LOW, HUM_HIGH, HUM_LOW
}

/**
 * Historial de alarmas disparadas.
 */
@Entity(tableName = "alarm_events")
data class AlarmEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: String,
    val type: AlarmType,
    val value: Double,
    val threshold: Double,
    val timestamp: Long,
    val acknowledged: Boolean = false
)
