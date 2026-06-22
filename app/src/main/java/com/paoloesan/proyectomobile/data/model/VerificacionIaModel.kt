package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerificacionIaModel(
    @SerialName("verificacion_id") val verificacionId: Int? = null,
    @SerialName("comprobante_id") val comprobanteId: Int,
    @SerialName("transaccion_id") val transaccionId: Int,
    @SerialName("es_valido") val esValido: Boolean,
    @SerialName("datos_extraidos") val datosExtraidos: String, // String formato JSON de campos leídos (banco, monto, nro_operacion, etc.)
    @SerialName("mensaje_error") val mensajeError: String? = null,
    @SerialName("fecha_analisis") val fechaAnalisis: String? = null
)
