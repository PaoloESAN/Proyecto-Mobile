package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionModel(
    @SerialName("transaccion_id") val transactionId: Int? = null,
    @SerialName("oferta_id") val offerId: Int,
    @SerialName("usuario_comprador_id") val usuarioCompradorId: Int,
    @SerialName("usuario_vendedor_id") val usuarioVendedorId: Int,
    @SerialName("metodo_pago_comprador_id") val metodoPagoCompradorId: Int? = null,
    @SerialName("monto_operacion") val amount: Double,
    @SerialName("tipo_cambio_aplicado") val tipoCambioAplicado: Double = 0.0,
    @SerialName("estado") val status: String = "Pendiente",
    @SerialName("confirmado_comprador") val confirmadoComprador: Boolean = false,
    @SerialName("confirmado_vendedor") val confirmadoVendedor: Boolean = false,
    @SerialName("ya_calificado") val yaCalificado: Boolean = false,
    @SerialName("fecha_inicio") val createDate: String? = null,
    @SerialName("fecha_actualizacion") val fechaActualizacion: String? = null
)
