package com.paoloesan.proyectomobile.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import android.webkit.WebView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.viewinterop.AndroidView
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.paoloesan.proyectomobile.presentation.navigation.Destination
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.checkbox.Checkbox
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.input.Input
import zed.rainxch.rikkaui.components.ui.label.Label
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    navController: NavController,
    viewModel: RegistroViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }

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
            scope.launch {
                toastState.show(
                    message = "Cuenta creada correctamente",
                    variant = ToastVariant.Success
                )
            }
            delay(800)
            viewModel.resetSuccess()
            navController.popBackStack()
        }
    }

    // Gradient / Background Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RikkaTheme.colors.background)
    ) {
        Scaffold(
            containerColor = RikkaTheme.colors.background,
            snackbarHost = {
                ToastHost(
                    hostState = toastState,
                )
            },
            topBar = {
                // Transparent Top Bar with White Icons
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
                            contentDescription = "Volver",
                            tint = RikkaTheme.colors.onBackground
                        )
                    }

                    Text(
                        text = "Crear Cuenta",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        ) { innerPadding ->
            val focusManager = LocalFocusManager.current
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // Welcome Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Crea tu cuenta",
                        variant = TextVariant.H1,
                        color = RikkaTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Para empezar a intercambiar",
                        variant = TextVariant.Large,
                        color = Color.Gray
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        // Nombres Label & Input
                        Label("Nombres")
                        Spacer(Modifier.height(4.dp))
                        Input(
                            value = uiState.nombres,
                            onValueChange = viewModel::onNombresChange,
                            placeholder = "Escribe tus nombres",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            leadingIcon = Icons.Default.Person,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading
                        )
                    }

                    Column {
                        // Apellidos Label & Input
                        Label("Apellidos")
                        Spacer(Modifier.height(4.dp))
                        Input(
                            value = uiState.apellidos,
                            onValueChange = viewModel::onApellidosChange,
                            placeholder = "Escribe tus apellidos",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            leadingIcon = Icons.Default.Person,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading
                        )
                    }

                    Column {
                        // Correo Label & Input
                        Label("Correo electrónico")
                        Spacer(Modifier.height(4.dp))
                        Input(
                            value = uiState.correo,
                            onValueChange = viewModel::onCorreoChange,
                            placeholder = "ejemplo@correo.com",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = Icons.Default.Email,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading
                        )
                    }

                    Column {
                        // Contraseña Label & Input
                        Label("Contraseña")
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Input(
                                value = uiState.password,
                                onValueChange = viewModel::onPasswordChange,
                                placeholder = "Por favor escribe tu contraseña",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                leadingIcon = Icons.Default.Lock,
                                trailingIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !uiState.isLoading
                            )
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable(
                                        enabled = !uiState.isLoading,
                                        onClick = { passwordVisible = !passwordVisible }
                                    )
                            )
                        }
                    }

                    Column {
                        // Confirmar Contraseña Label & Input
                        Label("Confirmar contraseña")
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Input(
                                value = uiState.confirmarPassword,
                                onValueChange = viewModel::onConfirmarPasswordChange,
                                placeholder = "Repite tu contraseña",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                leadingIcon = Icons.Default.Lock,
                                trailingIcon = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !uiState.isLoading
                            )
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable(
                                        enabled = !uiState.isLoading,
                                        onClick = {
                                            confirmPasswordVisible = !confirmPasswordVisible
                                        }
                                    )
                            )
                        }
                    }

                    // Términos y condiciones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.acceptTerms,
                            onCheckedChange = viewModel::onAcceptTermsChange,
                            label = "Acepto los "
                        )
                        Text(
                            text = "términos y condiciones",
                            variant = TextVariant.P,
                            color = RikkaTheme.colors.primary,
                            modifier = Modifier.clickable {
                                showTerms = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Registrarse Button
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = RikkaTheme.colors.primary
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.register() },
                            modifier = Modifier.fillMaxWidth(),
                            text = "Registrarse"
                        )
                    }
                }

                // Login Link Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "¿Ya tienes cuenta? ",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground,
                    )
                    Text(
                        text = "Iniciar sesión",
                        variant = TextVariant.Large,
                        color = RikkaTheme.colors.primary,
                        modifier = Modifier.clickable {
                            navController.navigate(Destination.Login.route)
                        }
                    )
                }
            }
        }
    }

    if (showTerms) {
        AlertDialog(
            onDismissRequest = { showTerms = false },
            title = {
                Text(
                    text = "Términos y condiciones",
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.onBackground
                )
            },
            text = {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            loadUrl("https://www.privacypolicies.com/live/4ad40d9d-c177-417a-b24b-660323acd99e")
                        }
                    },
                    modifier = Modifier.height(400.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showTerms = false },
                    text = "Cerrar"
                )
            },
            containerColor = RikkaTheme.colors.background
        )
    }
}
