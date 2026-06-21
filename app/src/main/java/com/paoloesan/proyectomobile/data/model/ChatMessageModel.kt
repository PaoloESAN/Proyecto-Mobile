package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageModel(
    @SerialName("mensaje_id") val mensajeId: Int? = null,
    @SerialName("transaccion_id") val transaccionId: Int,
    @SerialName("remitente_id") val remitenteId: Int,
    @SerialName("contenido") val contenido: String,
    @SerialName("fecha_envio") val fechaEnvio: String? = null
)
