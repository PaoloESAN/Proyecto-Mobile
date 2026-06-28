package com.paoloesan.proyectomobile.presentation.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.paoloesan.proyectomobile.presentation.navigation.Destination
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.checkbox.Checkbox
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
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

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
            navController.navigate(Destination.Marketplace.route) {
                popUpTo(Destination.Login.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = RikkaTheme.colors.background,
        snackbarHost = {
            ToastHost(
                hostState = toastState,
            )
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
            verticalArrangement = Arrangement.spacedBy(
                24.dp,
                alignment = Alignment.CenterVertically
            )
        ) {

            // Welcome Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "¡Hola, Bienvenido!",
                    variant = TextVariant.H1,
                    color = RikkaTheme.colors.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¿Listo para intercambiar?",
                    variant = TextVariant.Large,
                    color = Color.Gray
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Label(
                        text = "Correo electrónico",
                    )
                    Spacer(Modifier.height(4.dp))
                    Input(
                        value = uiState.correo,
                        onValueChange = viewModel::onCorreoChange,
                        placeholder = "usuario@email.com",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = Icons.Default.Email,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Column {
                    // Password Label
                    Label(
                        text = "Contraseña",
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Password Input with eye icon toggle overlay
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

                    Spacer(modifier = Modifier.height(4.dp))

                    // Olvidé mi contraseña justo debajo a la derecha de la contraseña, sin padding
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Olvidé mi contraseña",
                            variant = TextVariant.Small,
                            color = RikkaTheme.colors.primary,
                            modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                                navController.navigate(Destination.RecoverPassword.route)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Login Button
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
                        onClick = { viewModel.login(context, rememberMe = true) },
                        modifier = Modifier.fillMaxWidth(),
                        text = "Iniciar Sesion"
                    )
                }

                // Or With Divider
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 8.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(1.dp)
//                                .background(Color.LightGray)
//                        )
//                        Text(
//                            text = "O continúe con",
//                            modifier = Modifier.padding(horizontal = 8.dp),
//                            color = Color.Gray,
//                        )
//                        Box(
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(1.dp)
//                                .background(Color.LightGray)
//                        )
//                    }

                // Social Buttons
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        Button(
//                            onClick = {},
//                            variant = ButtonVariant.Outline,
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.Center
//                            ) {
//                                androidx.compose.material3.Icon(
//                                    imageVector = GitHubIcon,
//                                    contentDescription = "GitHub",
//                                    modifier = Modifier.size(18.dp),
//                                    tint = Color.Unspecified
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = "GitHub",
//                                    variant = TextVariant.P,
//                                )
//                            }
//                        }
//
//                        Button(
//                            onClick = {},
//                            variant = ButtonVariant.Outline,
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.Center
//                            ) {
//                                androidx.compose.material3.Icon(
//                                    imageVector = GitLabIcon,
//                                    contentDescription = "GitLab",
//                                    modifier = Modifier.size(18.dp),
//                                    tint = Color.Unspecified
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = "GitLab",
//                                    variant = TextVariant.P,
//                                )
//                            }
//                        }
//                    }

                // Botones de acceso rápido
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.onCorreoChange("julioprofe@email.com")
                            viewModel.onPasswordChange("123456")
                        },
                        variant = ButtonVariant.Outline,
                        modifier = Modifier.weight(1f),
                        text = "Usuario 1"
                    )
                    Button(
                        onClick = {
                            viewModel.onCorreoChange("juanjose@email.com")
                            viewModel.onPasswordChange("123456")
                        },
                        variant = ButtonVariant.Outline,
                        modifier = Modifier.weight(1f),
                        text = "Usuario 2"
                    )
                    Button(
                        onClick = {
                            viewModel.onCorreoChange("admin@exchange.com")
                            viewModel.onPasswordChange("admin123")
                        },
                        variant = ButtonVariant.Outline,
                        modifier = Modifier.weight(1f),
                        text = "Admin"
                    )
                }
            }


            // Sign Up Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¿No tienes una cuenta? ",
                    variant = TextVariant.P,
                    color = RikkaTheme.colors.onBackground,
                )
                Text(
                    text = "Regístrate",
                    variant = TextVariant.Large,
                    color = RikkaTheme.colors.primary,
                    modifier = Modifier.clickable {
                        navController.navigate(Destination.Registro.route)
                    }
                )
            }
        }

    }
}
