package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class TransactionLogModel(
    @SerialName("log_id") val logId: Long? = null,
    @SerialName("transaccion_id") val transaccionId: Int? = null,
    @SerialName("tipo_evento") val tipoEvento: String,
    @SerialName("descripcion") val descripcion: String,
    @SerialName("usuario_id") val usuarioId: Int? = null,
    @SerialName("datos_extra") val datosExtra: JsonElement? = null,
    @SerialName("fecha_evento") val fechaEvento: String? = null
)
