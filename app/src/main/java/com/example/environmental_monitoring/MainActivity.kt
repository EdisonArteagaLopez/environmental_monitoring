package com.example.environmental_monitoring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.environmental_monitoring.ui.navigation.EnvironmentalMonitoringApp
import com.example.environmental_monitoring.ui.theme.EnvironmentalMonitoringTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnvironmentalMonitoringTheme {
                EnvironmentalMonitoringApp()
            }
        }
    }
}
