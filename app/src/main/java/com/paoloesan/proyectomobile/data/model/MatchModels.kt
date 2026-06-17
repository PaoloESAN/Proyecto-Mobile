package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchParams(
    @SerialName("p_usuario_id") val usuarioId: Int,
    @SerialName("p_moneda") val moneda: String,
    @SerialName("p_tipo_operacion") val tipoOperacion: String,
    @SerialName("p_monto") val monto: Double,
    @SerialName("p_tipo_cambio") val tipoCambio: Double
)

@Serializable
data class OfertaMatch(
    @SerialName("oferta_id") val ofertaId: Int,
    @SerialName("usuario_creador_id") val usuarioCreadorId: Int,
    @SerialName("tipo_operacion") val tipoOperacion: String,
    val moneda: String,
    @SerialName("monto_total") val montoTotal: Double,
    @SerialName("monto_minimo") val montoMinimo: Double,
    @SerialName("monto_maximo") val montoMaximo: Double,
    @SerialName("tipo_cambio") val tipoCambio: Double,
    val estado: String,
    @SerialName("fecha_publicacion") val fechaPublicacion: String,
    var nombreCreador: String = "Usuario P2P",
    var banco: String = "Por transferir"
)
