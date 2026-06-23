package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfferModel(
    @SerialName("oferta_id") val offerId: Int? = null, // PK Autogenerada
    @SerialName("usuario_creador_id") val usuarioCreadorId: Int, // FK a usuarios
    @SerialName("metodo_pago_id") val metodoPagoId: Int, // FK a metodos_pago
    @SerialName("tipo_operacion") val tipoOperacion: String, // "Compra" | "Venta"
    @SerialName("moneda_tengo") val monedaTengo: String, // Ej: "PEN"
    @SerialName("moneda_recibo") val monedaRecibo: String, // Ej: "USD"
    @SerialName("monto_tengo") val montoTengo: Double,
    @SerialName("monto_recibo") val montoRecibo: Double,
    @SerialName("tipo_cambio") val price: Double, // Tipo de cambio aplicado
    @SerialName("estado") val estado: String = "Activa", // "Activa" | "En Proceso" | "Inactiva" | "Cancelada" | "Finalizada"
    @SerialName("fecha_publicacion") val fechaPublicacion: String? = null
)
