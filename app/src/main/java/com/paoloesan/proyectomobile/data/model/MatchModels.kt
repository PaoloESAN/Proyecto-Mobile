package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchParams(
    @SerialName("p_usuario_id") val usuarioId: Int,
    @SerialName("p_moneda_tengo") val monedaTengo: String,
    @SerialName("p_moneda_recibo") val monedaRecibo: String,
    @SerialName("p_monto_tengo") val montoTengo: Double,
    @SerialName("p_monto_recibo") val montoRecibo: Double,
    @SerialName("p_tipo_operacion") val tipoOperacion: String
)

@Serializable
data class OfertaMatch(
    @SerialName("oferta_id") val ofertaId: Int,
    @SerialName("usuario_creador_id") val usuarioCreadorId: Int,
    @SerialName("tipo_operacion") val tipoOperacion: String,
    @SerialName("moneda_tengo") val monedaTengo: String,
    @SerialName("moneda_recibo") val monedaRecibo: String,
    @SerialName("monto_tengo") val montoTengo: Double,
    @SerialName("monto_recibo") val montoRecibo: Double,
    @SerialName("tipo_cambio") val tipoCambio: Double,
    val estado: String,
    @SerialName("fecha_publicacion") val fechaPublicacion: String,
    var nombreCreador: String = "Usuario P2P",
    var banco: String = "Por transferir"
)
