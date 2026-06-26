package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlertaCambioModel(
    @SerialName("alerta_id") val alertaId: Int? = null, // PK Autogenerada
    @SerialName("usuario_id") val usuarioId: Int, // FK a usuarios
    @SerialName("moneda") val moneda: String, // Ej: "USD"
    @SerialName("tipo_cambio_deseado") val tipoCambioDeseado: Double,
    @SerialName("estado") val estado: String = "Activa", // "Activa" | "Inactiva"
    @SerialName("fecha_creacion") val fechaCreacion: String? = null
)
