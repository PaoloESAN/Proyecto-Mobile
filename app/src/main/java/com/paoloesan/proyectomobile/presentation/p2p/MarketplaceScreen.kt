package com.paoloesan.proyectomobile.presentation.p2p

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    navController: NavController,
    viewModel: MarketplaceViewModel = viewModel()
) {
    val offers by viewModel.filteredOffers.collectAsState()
    val activeFilters by viewModel.filters.collectAsState()

    // Temporary local states for filter controls (applied only when clicking "Aplicar filtros")
    var tempCurrency by remember { mutableStateOf(activeFilters.currency) }
    var tempType by remember { mutableStateOf(activeFilters.type) }
    var tempAmountStr by remember { mutableStateOf(activeFilters.amount?.toString() ?: "") }
    var tempPaymentMethod by remember { mutableStateOf(activeFilters.paymentMethod) }

    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var paymentDropdownExpanded by remember { mutableStateOf(false) }

    val currencies = listOf("TODOS", "USD", "PEN")
    val paymentMethods = listOf("TODOS", "BCP", "Yape", "Interbank")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mercado P2P") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("matches") }) {
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Coincidencias")
                    }
                    IconButton(onClick = { navController.navigate("alerts") }) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alertas")
                    }
                    IconButton(onClick = { navController.navigate("publish_offer") }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Publicar oferta")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Sección de Filtros
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Filtros de búsqueda",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Filtro por Moneda y Tipo de Operación en una fila
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Moneda Dropdown
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Moneda", style = MaterialTheme.typography.bodySmall)
                            OutlinedButton(
                                onClick = { currencyDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tempCurrency)
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = currencyDropdownExpanded,
                                onDismissRequest = { currencyDropdownExpanded = false }
                            ) {
                                currencies.forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text(curr) },
                                        onClick = {
                                            tempCurrency = curr
                                            currencyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Tipo de operación Dropdown
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Operación", style = MaterialTheme.typography.bodySmall)
                            var typeDropdownExpanded by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { typeDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tempType)
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = typeDropdownExpanded,
                                onDismissRequest = { typeDropdownExpanded = false }
                            ) {
                                listOf("TODOS", "Compra", "Venta").forEach { typeOp ->
                                    DropdownMenuItem(
                                        text = { Text(typeOp) },
                                        onClick = {
                                            tempType = typeOp
                                            typeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Filtro por Monto y Método de Pago
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Monto input
                        OutlinedTextField(
                            value = tempAmountStr,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    tempAmountStr = input
                                }
                            },
                            label = { Text("Monto") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        // Método de Pago Dropdown
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Método de pago", style = MaterialTheme.typography.bodySmall)
                            OutlinedButton(
                                onClick = { paymentDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tempPaymentMethod)
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = paymentDropdownExpanded,
                                onDismissRequest = { paymentDropdownExpanded = false }
                            ) {
                                paymentMethods.forEach { method ->
                                    DropdownMenuItem(
                                        text = { Text(method) },
                                        onClick = {
                                            tempPaymentMethod = method
                                            paymentDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Botón Aplicar Filtros
                    Button(
                        onClick = {
                            val amountVal = tempAmountStr.toDoubleOrNull()
                            viewModel.applyFilters(
                                currency = tempCurrency,
                                type = tempType,
                                amount = amountVal,
                                paymentMethod = tempPaymentMethod
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.FilterList, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aplicar filtros")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Resultado de Búsqueda
            if (offers.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No se encontraron ofertas disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(offers, key = { it.id }) { offer ->
                        OfferCard(
                            offer = offer,
                            onSelect = {
                                navController.navigate("offerDetail/1")
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferCard(
    offer: P2POffer,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = offer.username,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = if (offer.type == "Compra") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = offer.type,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (offer.type == "Compra") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${offer.currency} ${offer.amount}",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.width(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "T.C.: ${offer.rate}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Límites: Min ${offer.minLimit} / Max ${offer.maxLimit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.width(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = offer.paymentMethod,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
