package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodModel(
    @SerialName("metodo_pago_id") val metodoPagoId: Int? = null, // PK Autogenerada
    @SerialName("usuario_id") val usuarioId: Int, // FK a usuarios
    @SerialName("banco") val banco: String,
    @SerialName("numero_cuenta") val numeroCuenta: String,
    @SerialName("nombre_titular") val nombreTitular: String,
    @SerialName("tipo_moneda") val tipoMoneda: String, // "USD" | "PEN"
    @SerialName("estado") val estado: String = "Activo", // "Activo" | "Inactivo"
    @SerialName("fecha_creacion") val fechaCreacion: String? = null
)
