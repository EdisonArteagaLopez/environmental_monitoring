package com.example.environmental_monitoring.ui.devicedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.environmental_monitoring.ui.components.SimpleLineChart
import com.example.environmental_monitoring.ui.theme.HumOk
import com.example.environmental_monitoring.ui.theme.TempOk
import java.util.Locale

@Composable
fun DeviceDetailScreen(
    viewModel: DeviceDetailViewModel,
    onConfigureThresholds: () -> Unit
) {
    val readings  by viewModel.readings.collectAsState()
    val threshold by viewModel.threshold.collectAsState()

    LazyColumn(
        modifier        = Modifier.fillMaxWidth(),
        contentPadding  = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado: nombre + ID
        item {
            Column {
                Text(
                    text  = threshold?.displayName ?: viewModel.deviceId,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text  = viewModel.deviceId,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        // Tarjetas de métricas actuales
        item {
            val latest = readings.lastOrNull()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    icon    = Icons.Default.DeviceThermostat,
                    label   = "Temperatura",
                    value   = latest?.let { String.format(Locale.getDefault(), "%.1f °C", it.temperature) } ?: "--",
                    tint    = TempOk,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon    = Icons.Default.WaterDrop,
                    label   = "Humedad",
                    value   = latest?.let { String.format(Locale.getDefault(), "%.1f %%", it.humidity) } ?: "--",
                    tint    = HumOk,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Botón de configuración de umbrales
        item {
            Button(
                onClick  = onConfigureThresholds,
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Configurar umbrales de alarma")
            }
        }

        // Umbral actual (resumen rápido)
        if (threshold != null) {
            item {
                val t = threshold!!
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ThresholdPill(label = "Temp", min = t.minTemp, max = t.maxTemp, unit = "°C")
                        ThresholdPill(label = "Hum",  min = t.minHum,  max = t.maxHum,  unit = "%")
                    }
                }
            }
        }

        // Gráfica de temperatura
        item {
            ChartSection(
                title  = "Temperatura (°C) — últimas ${readings.size} lecturas",
                chart  = {
                    SimpleLineChart(
                        values       = readings.map { it.temperature.toFloat() },
                        minThreshold = threshold?.minTemp?.toFloat(),
                        maxThreshold = threshold?.maxTemp?.toFloat(),
                        lineColor    = TempOk
                    )
                }
            )
        }

        // Gráfica de humedad
        item {
            ChartSection(
                title  = "Humedad (%) — últimas ${readings.size} lecturas",
                chart  = {
                    SimpleLineChart(
                        values       = readings.map { it.humidity.toFloat() },
                        minThreshold = threshold?.minHum?.toFloat(),
                        maxThreshold = threshold?.maxHum?.toFloat(),
                        lineColor    = HumOk
                    )
                }
            )
        }

        if (readings.isEmpty()) {
            item {
                Text(
                    text  = "Aún no hay lecturas para este dispositivo.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape    = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Spacer(Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = tint)
        }
    }
}

@Composable
private fun ThresholdPill(label: String, min: Double, max: Double, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text  = String.format(Locale.getDefault(), "%.0f – %.0f %s", min, max, unit),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ChartSection(title: String, chart: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape  = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text  = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(8.dp))
            chart()
        }
    }
}
