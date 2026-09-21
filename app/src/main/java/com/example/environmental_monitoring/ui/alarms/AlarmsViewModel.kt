package com.example.environmental_monitoring.ui.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.environmental_monitoring.data.local.AlarmEventEntity
import com.example.environmental_monitoring.data.repository.SensorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmsViewModel(private val repository: SensorRepository) : ViewModel() {

    val alarms: StateFlow<List<AlarmEventEntity>> =
        repository.recentAlarms()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun acknowledge(id: Long) {
        viewModelScope.launch { repository.acknowledgeAlarm(id) }
    }

    fun acknowledgeAll() {
        viewModelScope.launch { repository.acknowledgeAllAlarms() }
    }
}
