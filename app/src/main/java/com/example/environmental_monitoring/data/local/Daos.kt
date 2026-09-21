package com.example.environmental_monitoring.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorReadingDao {

    @Insert
    suspend fun insert(reading: SensorReadingEntity): Long

    /** Última lectura de cada dispositivo, para el dashboard. */
    @Query(
        """
        SELECT * FROM sensor_readings
        WHERE id IN (SELECT MAX(id) FROM sensor_readings GROUP BY deviceId)
        ORDER BY deviceId ASC
        """
    )
    fun latestReadingPerDevice(): Flow<List<SensorReadingEntity>>

    @Query(
        """
        SELECT * FROM sensor_readings
        WHERE deviceId = :deviceId
        ORDER BY timestamp DESC
        LIMIT :limit
        """
    )
    fun recentReadingsForDevice(deviceId: String, limit: Int = 100): Flow<List<SensorReadingEntity>>

    @Query("SELECT DISTINCT deviceId FROM sensor_readings ORDER BY deviceId ASC")
    fun distinctDeviceIds(): Flow<List<String>>

    @Query("DELETE FROM sensor_readings WHERE timestamp < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)
}

@Dao
interface DeviceThresholdDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(threshold: DeviceThresholdEntity)

    @Update
    suspend fun update(threshold: DeviceThresholdEntity)

    @Query("SELECT * FROM device_thresholds WHERE deviceId = :deviceId")
    suspend fun getForDevice(deviceId: String): DeviceThresholdEntity?

    @Query("SELECT * FROM device_thresholds WHERE deviceId = :deviceId")
    fun observeForDevice(deviceId: String): Flow<DeviceThresholdEntity?>

    @Query("SELECT * FROM device_thresholds ORDER BY deviceId ASC")
    fun observeAll(): Flow<List<DeviceThresholdEntity>>
}

@Dao
interface AlarmEventDao {

    @Insert
    suspend fun insert(event: AlarmEventEntity): Long

    @Update
    suspend fun update(event: AlarmEventEntity)

    @Query("SELECT * FROM alarm_events ORDER BY timestamp DESC LIMIT :limit")
    fun recentAlarms(limit: Int = 200): Flow<List<AlarmEventEntity>>

    @Query("SELECT * FROM alarm_events WHERE acknowledged = 0 ORDER BY timestamp DESC")
    fun unacknowledgedAlarms(): Flow<List<AlarmEventEntity>>

    @Query("UPDATE alarm_events SET acknowledged = 1 WHERE id = :id")
    suspend fun acknowledge(id: Long)

    @Query("UPDATE alarm_events SET acknowledged = 1")
    suspend fun acknowledgeAll()

    /** Última alarma del mismo tipo para un dispositivo, usada para evitar spam de notificaciones. */
    @Query(
        """
        SELECT * FROM alarm_events
        WHERE deviceId = :deviceId AND type = :type
        ORDER BY timestamp DESC LIMIT 1
        """
    )
    suspend fun lastAlarmOfType(deviceId: String, type: AlarmType): AlarmEventEntity?
}
