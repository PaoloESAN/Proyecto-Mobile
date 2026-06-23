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
    var currency by remember { mutableStateOf("USD") }
    var amount by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var minAmount by remember { mutableStateOf("") }
    var maxAmount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }

    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val currencyOptions = listOf(
        SelectOption("USD", "USD"),
        SelectOption("PEN", "PEN")
    )

    val paymentOptions = remember(uiState.paymentMethods, currency) {
        uiState.paymentMethods
            .filter { it.tipoMoneda.equals(currency, ignoreCase = true) }
            .map {
                SelectOption(
                    value = it.metodoPagoId.toString(),
                    label = "${it.banco} - ${it.numeroCuenta} (${it.tipoMoneda})"
                )
            }
    }

    LaunchedEffect(uiState.paymentMethods, currency) {
        val filtered = uiState.paymentMethods.filter { it.tipoMoneda.equals(currency, ignoreCase = true) }
        if (filtered.isNotEmpty()) {
            paymentMethod = filtered.first().metodoPagoId.toString()
        } else {
            paymentMethod = ""
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
            rate = ""
            minAmount = ""
            maxAmount = ""
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

    // Validaciones de publicación
    val isMinMaxError by remember {
        derivedStateOf {
            val min = minAmount.toDoubleOrNull()
            val max = maxAmount.toDoubleOrNull()
            minAmount.isNotBlank() && maxAmount.isNotBlank() &&
                    min != null && max != null && min > max
        }
    }

    val isMinZero by remember {
        derivedStateOf {
            minAmount.isNotBlank() && (minAmount.toDoubleOrNull() ?: -1.0) <= 0
        }
    }

    val isMaxZero by remember {
        derivedStateOf {
            maxAmount.isNotBlank() && (maxAmount.toDoubleOrNull() ?: -1.0) <= 0
        }
    }

    val canPublish by remember {
        derivedStateOf {
            val min = minAmount.toDoubleOrNull()
            val max = maxAmount.toDoubleOrNull()
            !uiState.isLoading &&
                    minAmount.isNotBlank() && maxAmount.isNotBlank() &&
                    amount.isNotBlank() && rate.isNotBlank() &&
                    (amount.toDoubleOrNull() ?: 0.0) > 0 &&
                    (rate.toDoubleOrNull() ?: 0.0) > 0 &&
                    min != null && min > 0 &&
                    max != null && max > 0 &&
                    !isMinMaxError &&
                    paymentMethod.isNotBlank()
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

                item {
                    PartialOffersInfoCard()
                }

                // Selector Compra/Venta
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = "Tipo de transacción")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { type = "Compra" },
                                variant = if (type == "Compra") ButtonVariant.Default else ButtonVariant.Outline,
                                modifier = Modifier.weight(1f),
                                text = "Compra"
                            )
                            Button(
                                onClick = { type = "Venta" },
                                variant = if (type == "Venta") ButtonVariant.Default else ButtonVariant.Outline,
                                modifier = Modifier.weight(1f),
                                text = "Venta"
                            )
                        }
                    }
                }

                // Moneda Select
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = "Moneda")
                        Select(
                            selectedValue = currency,
                            onValueChange = { currency = it },
                            options = currencyOptions,
                            placeholder = "Seleccione moneda...",
                            animation = PopupAnimation.Fade,
                            maxHeight = 300.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Monto Input
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = "Monto")
                        Input(
                            value = amount,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    amount = input
                                }
                            },
                            placeholder = "Ingrese monto",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = Icons.Default.AttachMoney,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Tipo de Cambio Input
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = "Tipo de cambio")
                        Input(
                            value = rate,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' }) {
                                    rate = input
                                }
                            },
                            placeholder = "3.75",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = Icons.Default.AttachMoney,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Monto Mínimo Input
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = "Monto mínimo")
                        Input(
                            value = minAmount,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    minAmount = input
                                }
                            },
                            placeholder = "Mínimo",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = Icons.Default.AttachMoney,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        if (isMinMaxError) {
                            Text(
                                text = "El monto mínimo no puede ser mayor al máximo",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.destructive
                            )
                        } else if (isMinZero) {
                            Text(
                                text = "El monto debe ser mayor a 0",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.destructive
                            )
                        }
                    }
                }

                // Monto Máximo Input
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = "Monto máximo")
                        Input(
                            value = maxAmount,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    maxAmount = input
                                }
                            },
                            placeholder = "Máximo",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = Icons.Default.AttachMoney,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        if (isMaxZero && !isMinMaxError) {
                            Text(
                                text = "El monto debe ser mayor a 0",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.destructive
                            )
                        }
                    }
                }

                // Método de Pago Select
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Label(text = "Método de Pago")
                        Select(
                            selectedValue = paymentMethod,
                            onValueChange = { paymentMethod = it },
                            options = paymentOptions,
                            placeholder = "Seleccione método de pago...",
                            animation = PopupAnimation.Fade,
                            maxHeight = 300.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Botón Publicar
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        enabled = canPublish,
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            val rt = rate.toDoubleOrNull() ?: 0.0
                            val min = minAmount.toDoubleOrNull() ?: 0.0
                            val max = maxAmount.toDoubleOrNull() ?: 0.0
                            val selectedMethodId = paymentMethod.toIntOrNull()

                            if (selectedMethodId != null) {
                                viewModel.publishOffer(
                                    metodoPagoId = selectedMethodId,
                                    tipoOperacion = type,
                                    moneda = currency,
                                    montoTotal = amt,
                                    montoMinimo = min,
                                    montoMaximo = max,
                                    tipoCambio = rt
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        text = "Publicar"
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

                    // Selector Compra/Venta
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Label(text = "Tipo de transacción")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { type = "Compra" },
                                variant = if (type == "Compra") ButtonVariant.Default else ButtonVariant.Outline,
                                modifier = Modifier.weight(1f),
                                text = "Compra"
                            )
                            Button(
                                onClick = { type = "Venta" },
                                variant = if (type == "Venta") ButtonVariant.Default else ButtonVariant.Outline,
                                modifier = Modifier.weight(1f),
                                text = "Venta"
                            )
                        }
                    }

                    // Moneda Select
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Label(text = "Moneda")
                        Select(
                            selectedValue = currency,
                            onValueChange = { currency = it },
                            options = currencyOptions,
                            placeholder = "Seleccione moneda...",
                            animation = PopupAnimation.Fade,
                            maxHeight = 300.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Monto Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Label(text = "Monto")
                        Input(
                            value = amount,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    amount = input
                                }
                            },
                            placeholder = "Ingrese monto",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = Icons.Default.AttachMoney,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Tipo de Cambio Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Label(text = "Tipo de cambio")
                        Input(
                            value = rate,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' }) {
                                    rate = input
                                }
                            },
                            placeholder = "3.75",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = Icons.Default.AttachMoney,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Monto Mínimo Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Label(text = "Monto mínimo")
                        Input(
                            value = minAmount,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    minAmount = input
                                }
                            },
                            placeholder = "Mínimo",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = Icons.Default.AttachMoney,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        if (isMinMaxError) {
                            Text(
                                text = "El monto mínimo no puede ser mayor al máximo",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.destructive
                            )
                        } else if (isMinZero) {
                            Text(
                                text = "El monto debe ser mayor a 0",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.destructive
                            )
                        }
                    }

                    // Monto Máximo Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Label(text = "Monto máximo")
                        Input(
                            value = maxAmount,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    maxAmount = input
                                }
                            },
                            placeholder = "Máximo",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = Icons.Default.AttachMoney,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        if (isMaxZero && !isMinMaxError) {
                            Text(
                                text = "El monto debe ser mayor a 0",
                                variant = TextVariant.Small,
                                color = RikkaTheme.colors.destructive
                            )
                        }
                    }

                    // Método de Pago Select
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Label(text = "Método de Pago")
                        Select(
                            selectedValue = paymentMethod,
                            onValueChange = { paymentMethod = it },
                            options = paymentOptions,
                            placeholder = "Seleccione método de pago...",
                            animation = PopupAnimation.Fade,
                            maxHeight = 300.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botones Cancelar / Publicar
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
                                val amt = amount.toDoubleOrNull() ?: 0.0
                                val rt = rate.toDoubleOrNull() ?: 0.0
                                val min = minAmount.toDoubleOrNull() ?: 0.0
                                val max = maxAmount.toDoubleOrNull() ?: 0.0
                                val selectedMethodId = paymentMethod.toIntOrNull()

                                if (selectedMethodId != null) {
                                    viewModel.publishOffer(
                                        metodoPagoId = selectedMethodId,
                                        tipoOperacion = type,
                                        moneda = currency,
                                        montoTotal = amt,
                                        montoMinimo = min,
                                        montoMaximo = max,
                                        tipoCambio = rt
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
