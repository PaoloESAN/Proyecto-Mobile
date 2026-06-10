package com.paoloesan.proyectomobile.presentation.disputa

data class Disputa(
    val id: Int,
    val estado: String,
    val comprador: String,
    val vendedor: String,
    val transaccion: String,
    val monto: String,
    val mensaje: String
)

