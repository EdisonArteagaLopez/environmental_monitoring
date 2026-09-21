package com.example.environmental_monitoring.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo del JSON publicado por The Things Network (v3) en el tópico
 * `v3/{application_id}@{tenant}/devices/+/up`.
 *
 * Solo se mapean los campos que necesitamos; el resto se ignora gracias a
 * `ignoreUnknownKeys = true` en el Json configurado en [TtnJson].
 */
@Serializable
data class TtnUplinkMessage(
    @SerialName("end_device_ids") val endDeviceIds: EndDeviceIds? = null,
    @SerialName("received_at") val receivedAt: String? = null,
    @SerialName("uplink_message") val uplinkMessage: UplinkMessage? = null
)

@Serializable
data class EndDeviceIds(
    @SerialName("device_id") val deviceId: String? = null,
    @SerialName("application_ids") val applicationIds: ApplicationIds? = null
)

@Serializable
data class ApplicationIds(
    @SerialName("application_id") val applicationId: String? = null
)

@Serializable
data class UplinkMessage(
    @SerialName("f_port") val fPort: Int? = null,
    @SerialName("f_cnt") val fCnt: Int? = null,
    @SerialName("decoded_payload") val decodedPayload: DecodedPayload? = null,
    @SerialName("received_at") val receivedAt: String? = null
)

/**
 * Corresponde exactamente al objeto `data` devuelto por el `decodeUplink`
 * configurado en TTN:
 * ```js
 * return { data: { device, seq, temp, hum } }
 * ```
 */
@Serializable
data class DecodedPayload(
    val device: String? = null,
    val seq: Int? = null,
    val temp: Double? = null,
    val hum: Double? = null
)
