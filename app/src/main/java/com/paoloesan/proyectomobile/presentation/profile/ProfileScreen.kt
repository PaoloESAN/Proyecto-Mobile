package com.paoloesan.proyectomobile.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

data class Alert(
    val currency: String,
    val rate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val alertSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showAlertSheet by remember { mutableStateOf(false) }
    var savedAlerts by remember { mutableStateOf(listOf<Alert>(
        Alert("USD", "3.85"),
        Alert("PEN", "3.72")
    )) }
    val focusManager = LocalFocusManager.current
    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadProfile(context)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch {
                toastState.show(
                    message = message,
                    variant = ToastVariant.Destructive
                )
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.consumeSuccess()
            scope.launch {
                toastState.show(
                    message = "Cambios guardados con exito",
                    variant = ToastVariant.Success
                )
            }
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
                        text = "Mi Perfil",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Button(
                        onClick = { navController.navigate("settings") },
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuracion",
                            tint = RikkaTheme.colors.onBackground
                        )
                    }
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
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Header / Avatar Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(
                                    color = RikkaTheme.colors.primary.copy(alpha = 0.12f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = RikkaTheme.colors.primary.copy(alpha = 0.5f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val firstLetter = uiState.nombres.firstOrNull()?.toString() ?: ""
                            val secondLetter = uiState.apellidos.firstOrNull()?.toString() ?: ""
                            val initials = (firstLetter + secondLetter).uppercase()
                            Text(
                                text = initials,
                                color = RikkaTheme.colors.primary,
                                variant = TextVariant.H1
                            )
                        }

                        Text(
                            text = "${uiState.nombres} ${uiState.apellidos}",
                            variant = TextVariant.H2,
                            color = RikkaTheme.colors.onBackground
                        )

                        if (uiState.isVerified) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    for (i in 1..5) {
                                        val starColor =
                                            if (i <= 4) Color(0xFFFFB74D) else Color.Gray.copy(alpha = 0.3f)
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = starColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "4.5",
                                        variant = TextVariant.Large,
                                        color = RikkaTheme.colors.onBackground
                                    )
                                }
                                Text(
                                    text = "120 reseñas",
                                    variant = TextVariant.Small,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = RikkaTheme.colors.warning.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Sin verificar",
                                    variant = TextVariant.Small,
                                    color = RikkaTheme.colors.warning
                                )
                            }
                        }

                        if (!uiState.isVerified) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = RikkaTheme.colors.warning,
                                        shape = RoundedCornerShape(999.dp)
                                    )
                                    .clickable { navController.navigate("identity_verification") }
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Verificar cuenta",
                                    variant = TextVariant.Small,
                                    color = RikkaTheme.colors.warning
                                )
                            }
                        }
                    }
                }

                // Bank Accounts Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mis Cuentas Bancarias",
                            variant = TextVariant.Large,
                            color = RikkaTheme.colors.onBackground
                        )

                        Button(
                            onClick = { showBottomSheet = true },
                            variant = ButtonVariant.Outline,
                            size = ButtonSize.Sm
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = RikkaTheme.colors.onBackground
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Agregar",
                                    variant = TextVariant.Small,
                                    color = RikkaTheme.colors.onBackground
                                )
                            }
                        }
                    }
                }

                if (uiState.cuentas.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No tienes cuentas registradas",
                                variant = TextVariant.P,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(uiState.cuentas, key = { it.id }) { cuenta ->
                        BankAccountCard(cuenta)
                    }
                }

                // Alertas de Tipo de Cambio Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alertas de Tipo de Cambio",
                            variant = TextVariant.Large,
                            color = RikkaTheme.colors.onBackground
                        )

                        Button(
                            onClick = { showAlertSheet = true },
                            variant = ButtonVariant.Outline,
                            size = ButtonSize.Sm
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = RikkaTheme.colors.onBackground
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Agregar",
                                    variant = TextVariant.Small,
                                    color = RikkaTheme.colors.onBackground
                                )
                            }
                        }
                    }
                }

                if (savedAlerts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No tienes alertas configuradas",
                                variant = TextVariant.P,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(savedAlerts) { alert ->
                        AlertCard(alert, onDelete = {
                            savedAlerts = savedAlerts.filter { it != alert }
                            scope.launch {
                                toastState.show("Alerta eliminada", ToastVariant.Success)
                            }
                        })
                    }
                }


            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = RikkaTheme.colors.background
        ) {
            AddBankAccountSheet(
                onCancel = { showBottomSheet = false },
                onConfirm = { cuenta ->
                    viewModel.addCuenta(cuenta)
                    showBottomSheet = false
                }
            )
        }
    }

    if (showAlertSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAlertSheet = false },
            sheetState = alertSheetState,
            containerColor = RikkaTheme.colors.background
        ) {
            AddAlertSheet(
                onCancel = { showAlertSheet = false },
                onConfirm = { alert ->
                    savedAlerts = savedAlerts + alert
                    showAlertSheet = false
                    scope.launch {
                        toastState.show("Alerta registrada correctamente", ToastVariant.Success)
                    }
                }
            )
        }
    }
}

@Composable
private fun BankAccountCard(cuenta: BankAccount) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = RikkaTheme.colors.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = RikkaTheme.colors.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cuenta.banco,
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.onBackground
                )
                Text(
                    text = "N. ${cuenta.numeroCuenta}",
                    variant = TextVariant.Small,
                    color = Color.Gray
                )
                Text(
                    text = cuenta.titular,
                    variant = TextVariant.Small,
                    color = Color.Gray
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        color = RikkaTheme.colors.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = cuenta.moneda,
                    variant = TextVariant.Small,
                    color = RikkaTheme.colors.primary
                )
            }
        }
    }
}

