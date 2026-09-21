package com.example.environmental_monitoring.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ttn_settings")

/**
 * Configuración de conexión MQTT a The Things Stack (TTS / TTI).
 *
 * Soporta tanto The Things Network Community (cloud.thethings.network)
 * como despliegues TTI personalizados (e.g. unicamp.au1.cloud.thethings.industries).
 *
 * Si [customMqttHost] está en blanco se computa como "{cluster}.cloud.thethings.network".
 * Para despliegues TTI, pon el host completo en [customMqttHost].
 *
 * Ejemplo para este proyecto:
 *   cluster       = "au1"
 *   tenant        = "unicamp"
 *   applicationId = "iotdlt"
 *   customMqttHost= "unicamp.au1.cloud.thethings.industries"
 *   apiKey        = "NNSXS.FO7U4…"
 */
data class TtnConnectionConfig(
    val cluster: String = "au1",
    val tenant: String = "unicamp",
    val applicationId: String = "",
    val apiKey: String = "",
    val customMqttHost: String = "",
    val serviceEnabled: Boolean = false
) {
    val isValid: Boolean
        get() = applicationId.isNotBlank() && apiKey.isNotBlank() && mqttHost.isNotBlank()

    /** Host MQTT efectivo: custom si se especificó, otherwise derivado del cluster. */
    val mqttHost: String
        get() = customMqttHost.trim().ifBlank { "$cluster.cloud.thethings.network" }

    val mqttUsername: String get() = "$applicationId@$tenant"
    val uplinkTopic: String get() = "v3/$applicationId@$tenant/devices/+/up"
}

class SettingsRepository(private val context: Context) {

    private object Keys {
        val CLUSTER = stringPreferencesKey("cluster")
        val TENANT = stringPreferencesKey("tenant")
        val APP_ID = stringPreferencesKey("application_id")
        val API_KEY = stringPreferencesKey("api_key")
        val CUSTOM_MQTT_HOST = stringPreferencesKey("custom_mqtt_host")
        val SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
    }

    val config: Flow<TtnConnectionConfig> = context.dataStore.data.map { prefs ->
        TtnConnectionConfig(
            cluster = prefs[Keys.CLUSTER] ?: "au1",
            tenant = prefs[Keys.TENANT] ?: "unicamp",
            applicationId = prefs[Keys.APP_ID] ?: "",
            apiKey = prefs[Keys.API_KEY] ?: "",
            customMqttHost = prefs[Keys.CUSTOM_MQTT_HOST] ?: "",
            serviceEnabled = prefs[Keys.SERVICE_ENABLED] ?: false
        )
    }

    suspend fun saveConnection(
        cluster: String,
        tenant: String,
        applicationId: String,
        apiKey: String,
        customMqttHost: String = ""
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CLUSTER] = cluster
            prefs[Keys.TENANT] = tenant
            prefs[Keys.APP_ID] = applicationId
            prefs[Keys.API_KEY] = apiKey
            prefs[Keys.CUSTOM_MQTT_HOST] = customMqttHost
        }
    }

    suspend fun setServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SERVICE_ENABLED] = enabled
        }
    }
}
