package com.paoloesan.proyectomobile.presentation.transaction

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.currentUserAwaitInit
import com.paoloesan.proyectomobile.data.model.ComprobanteModel
import com.paoloesan.proyectomobile.data.model.OfferModel
import com.paoloesan.proyectomobile.data.model.PaymentMethodModel
import com.paoloesan.proyectomobile.data.model.TransactionModel
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import com.paoloesan.proyectomobile.data.model.VerificarVoucherRequest
import com.paoloesan.proyectomobile.data.model.VerificarVoucherResponse
import com.paoloesan.proyectomobile.data.model.VerificacionIaModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BankUiState(
    val isLoading: Boolean = true,
    val currentUserId: Int? = null,
    val myVoucherUploaded: Boolean = false,
    val peerVoucherUploaded: Boolean = false,
    val myConfirmed: Boolean = false,
    val peerConfirmed: Boolean = false,
    val transactionStatus: String = "",
    val errorMessage: String? = null,
    val uploadingVoucher: Boolean = false,
    val voucherPublicUrl: String? = null,
    val myVoucherUrl: String? = null,
    val peerVoucherUrl: String? = null,
    val myIaVerification: VerificacionIaModel? = null,
    val peerIaVerification: VerificacionIaModel? = null,
    val bankName: String = "",
    val accountNumber: String = "",
    val cci: String = "",
    val titularName: String = "",
    val currency: String = "",
    val transactionAmount: Double = 0.0,
    val exchangeRate: Double = 0.0,
    val monedaTengo: String = "USD",
    val monedaRecibo: String = "PEN",
    val montoTengo: Double = 0.0,
    val montoRecibo: Double = 0.0
)

class BankViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BankUiState())
    val uiState: StateFlow<BankUiState> = _uiState.asStateFlow()

    private var transaccionId: Int = 0
    private var esComprador: Boolean = false

    fun initialize(
        transactionIdStr: String,
        isSeller: Boolean,
        status: String
    ) {
        transaccionId = transactionIdStr.toIntOrNull() ?: 0
        esComprador = !isSeller

        _uiState.update {
            it.copy(
                transactionStatus = status,
                myConfirmed = status == "Finalizado" || status == "Finalizada",
                peerConfirmed = status == "Finalizado" || status == "Finalizada",
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val authId = Supabase.client.auth.currentUserAwaitInit()?.id
                if (authId == null) {
                    _uiState.update { it.copy(errorMessage = "Usuario no autenticado", isLoading = false) }
                    return@launch
                }

                val perfil = Supabase.client.postgrest["usuarios"]
                    .select {
                        filter { eq("auth_id", authId) }
                    }
                    .decodeSingle<UserProfileModel>()

                val myUserId = perfil.usuarioId
                _uiState.update { it.copy(currentUserId = myUserId) }

                loadDataFromDatabase()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Error al cargar perfil: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun loadDataFromDatabase() {
        try {
            val myUserId = _uiState.value.currentUserId
            if (transaccionId != 0 && myUserId != null) {
                // 1. Obtener transacción real
                val tx = Supabase.client.postgrest["transacciones"]
                    .select { filter { eq("transaccion_id", transaccionId) } }
                    .decodeSingle<TransactionModel>()

                // 2. Obtener oferta
                val offer = Supabase.client.postgrest["ofertas"]
                    .select { filter { eq("oferta_id", tx.offerId) } }
                    .decodeSingle<OfferModel>()

                // Determinar cuál es el método de pago de la contraparte a la que se le debe pagar
                val paymentMethodId = if (offer.tipoOperacion == "Venta") {
                    if (esComprador) offer.metodoPagoId else tx.metodoPagoCompradorId
                } else {
                    if (esComprador) tx.metodoPagoCompradorId else offer.metodoPagoId
                }

                var bName = ""
                var accNum = ""
                var ccId = ""
                var titName = ""
                var pCurrency = ""

                if (paymentMethodId != null) {
                    try {
                        val pm = Supabase.client.postgrest["metodos_pago"]
                            .select { filter { eq("metodo_pago_id", paymentMethodId) } }
                            .decodeSingle<PaymentMethodModel>()
                        
                        bName = pm.banco
                        accNum = pm.numeroCuenta
                        ccId = if (pm.banco.uppercase().contains("BCP")) "002-${pm.numeroCuenta}-45" else "003-${pm.numeroCuenta}-12"
                        titName = pm.nombreTitular
                        pCurrency = pm.tipoMoneda
                    } catch (_: Exception) {}
                }

                // 3. Obtener comprobantes
                val comprobantes = Supabase.client.postgrest["comprobantes"]
                    .select { filter { eq("transaccion_id", transaccionId) } }
                    .decodeList<ComprobanteModel>()

                val myVoucherObj = comprobantes.find { it.usuarioId == (if (esComprador) tx.usuarioCompradorId else tx.usuarioVendedorId) }
                val peerVoucherObj = comprobantes.find { it.usuarioId == (if (esComprador) tx.usuarioVendedorId else tx.usuarioCompradorId) }

                val myVoucher = myVoucherObj != null
                val peerVoucher = peerVoucherObj != null
                val myVoucherUrl = myVoucherObj?.imagenUrl
                val peerVoucherUrl = peerVoucherObj?.imagenUrl

                var myIaVerification: VerificacionIaModel? = null
                var peerIaVerification: VerificacionIaModel? = null
                try {
                    val verifs = Supabase.client.postgrest["verificaciones_ia"]
                        .select { filter { eq("transaccion_id", transaccionId) } }
                        .decodeList<VerificacionIaModel>()
                    if (myVoucherObj != null) {
                        myIaVerification = verifs.filter { it.comprobanteId == myVoucherObj.comprobanteId }
                            .maxByOrNull { it.verificacionId ?: 0 }
                    }
                    if (peerVoucherObj != null) {
                        peerIaVerification = verifs.filter { it.comprobanteId == peerVoucherObj.comprobanteId }
                            .maxByOrNull { it.verificacionId ?: 0 }
                    }
                } catch (e: Exception) {
                    println("Error loading IA verifications: ${e.message}")
                }

                val myConfirmed = if (esComprador) tx.confirmadoComprador else tx.confirmadoVendedor
                val peerConfirmed = if (esComprador) tx.confirmadoVendedor else tx.confirmadoComprador

                _uiState.update {
                    it.copy(
                        transactionStatus = tx.status,
                        myConfirmed = myConfirmed,
                        peerConfirmed = peerConfirmed,
                        myVoucherUploaded = myVoucher,
                        peerVoucherUploaded = peerVoucher,
                        myVoucherUrl = myVoucherUrl,
                        peerVoucherUrl = peerVoucherUrl,
                        myIaVerification = myIaVerification,
                        peerIaVerification = peerIaVerification,
                        bankName = bName,
                        accountNumber = accNum,
                        cci = ccId,
                        titularName = titName,
                        currency = pCurrency,
                        transactionAmount = tx.amount,
                        exchangeRate = tx.tipoCambioAplicado,
                        monedaTengo = offer.monedaTengo,
                        monedaRecibo = offer.monedaRecibo,
                        montoTengo = offer.montoTengo,
                        montoRecibo = offer.montoRecibo,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = "Error al sincronizar con BD: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }

    fun uploadVoucher(context: Context, uri: Uri) {
        val userId = _uiState.value.currentUserId ?: run {
            _uiState.update { it.copy(errorMessage = "Usuario no identificado") }
            return
        }
        if (transaccionId == 0) return

        _uiState.update { it.copy(uploadingVoucher = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("No se pudo leer el archivo")
                val bytes = inputStream.readBytes()
                inputStream.close()

                val originalFileName = getFileName(context, uri)
                val cleanFileName = originalFileName.replace("\\s+".toRegex(), "_")
                val uniqueFileName = "voucher_${System.currentTimeMillis()}_$cleanFileName"
                val storagePath = "private/$transaccionId/$userId/$uniqueFileName"

                Supabase.client.storage.from("vouchers").upload(
                    path = storagePath,
                    data = bytes
                ) { upsert = true }

                val publicUrl = Supabase.client.storage.from("vouchers").publicUrl(storagePath)

                val comprobante = ComprobanteModel(
                    transaccionId = transaccionId,
                    usuarioId = userId,
                    imagenUrl = publicUrl
                )

                val comprobanteInsertado = Supabase.client.postgrest["comprobantes"]
                    .insert(comprobante) { select() }
                    .decodeSingle<ComprobanteModel>()

                _uiState.update {
                    it.copy(
                        myVoucherUploaded = true,
                        uploadingVoucher = false,
                        voucherPublicUrl = publicUrl,
                        myVoucherUrl = publicUrl
                    )
                }

                comprobanteInsertado.comprobanteId?.let { id ->
                    verificarVoucherIa(id)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        uploadingVoucher = false,
                        errorMessage = "Error al subir comprobante: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private suspend fun verificarVoucherIa(comprobanteId: Int) {
        try {
            _uiState.update { it.copy(errorMessage = null) }
            val response = Supabase.client.functions.invoke(
                function = "verificar-voucher-ia",
                body = VerificarVoucherRequest(
                    comprobanteId = comprobanteId,
                    transaccionId = transaccionId
                )
            )
            val result = response.body<VerificarVoucherResponse>()
            loadDataFromDatabase()
            if (!result.success) {
                _uiState.update {
                    it.copy(errorMessage = result.message ?: "La verificación del comprobante falló")
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = "Error al verificar comprobante con IA: ${e.localizedMessage}")
            }
        }
    }

    fun confirmarRecepcion(onSuccess: () -> Unit) {
        val userId = _uiState.value.currentUserId ?: return
        if (transaccionId == 0) return

        viewModelScope.launch {
            try {
                val campo = if (esComprador) "confirmado_comprador" else "confirmado_vendedor"
                val otherConfirmed = _uiState.value.peerConfirmed

                // 1. Actualizar confirmación de esta parte (Map<String, Boolean>)
                Supabase.client.postgrest["transacciones"].update(
                    mapOf(campo to true)
                ) {
                    filter { eq("transaccion_id", transaccionId) }
                }

                // 2. Si la otra parte ya confirmó, cambiar estado a Finalizado (Map<String, String>)
                if (otherConfirmed) {
                    Supabase.client.postgrest["transacciones"].update(
                        mapOf("estado" to "Finalizado")
                    ) {
                        filter { eq("transaccion_id", transaccionId) }
                    }
                }

                if (otherConfirmed) {
                    try {
                        val tx = Supabase.client.postgrest["transacciones"]
                            .select { filter { eq("transaccion_id", transaccionId) } }
                            .decodeSingle<TransactionModel>()
                        Supabase.client.postgrest["ofertas"].update(
                            mapOf("estado" to "Finalizada")
                        ) {
                            filter { eq("oferta_id", tx.offerId) }
                        }
                    } catch (e: Exception) {
                        println("Error finalizando oferta: ${e.message}")
                    }
                }

                _uiState.update { 
                    it.copy(
                        myConfirmed = true, 
                        transactionStatus = if (otherConfirmed) "Finalizado" else it.transactionStatus
                    ) 
                }

                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al confirmar: ${e.localizedMessage}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    return when {
        uri.scheme == "content" -> {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex("_display_name")
                if (nameIndex != -1 && it.moveToFirst()) {
                    it.getString(nameIndex)
                } else "comprobante.jpg"
            } ?: "comprobante.jpg"
        }
        uri.path != null -> uri.path!!.substringAfterLast("/")
        else -> "comprobante.jpg"
    }
}
