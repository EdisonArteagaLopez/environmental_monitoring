package com.example.environmental_monitoring.mqtt

import android.util.Log
import com.example.environmental_monitoring.data.settings.TtnConnectionConfig
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Cliente MQTT que se conecta directamente al broker de The Things Network
 * (The Things Stack Cloud) y se suscribe a los mensajes "uplink" (subida)
 * de todos los dispositivos de la aplicación.
 *
 * Broker: {cluster}.cloud.thethings.network:8883 (TLS)
 * Usuario: {application_id}@{tenant}
 * Password: API Key de la aplicación
 * Tópico: v3/{application_id}@{tenant}/devices/+/up
 */
class TtnMqttClient(
    private val config: TtnConnectionConfig,
    private val onMessage: (topic: String, payload: ByteArray) -> Unit,
    private val onConnectionStateChanged: (ConnectionState) -> Unit
) {
    enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

    private var client: Mqtt3AsyncClient? = null

    fun connect() {
        onConnectionStateChanged(ConnectionState.CONNECTING)

        val mqttClient = MqttClient.builder()
            .useMqttVersion3()
            .identifier("android-envmon-${UUID.randomUUID()}")
            .serverHost(config.mqttHost)
            .serverPort(MQTT_TLS_PORT)
            .sslWithDefaultConfig()
            .automaticReconnectWithDefaultConfig()
            .addConnectedListener { onConnectionStateChanged(ConnectionState.CONNECTED) }
            .addDisconnectedListener { onConnectionStateChanged(ConnectionState.DISCONNECTED) }
            .buildAsync()

        client = mqttClient

        mqttClient.connectWith()
            .simpleAuth()
            .username(config.mqttUsername)
            .password(config.apiKey.toByteArray(StandardCharsets.UTF_8))
            .applySimpleAuth()
            .send()
            .whenComplete { connAck, throwable ->
                if (throwable != null) {
                    Log.e(TAG, "MQTT connect FALLÓ: ${throwable.javaClass.simpleName} — ${throwable.message}", throwable)
                    onConnectionStateChanged(ConnectionState.ERROR)
                } else {
                    Log.d(TAG, "MQTT conectado OK. connAck=$connAck")
                    subscribeToUplinks(mqttClient)
                }
            }
    }

    private fun subscribeToUplinks(mqttClient: Mqtt3AsyncClient) {
        Log.d(TAG, "Suscribiendo a tópico: ${config.uplinkTopic}")
        mqttClient.subscribeWith()
            .topicFilter(config.uplinkTopic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback { publish ->
                Log.d(TAG, "Mensaje MQTT recibido: ${publish.topic}")
                onMessage(publish.topic.toString(), publish.payloadAsBytes ?: ByteArray(0))
            }
            .send()
            .whenComplete { subAck, throwable ->
                if (throwable != null) {
                    Log.e(TAG, "Suscripción FALLÓ: ${throwable.message}", throwable)
                } else {
                    Log.d(TAG, "Suscripción OK. subAck=$subAck")
                }
            }
    }

    fun disconnect() {
        val current = client
        client = null
        if (current?.state?.isConnectedOrReconnect == true) {
            current.disconnect()
        }
        onConnectionStateChanged(ConnectionState.DISCONNECTED)
    }

    companion object {
        private const val TAG = "TtnMqttClient"
        private const val MQTT_TLS_PORT = 8883
    }
}
