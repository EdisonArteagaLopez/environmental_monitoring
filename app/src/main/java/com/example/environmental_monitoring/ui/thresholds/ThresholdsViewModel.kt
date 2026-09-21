package com.example.environmental_monitoring.ui.thresholds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.environmental_monitoring.data.local.DeviceThresholdEntity
import com.example.environmental_monitoring.data.repository.SensorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThresholdsViewModel(
    private val repository: SensorRepository,
    val deviceId: String
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.getOrCreateThreshold(deviceId)
        }
    }

    val threshold: StateFlow<DeviceThresholdEntity?> =
        repository.observeThresholdForDevice(deviceId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun save(threshold: DeviceThresholdEntity) {
        viewModelScope.launch {
            repository.updateThreshold(threshold)
        }
    }
}
