package com.paoloesan.proyectomobile.presentation.disputa

val disputasFalsas = listOf(

    Disputa(
        id = 1,
        estado = "Activa",
        comprador = "Juan Pérez",
        vendedor = "Carlos López",
        transaccion = "TX-001",
        monto = "S/ 500.00",
        mensaje = "El comprador afirma haber realizado la transferencia, pero el vendedor indica que el saldo aún no aparece en su cuenta bancaria."
    ),

    Disputa(
        id = 2,
        estado = "Activa",
        comprador = "María Torres",
        vendedor = "Ana García",
        transaccion = "TX-002",
        monto = "S/ 350.00",
        mensaje = "El voucher adjunto de Yape parece alterado o modificado en el monto final."
    ),

    Disputa(
        id = 3,
        estado = "Activa",
        comprador = "Pedro Ruiz",
        vendedor = "Luis Mendoza",
        transaccion = "TX-003",
        monto = "S/ 250.00",
        mensaje = "El comprador transfirió desde una cuenta de un tercero no titular."
    )
)