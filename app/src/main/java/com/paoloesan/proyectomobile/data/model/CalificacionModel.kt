package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalificacionModel(
    @SerialName("calificacion_id") val calificacionId: Int? = null, // PK Autogenerada
    @SerialName("transaccion_id") val transaccionId: Int, // FK a transacciones
    @SerialName("usuario_evaluador_id") val usuarioEvaluadorId: Int, // FK a usuarios
    @SerialName("usuario_evaluado_id") val usuarioEvaluadoId: Int, // FK a usuarios
    @SerialName("puntaje") val puntaje: Int, // Rango 1 - 5
    @SerialName("comentario") val comentario: String? = null,
    @SerialName("fecha_calificacion") val fechaCalificacion: String? = null
)
