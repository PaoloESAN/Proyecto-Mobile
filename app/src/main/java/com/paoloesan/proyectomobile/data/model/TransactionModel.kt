package com.paoloesan.proyectomobile.data.model

/**
 * Modelo de datos para las transacciones
 * Relacionado con US-14, US-15, US-16: Gestión de transacciones y pagos
 */
data class TransactionModel(
    val transactionId: String,
    val offerId: String,
    val status: String,
    val amount: Double,
    val currency: String,
    val createDate: String,
    val paymentMethod: String
)
