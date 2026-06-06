package com.paoloesan.proyectomobile.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.paoloesan.proyectomobile.R
import com.paoloesan.proyectomobile.data.local.SessionManager
import com.paoloesan.proyectomobile.presentation.navigation.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
            viewModel.consumeSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.nombres,
                    onValueChange = viewModel::onNombresChange,
                    label = { Text("Nombres") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.apellidos,
                    onValueChange = viewModel::onApellidosChange,
                    label = { Text("Apellidos") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.telefono,
                    onValueChange = viewModel::onTelefonoChange,
                    label = { Text("Teléfono") },
                    placeholder = { Text("987654321") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Text(
                    text = "Cuentas bancarias registradas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState.cuentas.isEmpty()) {
                item {
                    Text(
                        text = "No tienes cuentas registradas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(uiState.cuentas, key = { it.id }) { cuenta ->
                    BankAccountCard(cuenta)
                }
            }

            item {
                OutlinedButton(
                    onClick = { showBottomSheet = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar cuenta bancaria")
                }
            }

            item {
                Button(
                    onClick = { viewModel.saveChanges() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar cambios")
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Más opciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ProfileMenuItem(
                    icon = Icons.Default.VerifiedUser,
                    title = "Verificación de identidad",
                    onClick = { navController.navigate("identity_verification") }
                )
            }

            item {
                ProfileMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Alertas de tipo de cambio",
                    onClick = { navController.navigate("alerts") }
                )
            }

            item {
                ProfileMenuItem(
                    icon = Icons.Default.Gavel,
                    title = "Mis disputas",
                    onClick = { navController.navigate("disputas") }
                )
            }

            item {
                ProfileMenuItem(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Administración de usuarios",
                    onClick = { navController.navigate("admin_users") }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                val context = LocalContext.current
                OutlinedButton(
                    onClick = {
                        SessionManager.clearToken(context)
                        navController.navigate(Destination.Login.route) {
                            popUpTo(Destination.Marketplace.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar sesión")
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
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
}

@Composable
private fun BankAccountCard(cuenta: BankAccount) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cuenta.banco,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "N° ${cuenta.numeroCuenta}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = cuenta.titular,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = cuenta.moneda,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBankAccountSheet(
    onCancel: () -> Unit,
    onConfirm: (BankAccount) -> Unit
) {
    val bancos = listOf("BCP", "Interbank", "Yape")
    val monedas = listOf("PEN", "USD")

    var bancoSeleccionado by remember { mutableStateOf(bancos.first()) }
    var bancoExpanded by remember { mutableStateOf(false) }

    var monedaSeleccionada by remember { mutableStateOf(monedas.first()) }
    var monedaExpanded by remember { mutableStateOf(false) }

    var numeroCuenta by remember { mutableStateOf("") }
    var titular by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Agregar cuenta bancaria",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        ExposedDropdownMenuBox(
            expanded = bancoExpanded,
            onExpandedChange = { bancoExpanded = it }
        ) {
            OutlinedTextField(
                value = bancoSeleccionado,
                onValueChange = {},
                readOnly = true,
                label = { Text("Banco") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = bancoExpanded)
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = bancoExpanded,
                onDismissRequest = { bancoExpanded = false }
            ) {
                bancos.forEach { banco ->
                    DropdownMenuItem(
                        text = { Text(banco) },
                        onClick = {
                            bancoSeleccionado = banco
                            bancoExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = numeroCuenta,
            onValueChange = { value ->
                if (value.isEmpty() || value.all { it.isDigit() }) {
                    numeroCuenta = value
                }
            },
            label = { Text("Número de cuenta") },
            placeholder = { Text("1234567890") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = titular,
            onValueChange = { value ->
                if (value.isEmpty() || value.all { it.isLetter() || it == ' ' }) {
                    titular = value
                }
            },
            label = { Text("Nombre del titular") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenuBox(
            expanded = monedaExpanded,
            onExpandedChange = { monedaExpanded = it }
        ) {
            OutlinedTextField(
                value = monedaSeleccionada,
                onValueChange = {},
                readOnly = true,
                label = { Text("Moneda") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = monedaExpanded)
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = monedaExpanded,
                onDismissRequest = { monedaExpanded = false }
            ) {
                monedas.forEach { moneda ->
                    DropdownMenuItem(
                        text = { Text(moneda) },
                        onClick = {
                            monedaSeleccionada = moneda
                            monedaExpanded = false
                        }
                    )
                }
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    when {
                        bancoSeleccionado.isBlank() -> errorMessage = "Seleccione un banco"
                        numeroCuenta.isBlank() -> errorMessage = "Ingrese el número de cuenta"
                        numeroCuenta.length < 10 -> errorMessage = "El número de cuenta debe tener al menos 10 dígitos"
                        titular.isBlank() -> errorMessage = "Ingrese el nombre del titular"
                        monedaSeleccionada.isBlank() -> errorMessage = "Seleccione una moneda"
                        else -> {
                            errorMessage = null
                            onConfirm(
                                BankAccount(
                                    banco = bancoSeleccionado,
                                    numeroCuenta = numeroCuenta,
                                    titular = titular,
                                    moneda = monedaSeleccionada
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar")
            }
        }

        Spacer(modifier = Modifier.size(8.dp))
    }
}
