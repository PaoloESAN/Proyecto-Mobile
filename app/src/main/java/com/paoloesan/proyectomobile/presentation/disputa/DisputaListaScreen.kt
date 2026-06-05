package com.paoloesan.proyectomobile.presentation.disputa

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisputaListaScreen(
    navController: NavController,
    viewModel: DisputaViewModel
) {
    val disputas by viewModel.disputas.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disputas") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
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
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(disputas, key = { it.id }) { disputa ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Estado: ${disputa.estado}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Comprador: ${disputa.comprador}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Vendedor: ${disputa.vendedor}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Transacción: ${disputa.transaccion}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                navController.navigate("detalle_disputa/${disputa.id}")
                            }
                        ) {
                            Text("Ver detalle")
                        }
                    }
                }
            }
        }
    }
}
