package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileModel(
    @SerialName("usuario_id") val usuarioId: Int? = null,
    @SerialName("auth_id") val authId: String? = null,
    @SerialName("nombres") val nombres: String,
    @SerialName("apellidos") val apellidos: String,
    @SerialName("correo") val correo: String,
    @SerialName("contrasena_hash") val contrasenaHash: String = "",
    @SerialName("rol") val rol: String = "Usuario",
    @SerialName("estado") val estado: String = "Activo",
    @SerialName("fecha_registro") val fechaRegistro: String? = null,
    @SerialName("es_verificado") val esVerificado: Boolean = false,
    @SerialName("dni_frontal_url") val dniFrontalUrl: String? = null,
    @SerialName("dni_posterior_url") val dniPosteriorUrl: String? = null,
    @SerialName("calificacion") val calificacion: Double = 0.00,
    @SerialName("bloqueos_anteriores") val bloqueosAnteriores: Int = 0,
    @SerialName("bloqueado_hasta") val bloqueadoHasta: String? = null
)
