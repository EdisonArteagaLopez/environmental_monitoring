package com.example.environmental_monitoring

import android.app.Application
import com.example.environmental_monitoring.notification.AlarmNotifier

class EnvironmentalMonitoringApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AlarmNotifier.createNotificationChannels(this)
    }
}
