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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zed.rainxch.rikkaui.components.ui.button.Button
import zed.rainxch.rikkaui.components.ui.button.ButtonSize
import zed.rainxch.rikkaui.components.ui.button.ButtonVariant
import zed.rainxch.rikkaui.components.ui.icon.RikkaIcons
import zed.rainxch.rikkaui.components.ui.input.Input
import zed.rainxch.rikkaui.components.ui.text.Text
import zed.rainxch.rikkaui.components.ui.text.TextVariant
import zed.rainxch.rikkaui.components.ui.toast.ToastHost
import zed.rainxch.rikkaui.components.ui.toast.ToastVariant
import zed.rainxch.rikkaui.components.ui.toast.rememberToastHostState
import zed.rainxch.rikkaui.foundation.RikkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(navController: NavController) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isResetting by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val toastState = rememberToastHostState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

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
                        text = "Restablecer Contraseña",
                        color = RikkaTheme.colors.onBackground,
                        variant = TextVariant.Large,
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        ) { innerPadding ->
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
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Crea una nueva contraseña",
                        variant = TextVariant.H1,
                        color = RikkaTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ingresa tu nueva contraseña para poder acceder a tu cuenta.",
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
                    // Password Label & Input with eye toggle
                    Text(
                        text = "Nueva Contraseña",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground,
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Input(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Escribe tu nueva contraseña",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = Icons.Default.Lock,
                            trailingIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isResetting
                        )
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(
                                    enabled = !isResetting,
                                    onClick = { passwordVisible = !passwordVisible }
                                )
                        )
                    }

                    // Confirm Password Label & Input with eye toggle
                    Text(
                        text = "Confirmar Nueva Contraseña",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground,
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Input(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = "Repite tu nueva contraseña",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = Icons.Default.Lock,
                            trailingIcon = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isResetting
                        )
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(
                                    enabled = !isResetting,
                                    onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isResetting) {
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
                            onClick = {
                                val hasLetter = password.any { it.isLetter() }
                                val hasDigit = password.any { it.isDigit() }

                                if (password.isBlank() || confirmPassword.isBlank()) {
                                    scope.launch {
                                        toastState.show(
                                            message = "Por favor completa ambos campos",
                                            variant = ToastVariant.Destructive
                                        )
                                    }
                                } else if (password.length < 8) {
                                    scope.launch {
                                        toastState.show(
                                            message = "La contraseña debe tener al menos 8 caracteres",
                                            variant = ToastVariant.Destructive
                                        )
                                    }
                                } else if (!hasLetter || !hasDigit) {
                                    scope.launch {
                                        toastState.show(
                                            message = "La contraseña debe contener letras y números",
                                            variant = ToastVariant.Destructive
                                        )
                                    }
                                } else if (password != confirmPassword) {
                                    scope.launch {
                                        toastState.show(
                                            message = "Las contraseñas no coinciden",
                                            variant = ToastVariant.Destructive
                                        )
                                    }
                                } else {
                                    scope.launch {
                                        isResetting = true
                                        toastState.show(
                                            message = "¡Contraseña restablecida con éxito!",
                                            variant = ToastVariant.Success
                                        )
                                        delay(1500)
                                        isResetting = false
                                        navController.popBackStack()
                                    }
                                }
                            },
                            enabled = !isResetting,
                            modifier = Modifier.fillMaxWidth(),
                            text = "Restablecer Contraseña"
                        )
                    }
                }
            }
        }
    }
}
