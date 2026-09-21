package com.example.environmental_monitoring.ui.devicedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.environmental_monitoring.data.local.DeviceThresholdEntity
import com.example.environmental_monitoring.data.local.SensorReadingEntity
import com.example.environmental_monitoring.data.repository.SensorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DeviceDetailViewModel(
    private val repository: SensorRepository,
    val deviceId: String
) : ViewModel() {

    /** Ordenadas de más antigua a más reciente, para graficar de izquierda a derecha. */
    val readings: StateFlow<List<SensorReadingEntity>> =
        repository.recentReadingsForDevice(deviceId, limit = 100)
            .map { it.reversed() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val threshold: StateFlow<DeviceThresholdEntity?> =
        repository.observeThresholdForDevice(deviceId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
