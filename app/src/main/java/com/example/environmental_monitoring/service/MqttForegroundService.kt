package com.example.environmental_monitoring.service

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.environmental_monitoring.R
import com.example.environmental_monitoring.data.local.AlarmEventEntity
import com.example.environmental_monitoring.data.local.AlarmType
import com.example.environmental_monitoring.data.model.TtnUplinkMessage
import com.example.environmental_monitoring.data.local.SensorReadingEntity
import com.example.environmental_monitoring.data.settings.TtnConnectionConfig
import com.example.environmental_monitoring.di.AppContainer
import com.example.environmental_monitoring.mqtt.TtnMqttClient
import com.example.environmental_monitoring.notification.AlarmNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Servicio en foreground que mantiene la conexión MQTT con The Things Network,
 * persiste cada lectura recibida y evalúa los umbrales de alarma configurados.
 *
 * Se inicia/detiene desde la pantalla de Ajustes según [TtnConnectionConfig.serviceEnabled].
 */
class MqttForegroundService : LifecycleService() {

    private lateinit var container: AppContainer
    private var mqttClient: TtnMqttClient? = null
    private var currentConfigKey: String? = null

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate — API ${Build.VERSION.SDK_INT}")
        container = AppContainer.get(applicationContext)

        try {
            ServiceCompat.startForeground(
                this,
                AlarmNotifier.SERVICE_NOTIFICATION_ID,
                AlarmNotifier.buildServiceNotification(
                    this,
                    getString(R.string.service_notification_text_connecting)
                ),
                when {
                    // remoteMessaging reemplaza dataSync (deprecado y bloqueado en API 35+)
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                    else -> 0
                }
            )
            Log.d(TAG, "startForeground OK")
        } catch (e: Exception) {
            Log.e(TAG, "startForeground FALLÓ", e)
        }

