package com.example.environmental_monitoring.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.environmental_monitoring.ui.theme.AlarmRed
import com.example.environmental_monitoring.ui.theme.HumHigh
import com.example.environmental_monitoring.ui.theme.HumLow
import com.example.environmental_monitoring.ui.theme.HumOk
import com.example.environmental_monitoring.ui.theme.OkGreen
import com.example.environmental_monitoring.ui.theme.TempCold
import com.example.environmental_monitoring.ui.theme.TempHot
import com.example.environmental_monitoring.ui.theme.TempOk
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onDeviceClick: (String) -> Unit,
    onConfigureAlarms: (String) -> Unit = {}
) {
    val devices by viewModel.devices.collectAsState()

    if (devices.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Text(
                    text = "Sin dispositivos conectados",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Configura la conexión TTN en Ajustes\no carga datos de prueba.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(devices, key = { it.deviceId }) { device ->
            DeviceCard(
                device = device,
                onClick = { onDeviceClick(device.deviceId) },
                onConfigure = { onConfigureAlarms(device.deviceId) }
            )
        }
    }
}

@Composable
private fun DeviceCard(
    device: DeviceUiState,
    onClick: () -> Unit,
    onConfigure: () -> Unit
) {
    val alarmColor by animateColorAsState(
        targetValue = if (device.inAlarm) AlarmRed else OkGreen,
        animationSpec = tween(600),
        label = "alarmColor"
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Barra lateral de estado (verde = OK, rojo = alarma)
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        color = alarmColor,
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 8.dp, top = 14.dp, bottom = 14.dp)
            ) {
                // Cabecera: nombre + indicador de alarma + botón configurar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Punto de estado pulsante
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(alarmColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = device.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (device.inAlarm) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alarma activa",
                            tint = AlarmRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = onConfigure,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Configurar umbrales",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Métricas: temperatura y humedad lado a lado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricChip(
                        icon = Icons.Default.DeviceThermostat,
                        label = "Temperatura",
                        value = String.format(Locale.getDefault(), "%.1f °C", device.temperature),
                        color = tempColor(device.tempStatus)
                    )
                    MetricChip(
                        icon = Icons.Default.WaterDrop,
                        label = "Humedad",
                        value = String.format(Locale.getDefault(), "%.1f %%", device.humidity),
                        color = humColor(device.humStatus)
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Pie: timestamp + seq + device ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(device.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (device.seq != null) {
                            Text(
                                text = "seq ${device.seq}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            text = device.deviceId,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = color
        )
    }
}

private fun tempColor(status: MetricStatus): Color = when (status) {
    MetricStatus.LOW  -> TempCold
    MetricStatus.OK   -> TempOk
    MetricStatus.HIGH -> TempHot
}

private fun humColor(status: MetricStatus): Color = when (status) {
    MetricStatus.LOW  -> HumLow
    MetricStatus.OK   -> HumOk
    MetricStatus.HIGH -> HumHigh
}

private fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
