package com.paoloesan.proyectomobile.presentation.disputa

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paoloesan.proyectomobile.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisputaDetalleScreen(
    navController: NavController,
    viewModel: DisputaViewModel,
    disputaId: Int
) {
    val context = LocalContext.current
    val disputa = viewModel.getDisputaById(disputaId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Disputa") },
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

        if (disputa == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Disputa no encontrada")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {

                    Text(
                        text = "Datos de la Transacción",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Código: ${disputa.transaccion}")
                    Text("Monto: S/ 500.00") // Valor estático por el momento
                    Text("Estado: ${disputa.estado}")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Voucher enviado",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Image(
                        painter = painterResource(id = R.drawable.voucher_demo),
                        contentDescription = "Voucher",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Historial de Mensajes",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text("Comprador: Ya realicé el pago.")
                            Text("Vendedor: No visualizo el depósito.")
                            Text("Comprador: Adjunto voucher.")
                            Text("Vendedor: Continúo sin visualizarlo.")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.resolverDisputa(disputaId)
                            Toast.makeText(
                                context,
                                "Disputa resuelta correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aprobar al Comprador")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.resolverDisputa(disputaId)
                            Toast.makeText(
                                context,
                                "Disputa resuelta correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Aprobar al Vendedor")
                    }
                }
            }
        }
    }
}
