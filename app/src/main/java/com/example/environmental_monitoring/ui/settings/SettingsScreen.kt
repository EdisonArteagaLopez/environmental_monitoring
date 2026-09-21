package com.example.environmental_monitoring.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.environmental_monitoring.service.MqttForegroundService

private val CLUSTERS = listOf("eu1", "nam1", "au1")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsState()
    val demoSeeded by viewModel.demoSeeded.collectAsState()

    var initialized by remember { mutableStateOf(false) }
    var cluster by rememberSaveable { mutableStateOf("au1") }
    var tenant by rememberSaveable { mutableStateOf("unicamp") }
    var applicationId by rememberSaveable { mutableStateOf("") }
    var apiKey by rememberSaveable { mutableStateOf("") }
    var customMqttHost by rememberSaveable { mutableStateOf("") }
    var apiKeyVisible by rememberSaveable { mutableStateOf(false) }
    var clusterMenuExpanded by remember { mutableStateOf(false) }

    // Carga los valores guardados la primera vez (o cuando cambia config mientras initialized = false)
    LaunchedEffect(config) {
        if (!initialized && (config.applicationId.isNotBlank() || config.apiKey.isNotBlank())) {
            cluster = config.cluster
            tenant = config.tenant
            applicationId = config.applicationId
            apiKey = config.apiKey
            customMqttHost = config.customMqttHost
            initialized = true
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* el switch refleja el estado real al recomponer */ }

    // Host efectivo que se usará (para la tarjeta de resumen)
    val effectiveHost = customMqttHost.trim().ifBlank { "$cluster.cloud.thethings.network" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Conexión MQTT — The Things Stack", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Conexión directa al broker MQTT (TLS 8883). Soporta TTN Community y despliegues TTI personalizados.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )

        // --- Application ID ---
        OutlinedTextField(
            value = applicationId,
            onValueChange = { applicationId = it },
            label = { Text("Application ID") },
            placeholder = { Text("iotdlt") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // --- API Key ---
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Key") },
            placeholder = { Text("NNSXS.…") },
            singleLine = true,
            visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                    Icon(
                        imageVector = if (apiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // --- Tenant ---
        OutlinedTextField(
            value = tenant,
            onValueChange = { tenant = it },
            label = { Text("Tenant") },
            placeholder = { Text("unicamp  (o \"ttn\" para TTS Community)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // --- Cluster ---
        ExposedDropdownMenuBox(
            expanded = clusterMenuExpanded,
            onExpandedChange = { clusterMenuExpanded = it }
        ) {
            OutlinedTextField(
                value = cluster,
                onValueChange = { cluster = it },
                label = { Text("Cluster") },
                placeholder = { Text("au1") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clusterMenuExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable)
            )
            ExposedDropdownMenu(
                expanded = clusterMenuExpanded,
                onDismissRequest = { clusterMenuExpanded = false }
            ) {
                CLUSTERS.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { cluster = option; clusterMenuExpanded = false }
                    )
                }
            }
        }

        // --- Host MQTT personalizado (campo clave para TTI) ---
        OutlinedTextField(
            value = customMqttHost,
            onValueChange = { customMqttHost = it },
            label = { Text("Host MQTT personalizado (opcional)") },
            placeholder = { Text("unicamp.au1.cloud.thethings.industries") },
            supportingText = {
                Text(
                    text = if (customMqttHost.isBlank())
                        "Vacío → se usará $cluster.cloud.thethings.network"
                    else
                        "Se usará: $customMqttHost",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.saveConnection(
                    cluster = cluster.trim(),
                    tenant = tenant.trim(),
                    applicationId = applicationId.trim(),
                    apiKey = apiKey.trim(),
                    customMqttHost = customMqttHost.trim()
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar conexión")
        }

        HorizontalDivider()

        Text(text = "Monitoreo en segundo plano", style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Activar conexión a TTN / TTI")
                Text(
                    text = if (config.isValid) "Listo para conectar" else "Guarda la conexión primero",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Switch(
                checked = config.serviceEnabled,
                enabled = config.isValid || config.serviceEnabled,
                onCheckedChange = { enabled ->
                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val granted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!granted) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    viewModel.setServiceEnabled(enabled)
                    if (enabled) MqttForegroundService.start(context)
                    else MqttForegroundService.stop(context)
                }
            )
        }

        HorizontalDivider()

        Text(text = "Datos de prueba", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Carga lecturas ficticias de 2 dispositivos virtuales para explorar la UI sin hardware.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Button(
            onClick = { viewModel.seedDemoData() },
            enabled = !demoSeeded,
            modifier = Modifier.fillMaxWidth(),
            colors = if (demoSeeded)
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            else
                ButtonDefaults.buttonColors()
        ) {
            Icon(
                imageVector = if (demoSeeded) Icons.Default.CheckCircle else Icons.Default.BugReport,
                contentDescription = null
            )
            Text(
                text = if (demoSeeded) "  Datos cargados" else "  Cargar datos de prueba",
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        HorizontalDivider()

        // Resumen de los parámetros de conexión calculados
        Card {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Parámetros MQTT efectivos", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)
                Text(text = "Host:    $effectiveHost:8883")
                Text(text = "Usuario: $applicationId@$tenant")
                Text(text = "Tópico:  v3/$applicationId@$tenant/devices/+/up")
            }
        }
    }
}