        lifecycleScope.launch {
            container.settingsRepository.config.collectLatest { config ->
                Log.d(TAG, "config recibido: valid=${config.isValid} serviceEnabled=${config.serviceEnabled} host=${config.mqttHost} user=${config.mqttUsername}")
                if (!config.serviceEnabled || !config.isValid) {
                    Log.d(TAG, "Servicio desactivado o config inválida — deteniendo")
                    disconnectClient()
                    stopSelf()
                    return@collectLatest
                }

                val key = "${config.cluster}|${config.tenant}|${config.applicationId}|${config.apiKey}"
                if (key != currentConfigKey) {
                    Log.d(TAG, "Config nueva detectada — reconectando")
                    currentConfigKey = key
                    disconnectClient()
                    connectClient(config)
                }
            }
        }
    }

    private fun connectClient(config: TtnConnectionConfig) {
        Log.d(TAG, "Conectando a MQTT: ${config.mqttHost}:8883 usuario=${config.mqttUsername} tópico=${config.uplinkTopic}")
        mqttClient = TtnMqttClient(
            config = config,
            onMessage = { topic, payload ->
                Log.d(TAG, "Mensaje recibido en tópico: $topic (${payload.size} bytes)")
                handleUplink(payload)
            },
            onConnectionStateChanged = { state ->
                Log.d(TAG, "Estado MQTT: $state")
                updateNotification(state)
            }
        ).also { it.connect() }
    }

    private fun disconnectClient() {
        Log.d(TAG, "Desconectando cliente MQTT")
        mqttClient?.disconnect()
        mqttClient = null
    }

    private fun updateNotification(state: TtnMqttClient.ConnectionState) {
        val text = when (state) {
            TtnMqttClient.ConnectionState.CONNECTED -> getString(R.string.service_notification_text_connected)
            TtnMqttClient.ConnectionState.CONNECTING -> getString(R.string.service_notification_text_connecting)
            else -> getString(R.string.service_notification_text_disconnected)
        }
        val notification = AlarmNotifier.buildServiceNotification(this, text)
        NotificationManagerCompat.from(this).notify(AlarmNotifier.SERVICE_NOTIFICATION_ID, notification)
    }

    private fun handleUplink(payload: ByteArray) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val raw = payload.toString(Charsets.UTF_8)
                Log.d(TAG, "Payload RAW: $raw")
                val message = json.decodeFromString<TtnUplinkMessage>(raw)
                val decoded = message.uplinkMessage?.decodedPayload ?: run {
                    Log.w(TAG, "decoded_payload es null — ignorando mensaje")
                    return@launch
                }
                val temp = decoded.temp ?: run {
                    Log.w(TAG, "temp es null en decoded_payload")
                    return@launch
                }
                val hum = decoded.hum ?: run {
                    Log.w(TAG, "hum es null en decoded_payload")
                    return@launch
                }
                val deviceId = message.endDeviceIds?.deviceId
                    ?: decoded.device
                    ?: "unknown"

                Log.d(TAG, "Lectura guardada: deviceId=$deviceId temp=$temp hum=$hum seq=${decoded.seq}")
                val reading = SensorReadingEntity(
                    deviceId = deviceId,
                    seq = decoded.seq,
                    temperature = temp,
                    humidity = hum,
                    timestamp = System.currentTimeMillis()
                )
                container.sensorRepository.insertReading(reading)
                checkThresholds(deviceId, temp, hum)
            } catch (e: Exception) {
                Log.e(TAG, "Error procesando uplink de TTN", e)
            }
        }
    }

    private suspend fun checkThresholds(deviceId: String, temp: Double, hum: Double) {
        val threshold = container.sensorRepository.getOrCreateThreshold(deviceId)
        if (!threshold.alarmsEnabled) return

        maybeRaiseAlarm(
            condition = temp > threshold.maxTemp,
            deviceId = deviceId,
            type = AlarmType.TEMP_HIGH,
            value = temp,
            limit = threshold.maxTemp,
            messageRes = R.string.alarm_temp_high,
            displayName = threshold.displayName
        )
        maybeRaiseAlarm(
            condition = temp < threshold.minTemp,
            deviceId = deviceId,
            type = AlarmType.TEMP_LOW,
            value = temp,
            limit = threshold.minTemp,
            messageRes = R.string.alarm_temp_low,
            displayName = threshold.displayName
        )
        maybeRaiseAlarm(
            condition = hum > threshold.maxHum,
            deviceId = deviceId,
            type = AlarmType.HUM_HIGH,
            value = hum,
            limit = threshold.maxHum,
            messageRes = R.string.alarm_hum_high,
            displayName = threshold.displayName
        )
        maybeRaiseAlarm(
            condition = hum < threshold.minHum,
            deviceId = deviceId,
            type = AlarmType.HUM_LOW,
            value = hum,
            limit = threshold.minHum,
            messageRes = R.string.alarm_hum_low,
            displayName = threshold.displayName
        )
    }

    private suspend fun maybeRaiseAlarm(
        condition: Boolean,
        deviceId: String,
        type: AlarmType,
        value: Double,
        limit: Double,
        messageRes: Int,
        displayName: String
    ) {
        if (!condition) return

        val last = container.sensorRepository.lastAlarmOfType(deviceId, type)
        val now = System.currentTimeMillis()
        if (last != null && now - last.timestamp < ALARM_COOLDOWN_MS) return

        container.sensorRepository.recordAlarm(
            AlarmEventEntity(
                deviceId = deviceId,
                type = type,
                value = value,
                threshold = limit,
                timestamp = now
            )
        )

        val text = getString(messageRes, displayName, value)
        AlarmNotifier.showAlarmNotification(
            this,
            getString(R.string.channel_alarm_name),
            text
        )
    }

    override fun onDestroy() {
        disconnectClient()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MqttForegroundService"
        private const val ALARM_COOLDOWN_MS = 5 * 60 * 1000L

        fun start(context: Context) {
            val intent = Intent(context, MqttForegroundService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MqttForegroundService::class.java))
        }
    }
}
