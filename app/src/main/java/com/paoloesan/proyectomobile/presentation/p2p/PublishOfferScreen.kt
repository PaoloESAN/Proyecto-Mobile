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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
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

    // Estados para publicación
    var showPublishSheet by remember { mutableStateOf(false) }
    val publishSheetState = rememberModalBottomSheetState()

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
            if (monedaRecibo.equals(monedaTengo, ignoreCase = true) || !options.contains(monedaRecibo)) {
                monedaRecibo = options.first()
            }
        }
    }

    // Filter payment methods based on monedaTengo
    val paymentOptions = remember(uiState.paymentMethods, monedaTengo) {
        uiState.paymentMethods
            .filter { it.tipoMoneda.equals(monedaTengo, ignoreCase = true) }
            .map {
                SelectOption(
                    value = it.metodoPagoId.toString(),
                    label = "${it.banco} - ${it.numeroCuenta} (${it.tipoMoneda})"
                )
            }
    }

    LaunchedEffect(monedaTengo, uiState.paymentMethods) {
        val filtered = uiState.paymentMethods.filter { it.tipoMoneda.equals(monedaTengo, ignoreCase = true) }
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
            },
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = RikkaIcons.ArrowLeft,
                            contentDescription = "Regresar",
                            tint = RikkaTheme.colors.onBackground
                        )
                    }

                    Text(
                        text = "Publicar",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }
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
                        variant = TextVariant.H2,
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

                // 2. Moneda que tienes
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

                // 3. Moneda que quieres recibir
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

                // 5. Método de Pago (cuenta en monedaTengo)
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = "Tu cuenta bancaria (de la moneda que tienes)")
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
                                text = "⚠️ No tienes cuentas registradas en $monedaTengo. Agrega una en tu perfil.",
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
                                        text = "Tipo de cambio actual: ${String.format(java.util.Locale.US, "%.6f", tipoCambioCalculado)}",
                                        variant = TextVariant.Small,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Tú entregas: ${String.format(java.util.Locale.US, "%,.2f", montoTengoCalculado)} $monedaTengo",
                                        variant = TextVariant.P,
                                        color = RikkaTheme.colors.onBackground
                                    )
                                    Text(
                                        text = "Tú recibes: ${String.format(java.util.Locale.US, "%,.2f", montoReciboCalculado)} $monedaRecibo",
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

        // Formulario de Creación de Oferta (ModalBottomSheet)
        if (showPublishSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPublishSheet = false },
                sheetState = publishSheetState,
                containerColor = RikkaTheme.colors.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Publicar oferta",
                        variant = TextVariant.H2,
                        color = RikkaTheme.colors.onBackground
                    )

                    // 1. Selector Compra/Venta
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

                    // 2. Moneda que tienes
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

                    // 3. Moneda que quieres recibir
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

                    // 4. Cantidad Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

                    // 5. Tu cuenta bancaria (de la moneda que tienes)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Label(text = "Tu cuenta bancaria (de la moneda que tienes)")
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
                                text = "⚠️ No tienes cuentas registradas en $monedaTengo. Agrega una en tu perfil.",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.destructive
                            )
                        }
                    }

                    // 6. Vista previa de conversión
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                        text = "Tipo de cambio actual: ${String.format(java.util.Locale.US, "%.6f", tipoCambioCalculado)}",
                                        variant = TextVariant.Small,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Tú entregas: ${String.format(java.util.Locale.US, "%,.2f", montoTengoCalculado)} $monedaTengo",
                                        variant = TextVariant.P,
                                        color = RikkaTheme.colors.onBackground
                                    )
                                    Text(
                                        text = "Tú recibes: ${String.format(java.util.Locale.US, "%,.2f", montoReciboCalculado)} $monedaRecibo",
                                        variant = TextVariant.P,
                                        color = RikkaTheme.colors.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 7. Botones Cancelar / Publicar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPublishSheet = false },
                            variant = ButtonVariant.Outline,
                            modifier = Modifier.weight(1f),
                            text = "Cancelar"
                        )
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
                                    showPublishSheet = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            text = "Publicar"
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun PartialOffersInfoCard() {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = RikkaTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "¿Cómo funcionan las ofertas?",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground
                    )
                }
                androidx.compose.material3.Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Al publicar una oferta, permites compras o ventas parciales según tus límites por transacción. El sistema descuenta el saldo automáticamente y ajusta inteligentemente el límite máximo si supera el monto restante disponible.",
                    variant = TextVariant.Small,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Example Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = RikkaTheme.colors.muted.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Ejemplo práctico (Oferta de 1000 USD | Min: 100 / Max: 400):",
                            variant = TextVariant.Small,
                            color = RikkaTheme.colors.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "•", variant = TextVariant.Small, color = Color.Gray)
                            Text(
                                text = "Usuario A toma 400 USD -> Quedan 600 USD de saldo (límite máximo sigue en 400 USD).",
                                variant = TextVariant.Small,
                                color = Color.Gray
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "•", variant = TextVariant.Small, color = Color.Gray)
                            Text(
                                text = "Usuario B toma 350 USD -> Quedan 250 USD de saldo.",
                                variant = TextVariant.Small,
                                color = Color.Gray
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "•", variant = TextVariant.Small, color = Color.Gray)
                            Text(
                                text = "Ajuste Inteligente -> El límite máximo se reduce automáticamente a 250 USD.",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.success
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "•", variant = TextVariant.Small, color = Color.Gray)
                            Text(
                                text = "Usuario C toma los 250 USD restantes -> Monto total llega a 0 y la oferta se completa.",
                                variant = TextVariant.Small,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