@Composable
private fun AddBankAccountSheet(
    onCancel: () -> Unit,
    onConfirm: (BankAccount) -> Unit
) {
    val monedas = listOf(
        SelectOption("USD", "USD"),
        SelectOption("EUR", "EUR"),
        SelectOption("GBP", "GBP"),
        SelectOption("MXN", "MXN"),
        SelectOption("PEN", "PEN"),
        SelectOption("JPY", "JPY")
    )

    var banco by remember { mutableStateOf("") }
    var monedaSeleccionada by remember { mutableStateOf(monedas.first().value) }

    var numeroCuenta by remember { mutableStateOf("") }
    var titular by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Agregar cuenta bancaria",
            variant = TextVariant.H2,
            color = RikkaTheme.colors.onBackground
        )

        // Banco Input
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Label(text = "Banco")
            Input(
                value = banco,
                onValueChange = { banco = it },
                placeholder = "Ej: BCP, Interbank, Yape...",
                leadingIcon = Icons.Default.AccountBalance,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // Numero de Cuenta Input
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Label(text = "Numero de cuenta")
            Input(
                value = numeroCuenta,
                onValueChange = { value ->
                    if (value.isEmpty() || value.all { it.isDigit() }) {
                        numeroCuenta = value
                    }
                },
                placeholder = "1234567890",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = Icons.Default.AccountBalance,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // Titular Input
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Label(text = "Nombre del titular")
            Input(
                value = titular,
                onValueChange = { value ->
                    if (value.isEmpty() || value.all { it.isLetter() || it == ' ' }) {
                        titular = value
                    }
                },
                placeholder = "Nombre del titular",
                leadingIcon = Icons.Default.Person,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // Moneda Dropdown
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Label(text = "Moneda")
            Select(
                selectedValue = monedaSeleccionada,
                onValueChange = { monedaSeleccionada = it },
                options = monedas,
                placeholder = "Seleccione una moneda...",
                animation = PopupAnimation.Fade,
                maxHeight = 300.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        errorMessage?.let {
            Text(
                text = it,
                variant = TextVariant.Small,
                color = RikkaTheme.colors.destructive
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancel,
                variant = ButtonVariant.Outline,
                modifier = Modifier.weight(1f),
                text = "Cancelar"
            )
            Button(
                onClick = {
                    when {
                        banco.isBlank() -> errorMessage = "Ingrese el nombre del banco"
                        numeroCuenta.isBlank() -> errorMessage = "Ingrese el numero de cuenta"
                        numeroCuenta.length < 10 -> errorMessage =
                            "El numero de cuenta debe tener al menos 10 digitos"

                        titular.isBlank() -> errorMessage = "Ingrese el nombre del titular"
                        monedaSeleccionada.isBlank() -> errorMessage = "Seleccione una moneda"
                        else -> {
                            errorMessage = null
                            onConfirm(
                                BankAccount(
                                    banco = banco,
                                    numeroCuenta = numeroCuenta,
                                    titular = titular,
                                    moneda = monedaSeleccionada
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                text = "Guardar"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AlertCard(alert: Alert, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = RikkaTheme.colors.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = RikkaTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = alert.currency,
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.onBackground
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "T.C.: ${alert.rate}",
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.primary
                )
                
                Button(
                    onClick = onDelete,
                    variant = ButtonVariant.Ghost,
                    size = ButtonSize.Icon
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = RikkaTheme.colors.destructive,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddAlertSheet(
    onCancel: () -> Unit,
    onConfirm: (Alert) -> Unit
) {
    val currencyOptions = listOf(
        SelectOption("USD", "USD"),
        SelectOption("PEN", "PEN")
    )

    var currency by remember { mutableStateOf("USD") }
    var rate by remember { mutableStateOf("") }
    var rateError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Configurar alerta de tipo de cambio",
            variant = TextVariant.H2,
            color = RikkaTheme.colors.onBackground
        )

        // Moneda Dropdown
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Label(text = "Moneda")
            Select(
                selectedValue = currency,
                onValueChange = { currency = it },
                options = currencyOptions,
                placeholder = "Seleccione una moneda...",
                animation = PopupAnimation.Fade,
                maxHeight = 300.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Tipo de Cambio Input
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Label(text = "Tipo de cambio")
            Input(
                value = rate,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == '.' }) {
                        rate = input
                        rateError = false
                    }
                },
                placeholder = "3.75",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = Icons.Default.AttachMoney,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (rateError) {
                Text(
                    text = "Ingrese un tipo de cambio válido",
                    variant = TextVariant.Small,
                    color = RikkaTheme.colors.destructive
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancel,
                variant = ButtonVariant.Outline,
                modifier = Modifier.weight(1f),
                text = "Cancelar"
            )
            Button(
                onClick = {
                    val rateVal = rate.toDoubleOrNull()
                    if (rate.isBlank() || rateVal == null || rateVal <= 0) {
                        rateError = true
                    } else {
                        rateError = false
                        onConfirm(Alert(currency = currency, rate = rate))
                    }
                },
                modifier = Modifier.weight(1f),
                text = "Guardar"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
