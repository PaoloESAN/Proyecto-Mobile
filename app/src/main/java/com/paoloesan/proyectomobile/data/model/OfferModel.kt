package com.paoloesan.proyectomobile.data.model

/**
 * Modelo de datos para las ofertas del mercado
 * Relacionado con US-13: Inicio de transacción desde mercado
 */
data class OfferModel(
    val offerId: String,
    val title: String,
    val description: String,
    val price: Double,
    val currency: String,
    val seller: String,
    val imageUrl: String
)
