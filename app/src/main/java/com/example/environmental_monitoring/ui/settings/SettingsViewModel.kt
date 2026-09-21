package com.example.environmental_monitoring.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.environmental_monitoring.data.repository.SensorRepository
import com.example.environmental_monitoring.data.settings.SettingsRepository
import com.example.environmental_monitoring.data.settings.TtnConnectionConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _demoSeeded = MutableStateFlow(false)
    val demoSeeded: StateFlow<Boolean> = _demoSeeded.asStateFlow()

    fun seedDemoData() {
        viewModelScope.launch {
            sensorRepository.seedDemoData()
            _demoSeeded.value = true
        }
    }

    val config: StateFlow<TtnConnectionConfig> =
        repository.config.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TtnConnectionConfig())

    fun saveConnection(
        cluster: String,
        tenant: String,
        applicationId: String,
        apiKey: String,
        customMqttHost: String
    ) {
        viewModelScope.launch {
            repository.saveConnection(cluster, tenant, applicationId, apiKey, customMqttHost)
        }
    }

    fun setServiceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setServiceEnabled(enabled)
        }
    }
}
