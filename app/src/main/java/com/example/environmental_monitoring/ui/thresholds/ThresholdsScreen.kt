package com.example.environmental_monitoring.ui.thresholds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ThresholdsScreen(viewModel: ThresholdsViewModel) {
    val threshold by viewModel.threshold.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var minTemp by remember { mutableStateOf("") }
    var maxTemp by remember { mutableStateOf("") }
    var minHum by remember { mutableStateOf("") }
    var maxHum by remember { mutableStateOf("") }
    var alarmsEnabled by remember { mutableStateOf(true) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(threshold) {
        val t = threshold
        if (t != null && !initialized) {
            displayName = t.displayName
            minTemp = t.minTemp.toString()
            maxTemp = t.maxTemp.toString()
            minHum = t.minHum.toString()
            maxHum = t.maxHum.toString()
            alarmsEnabled = t.alarmsEnabled
            initialized = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Umbrales de alarma", style = MaterialTheme.typography.titleLarge)
        Text(text = viewModel.deviceId, style = MaterialTheme.typography.labelSmall)

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Nombre del dispositivo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Alarmas activas")
            Switch(checked = alarmsEnabled, onCheckedChange = { alarmsEnabled = it })
        }

        HorizontalDivider()
        Text(text = "Temperatura (°C)", style = MaterialTheme.typography.titleLarge)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(label = "Mínima", value = minTemp, onChange = { minTemp = it }, modifier = Modifier.weight(1f))
            NumberField(label = "Máxima", value = maxTemp, onChange = { maxTemp = it }, modifier = Modifier.weight(1f))
        }

        HorizontalDivider()
        Text(text = "Humedad (%)", style = MaterialTheme.typography.titleLarge)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(label = "Mínima", value = minHum, onChange = { minHum = it }, modifier = Modifier.weight(1f))
            NumberField(label = "Máxima", value = maxHum, onChange = { maxHum = it }, modifier = Modifier.weight(1f))
        }

        Button(
            onClick = {
                val current = threshold ?: return@Button
                viewModel.save(
                    current.copy(
                        displayName = displayName.ifBlank { viewModel.deviceId },
                        minTemp = minTemp.toDoubleOrNull() ?: current.minTemp,
                        maxTemp = maxTemp.toDoubleOrNull() ?: current.maxTemp,
                        minHum = minHum.toDoubleOrNull() ?: current.minHum,
                        maxHum = maxHum.toDoubleOrNull() ?: current.maxHum,
                        alarmsEnabled = alarmsEnabled
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar umbrales")
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}
