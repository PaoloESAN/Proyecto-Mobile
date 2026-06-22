package com.paoloesan.proyectomobile.presentation.disputa

import com.paoloesan.proyectomobile.data.model.DisputeModel
import com.paoloesan.proyectomobile.data.model.TransactionModel
import com.paoloesan.proyectomobile.data.model.UserProfileModel

data class Disputa(
    val id: Int,
    val estado: String,
    val comprador: String,
    val vendedor: String,
    val transaccionId: Int,
    val transaccion: String,
    val monto: String,
    val mensaje: String,
    val resolucion: String? = null
) {
    companion object {
        fun fromModels(
            dispute: DisputeModel,
            transaction: TransactionModel,
            compradorProfile: UserProfileModel,
            vendedorProfile: UserProfileModel
        ): Disputa {
            val compradorNombre = "${compradorProfile.nombres} ${compradorProfile.apellidos}"
            val vendedorNombre = "${vendedorProfile.nombres} ${vendedorProfile.apellidos}"
            return Disputa(
                id = dispute.disputaId ?: 0,
                estado = dispute.estado,
                comprador = compradorNombre,
                vendedor = vendedorNombre,
                transaccionId = transaction.transactionId ?: 0,
                transaccion = "TX-${transaction.transactionId}",
                monto = "S/ ${String.format("%.2f", transaction.amount)}",
                mensaje = "Disputa abierta por $compradorNombre",
                resolucion = dispute.resolucion
            )
        }
    }
}
