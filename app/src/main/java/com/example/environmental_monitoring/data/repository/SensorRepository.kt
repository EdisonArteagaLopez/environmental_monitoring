package com.example.environmental_monitoring.data.repository

import com.example.environmental_monitoring.data.local.AlarmEventDao
import com.example.environmental_monitoring.data.local.AlarmEventEntity
import com.example.environmental_monitoring.data.local.AlarmType
import com.example.environmental_monitoring.data.local.DeviceThresholdDao
import com.example.environmental_monitoring.data.local.DeviceThresholdEntity
import com.example.environmental_monitoring.data.local.SensorReadingDao
import com.example.environmental_monitoring.data.local.SensorReadingEntity
import kotlinx.coroutines.flow.Flow

/**
 * Punto único de acceso a la base de datos local: lecturas de sensores,
 * umbrales de alarma por dispositivo e historial de alarmas.
 */
class SensorRepository(
    private val readingDao: SensorReadingDao,
    private val thresholdDao: DeviceThresholdDao,
    private val alarmDao: AlarmEventDao
) {
    // Lecturas
    fun latestReadingPerDevice(): Flow<List<SensorReadingEntity>> = readingDao.latestReadingPerDevice()

    fun recentReadingsForDevice(deviceId: String, limit: Int = 100): Flow<List<SensorReadingEntity>> =
        readingDao.recentReadingsForDevice(deviceId, limit)

    fun distinctDeviceIds(): Flow<List<String>> = readingDao.distinctDeviceIds()

    suspend fun insertReading(reading: SensorReadingEntity): Long = readingDao.insert(reading)

    // Umbrales
    fun observeThresholds(): Flow<List<DeviceThresholdEntity>> = thresholdDao.observeAll()

    fun observeThresholdForDevice(deviceId: String): Flow<DeviceThresholdEntity?> =
        thresholdDao.observeForDevice(deviceId)

    suspend fun getOrCreateThreshold(deviceId: String): DeviceThresholdEntity {
        thresholdDao.insertIfAbsent(DeviceThresholdEntity(deviceId = deviceId, displayName = deviceId))
        return thresholdDao.getForDevice(deviceId) ?: DeviceThresholdEntity(deviceId = deviceId)
    }

    suspend fun updateThreshold(threshold: DeviceThresholdEntity) = thresholdDao.update(threshold)

    // Alarmas
    fun recentAlarms(limit: Int = 200): Flow<List<AlarmEventEntity>> = alarmDao.recentAlarms(limit)

    fun unacknowledgedAlarms(): Flow<List<AlarmEventEntity>> = alarmDao.unacknowledgedAlarms()

    suspend fun acknowledgeAlarm(id: Long) = alarmDao.acknowledge(id)

    suspend fun acknowledgeAllAlarms() = alarmDao.acknowledgeAll()

    suspend fun recordAlarm(event: AlarmEventEntity): Long = alarmDao.insert(event)

    suspend fun lastAlarmOfType(deviceId: String, type: AlarmType): AlarmEventEntity? =
        alarmDao.lastAlarmOfType(deviceId, type)

    /**
     * Inserta datos de demostración: 2 dispositivos, 30 lecturas cada uno
     * distribuidas en la última hora con variación realista de temperatura y humedad.
     * También crea una alarma de ejemplo para lorawan_node_1.
     */
    suspend fun seedDemoData() {
        val devices = listOf(
            Triple("lorawan_node_1", 24.0, 58.0),   // base temp, base hum
            Triple("lorawan_node_2", 19.5, 72.0)
        )
        val now = System.currentTimeMillis()
        val intervalMs = 2 * 60 * 1000L  // lectura cada 2 minutos → 60 min de historia

        devices.forEach { (deviceId, baseTemp, baseHum) ->
            // Crear umbral si no existe
            thresholdDao.insertIfAbsent(
                DeviceThresholdEntity(
                    deviceId = deviceId,
                    displayName = deviceId.replace("_", " ").replaceFirstChar { it.uppercase() },
                    minTemp = 15.0, maxTemp = 30.0,
                    minHum = 30.0, maxHum = 80.0,
                    alarmsEnabled = true
                )
            )
            // 30 lecturas, de más antigua a más reciente
            repeat(30) { i ->
                val noise = (i % 5 - 2) * 0.4   // ±0.8 oscilación
                val trend = i * 0.05             // tendencia leve ascendente
                readingDao.insert(
                    SensorReadingEntity(
                        deviceId = deviceId,
                        seq = i,
                        temperature = baseTemp + noise + trend,
                        humidity = baseHum - noise * 1.5,
                        timestamp = now - (30 - i) * intervalMs
                    )
                )
            }
        }

        // Una alarma de ejemplo en el historial para lorawan_node_1
        alarmDao.insert(
            AlarmEventEntity(
                deviceId = "lorawan_node_1",
                type = AlarmType.TEMP_HIGH,
                value = 30.8,
                threshold = 30.0,
                timestamp = now - 15 * 60 * 1000L,
                acknowledged = false
            )
        )
    }
}
