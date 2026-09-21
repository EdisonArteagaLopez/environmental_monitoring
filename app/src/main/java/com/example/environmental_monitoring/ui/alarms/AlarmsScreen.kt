package com.example.environmental_monitoring.ui.alarms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ThermostatAuto
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.environmental_monitoring.data.local.AlarmEventEntity
import com.example.environmental_monitoring.data.local.AlarmType
import com.example.environmental_monitoring.ui.theme.AlarmRed
import com.example.environmental_monitoring.ui.theme.HumHigh
import com.example.environmental_monitoring.ui.theme.HumLow
import com.example.environmental_monitoring.ui.theme.TempCold
import com.example.environmental_monitoring.ui.theme.TempHot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlarmsScreen(viewModel: AlarmsViewModel) {
    val alarms by viewModel.alarms.collectAsState()
    val unread = alarms.count { !it.acknowledged }

    if (alarms.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
                Text("Sin alarmas registradas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Las alarmas aparecen aquí cuando\ntemperatura o humedad salen de los umbrales.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Barra de acción superior
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (unread > 0) {
                    Text(
                        "$unread sin reconocer",
                        style = MaterialTheme.typography.labelMedium,
                        color = AlarmRed
                    )
                } else {
                    Text(
                        "Todas reconocidas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (unread > 0) {
                    Button(
                        onClick = { viewModel.acknowledgeAll() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Marcar todas", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        items(alarms, key = { it.id }) { alarm ->
            AlarmCard(alarm = alarm, onAcknowledge = { viewModel.acknowledge(alarm.id) })
        }
    }
}

@Composable
private fun AlarmCard(alarm: AlarmEventEntity, onAcknowledge: () -> Unit) {
    val (icon, tint) = alarmIconAndTint(alarm.type)
    val isNew = !alarm.acknowledged

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (isNew)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(if (isNew) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono de tipo de alarma
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint     = if (isNew) tint else tint.copy(alpha = 0.4f),
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = describeAlarmType(alarm.type),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isNew) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isNew) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Warning, null, tint = AlarmRed,
                            modifier = Modifier.size(12.dp))
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = alarm.deviceId,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = String.format(
                        Locale.getDefault(),
                        "Valor: %.1f  |  Límite: %.1f",
                        alarm.value, alarm.threshold
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isNew) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = formatTimestamp(alarm.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            if (isNew) {
                IconButton(onClick = onAcknowledge, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Reconocer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun alarmIconAndTint(type: AlarmType): Pair<ImageVector, androidx.compose.ui.graphics.Color> =
    when (type) {
        AlarmType.TEMP_HIGH -> Pair(Icons.Default.ThermostatAuto, TempHot)
        AlarmType.TEMP_LOW  -> Pair(Icons.Default.ThermostatAuto, TempCold)
        AlarmType.HUM_HIGH  -> Pair(Icons.Default.WaterDrop, HumHigh)
        AlarmType.HUM_LOW   -> Pair(Icons.Default.WaterDrop, HumLow)
    }

private fun describeAlarmType(type: AlarmType): String = when (type) {
    AlarmType.TEMP_HIGH -> "Temperatura alta"
    AlarmType.TEMP_LOW  -> "Temperatura baja"
    AlarmType.HUM_HIGH  -> "Humedad alta"
    AlarmType.HUM_LOW   -> "Humedad baja"
}

private fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
