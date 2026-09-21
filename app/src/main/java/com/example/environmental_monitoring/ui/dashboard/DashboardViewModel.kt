package com.example.environmental_monitoring.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.environmental_monitoring.data.local.DeviceThresholdEntity
import com.example.environmental_monitoring.data.local.SensorReadingEntity
import com.example.environmental_monitoring.data.repository.SensorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class MetricStatus { OK, LOW, HIGH }

data class DeviceUiState(
    val deviceId: String,
    val displayName: String,
    val temperature: Double,
    val humidity: Double,
    val timestamp: Long,
    val seq: Int?,
    val tempStatus: MetricStatus,
    val humStatus: MetricStatus,
    val inAlarm: Boolean = tempStatus != MetricStatus.OK || humStatus != MetricStatus.OK
)

class DashboardViewModel(private val repository: SensorRepository) : ViewModel() {

    val devices: StateFlow<List<DeviceUiState>> =
        combine(
            repository.latestReadingPerDevice(),
            repository.observeThresholds()
        ) { readings, thresholds ->
            val thresholdMap = thresholds.associateBy { it.deviceId }
            readings.sortedBy { it.deviceId }
                .map { reading -> reading.toUiState(thresholdMap[reading.deviceId]) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unacknowledgedCount: StateFlow<Int> =
        repository.unacknowledgedAlarms()
            .combine(repository.latestReadingPerDevice()) { alarms, _ -> alarms.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun SensorReadingEntity.toUiState(threshold: DeviceThresholdEntity?): DeviceUiState {
        val tempStatus = when {
            threshold == null || !threshold.alarmsEnabled -> MetricStatus.OK
            temperature > threshold.maxTemp -> MetricStatus.HIGH
            temperature < threshold.minTemp -> MetricStatus.LOW
            else -> MetricStatus.OK
        }
        val humStatus = when {
            threshold == null || !threshold.alarmsEnabled -> MetricStatus.OK
            humidity > threshold.maxHum -> MetricStatus.HIGH
            humidity < threshold.minHum -> MetricStatus.LOW
            else -> MetricStatus.OK
        }
        return DeviceUiState(
            deviceId    = deviceId,
            displayName = threshold?.displayName ?: deviceId,
            temperature = temperature,
            humidity    = humidity,
            timestamp   = timestamp,
            seq         = seq,
            tempStatus  = tempStatus,
            humStatus   = humStatus
        )
    }
}
