package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DisputeModel(
    @SerialName("disputa_id") val disputaId: Int? = null,
    @SerialName("transaccion_id") val transaccionId: Int,
    @SerialName("usuario_reportador_id") val usuarioReportadorId: Int,
    @SerialName("estado") val estado: String = "Abierta",
    @SerialName("resolucion") val resolucion: String? = null,
    @SerialName("fecha_apertura") val fechaApertura: String? = null,
    @SerialName("fecha_cierre") val fechaCierre: String? = null
)
