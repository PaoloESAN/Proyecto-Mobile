package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageModel(
    @SerialName("mensaje_id") val mensajeId: Int? = null, // PK Autogenerada
    @SerialName("transaccion_id") val transaccionId: Int, // FK a transacciones
    @SerialName("remitente_id") val remitenteId: Int, // FK a usuarios
    @SerialName("contenido") val contenido: String,
    @SerialName("fecha_envio") val fechaEnvio: String? = null
)
