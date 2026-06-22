package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComprobanteModel(
    @SerialName("comprobante_id") val comprobanteId: Int? = null, // PK Autogenerada
    @SerialName("transaccion_id") val transaccionId: Int, // FK a transacciones
    @SerialName("usuario_id") val usuarioId: Int, // FK a usuarios (quien sube el voucher)
    @SerialName("imagen_url") val imagenUrl: String, // URL pública del voucher en Storage
    @SerialName("ia_verificado") val iaVerificado: Boolean? = null,
    @SerialName("fecha_subida") val fechaSubida: String? = null
)

@Serializable
data class VerificarVoucherRequest(
    @SerialName("comprobante_id") val comprobanteId: Int,
    @SerialName("transaccion_id") val transaccionId: Int
)

@Serializable
data class VerificarVoucherResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null
)
