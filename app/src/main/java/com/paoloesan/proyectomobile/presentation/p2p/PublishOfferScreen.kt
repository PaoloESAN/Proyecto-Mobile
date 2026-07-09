package com.paoloesan.proyectomobile.presentation.p2p

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.PopupAnimation
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.card.Card
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.input.Input
import zed.rainxch.rikkaui.components.ui.label.Label
import zed.rainxch.rikkaui.components.ui.select.Select
import zed.rainxch.rikkaui.components.ui.select.SelectOption
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishOfferScreen(
    navController: NavController,
    viewModel: PublishOfferViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var type by remember { mutableStateOf("Compra") }
    var amount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var monedaTengo by remember { mutableStateOf("USD") }
    var monedaRecibo by remember { mutableStateOf("PEN") }

    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val todasLasMonedas = listOf("USD", "PEN", "MXN", "EUR", "GBP", "JPY")

    val monedaTengoOptions = remember {
        todasLasMonedas.map { SelectOption(it, it) }
    }

    val monedaReciboOptions = remember(monedaTengo) {
        todasLasMonedas
            .filter { !it.equals(monedaTengo, ignoreCase = true) }
            .map { SelectOption(it, it) }
    }

    LaunchedEffect(monedaTengo) {
        val options = todasLasMonedas.filter { !it.equals(monedaTengo, ignoreCase = true) }
        if (options.isNotEmpty()) {
            if (monedaRecibo.equals(monedaTengo, ignoreCase = true) || !options.contains(
                    monedaRecibo
                )
            ) {
                monedaRecibo = options.first()
            }
        }
    }

    // Filter payment methods based on monedaRecibo
    val paymentOptions = remember(uiState.paymentMethods, monedaRecibo) {
        uiState.paymentMethods
            .filter { it.tipoMoneda.equals(monedaRecibo, ignoreCase = true) }
            .map {
                SelectOption(
                    value = it.metodoPagoId.toString(),
                    label = "${it.banco} - ${it.numeroCuenta} (${it.tipoMoneda})"
                )
            }
    }

    LaunchedEffect(monedaRecibo, uiState.paymentMethods) {
        val filtered =
            uiState.paymentMethods.filter { it.tipoMoneda.equals(monedaRecibo, ignoreCase = true) }
        if (filtered.isNotEmpty()) {
            if (paymentMethod.isBlank() || !filtered.any { it.metodoPagoId.toString() == paymentMethod }) {
                paymentMethod = filtered.first().metodoPagoId.toString()
            }
        } else {
            paymentMethod = ""
        }
    }

    // Funciones para calcular el tipo de cambio dinámico
    fun obtenerTasaBase(moneda: String): Double {
        return when (moneda.uppercase()) {
            "USD" -> 1.0
            "PEN" -> 3.80
            "MXN" -> 18.00
            "EUR" -> 0.92
            "GBP" -> 0.78
            "JPY" -> 155.00
            else -> 1.0
        }
    }

    fun obtenerTipoCambio(mTengo: String, mRecibo: String): Double {
        val tasaTengo = obtenerTasaBase(mTengo)
        val tasaRecibo = obtenerTasaBase(mRecibo)
        return tasaRecibo / tasaTengo
    }

    val tipoCambioCalculado by remember(monedaTengo, monedaRecibo) {
        derivedStateOf { obtenerTipoCambio(monedaTengo, monedaRecibo) }
    }

    val montoTengoCalculado by remember(amount, type, tipoCambioCalculado) {
        derivedStateOf {
            val amt = amount.toDoubleOrNull() ?: 0.0
            if (type == "Compra") {
                if (tipoCambioCalculado > 0.0) amt / tipoCambioCalculado else 0.0
            } else {
                amt
            }
        }
    }

    val montoReciboCalculado by remember(amount, type, tipoCambioCalculado) {
        derivedStateOf {
            val amt = amount.toDoubleOrNull() ?: 0.0
            if (type == "Compra") {
                amt
            } else {
                amt * tipoCambioCalculado
            }
        }
    }

    val cantidadLabel by remember(type, monedaTengo, monedaRecibo) {
        derivedStateOf {
            if (type == "Compra") {
                "Cantidad que quieres recibir ($monedaRecibo)"
            } else {
                "Cantidad que tienes para vender ($monedaTengo)"
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.consumeSuccess()
            scope.launch {
                toastState.show(
                    message = "Oferta publicada correctamente",
                    variant = ToastVariant.Success
                )
            }
            amount = ""
            paymentMethod = ""
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch {
                toastState.show(
                    message = message,
                    variant = ToastVariant.Destructive
                )
            }
            viewModel.consumeError()
        }
    }

    val canPublish by remember {
        derivedStateOf {
            val amt = amount.toDoubleOrNull() ?: 0.0
            !uiState.isLoading &&
                    amount.isNotBlank() &&
                    amt > 0.0 &&
                    paymentMethod.isNotBlank() &&
                    monedaTengo.isNotBlank() &&
                    monedaRecibo.isNotBlank() &&
                    !monedaTengo.equals(monedaRecibo, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RikkaTheme.colors.background)
    ) {
        Scaffold(
            containerColor = RikkaTheme.colors.background,
            snackbarHost = {
                ToastHost(
                    hostState = toastState
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    }
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Text(
                        text = "Publicar oferta",
                        variant = TextVariant.H1,
                        color = RikkaTheme.colors.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                // 1. Selector Compra/Venta
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = "¿Qué deseas hacer?")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { type = "Compra" },
                                variant = if (type == "Compra") ButtonVariant.Default else ButtonVariant.Outline,
                                modifier = Modifier.weight(1f),
                                text = "Comprar"
                            )
                            Button(
                                onClick = { type = "Venta" },
                                variant = if (type == "Venta") ButtonVariant.Default else ButtonVariant.Outline,
                                modifier = Modifier.weight(1f),
                                text = "Vender"
                            )
                        }
                    }
                }

                // 2. Moneda que quieres recibir
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = "Moneda que quieres recibir")
                        Select(
                            selectedValue = monedaRecibo,
                            onValueChange = { monedaRecibo = it },
                            options = monedaReciboOptions,
                            placeholder = "Selecciona",
                            animation = PopupAnimation.Fade,
                            maxHeight = 300.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 3. Moneda que tienes
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = "Moneda que tienes")
                        Select(
                            selectedValue = monedaTengo,
                            onValueChange = { monedaTengo = it },
                            options = monedaTengoOptions,
                            placeholder = "Selecciona",
                            animation = PopupAnimation.Fade,
                            maxHeight = 300.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 4. Cantidad Input
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = cantidadLabel)
                        Input(
                            value = amount,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' }) {
                                    amount = input
                                }
                            },
                            placeholder = "Ej: 1000",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = Icons.Default.AttachMoney,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // 5. Método de Pago (cuenta en monedaRecibo)
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = "Tu cuenta bancaria (de la moneda que quieres recibir)")
                        if (paymentOptions.isNotEmpty()) {
                            Select(
                                selectedValue = paymentMethod,
                                onValueChange = { paymentMethod = it },
                                options = paymentOptions,
                                placeholder = "Elige una cuenta...",
                                animation = PopupAnimation.Fade,
                                maxHeight = 300.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = "⚠️ No tienes cuentas registradas en $monedaRecibo. Agrega una en tu perfil.",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.destructive
                            )
                        }
                    }
                }

                // 6. Vista previa de conversión
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = "Vista previa de conversión")
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDoubleOrNull() == 0.0) {
                                    Text(
                                        text = "Completa monedas y cantidad para ver el cálculo automático.",
                                        variant = TextVariant.Small,
                                        color = Color.Gray
                                    )
                                } else {
                                    Text(
                                        text = "Tipo de cambio actual: ${
                                            String.format(
                                                java.util.Locale.US,
                                                "%.6f",
                                                tipoCambioCalculado
                                            )
                                        }",
                                        variant = TextVariant.Small,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Tú entregas: ${
                                            String.format(
                                                java.util.Locale.US,
                                                "%,.2f",
                                                montoTengoCalculado
                                            )
                                        } $monedaTengo",
                                        variant = TextVariant.P,
                                        color = RikkaTheme.colors.onBackground
                                    )
                                    Text(
                                        text = "Tú recibes: ${
                                            String.format(
                                                java.util.Locale.US,
                                                "%,.2f",
                                                montoReciboCalculado
                                            )
                                        } $monedaRecibo",
                                        variant = TextVariant.P,
                                        color = RikkaTheme.colors.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. Botón Publicar
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        enabled = canPublish,
                        onClick = {
                            val selectedMethodId = paymentMethod.toIntOrNull()
                            if (selectedMethodId != null) {
                                viewModel.publishOffer(
                                    metodoPagoId = selectedMethodId,
                                    tipoOperacion = type,
                                    monedaTengo = monedaTengo,
                                    monedaRecibo = monedaRecibo,
                                    montoTengo = montoTengoCalculado,
                                    montoRecibo = montoReciboCalculado,
                                    tipoCambio = tipoCambioCalculado
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        text = "Publicar oferta"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}