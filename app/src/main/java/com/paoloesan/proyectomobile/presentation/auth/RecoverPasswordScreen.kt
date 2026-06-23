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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paoloesan.proyectomobile.data.Supabase
import io.github.jan.supabase.auth.auth
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
fun RecoverPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

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
                        text = "Recuperar Contraseña",
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
                        text = "Recupera tu contraseña",
                        variant = TextVariant.H1,
                        color = RikkaTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ingresa tu correo electrónico registrado y te enviaremos un enlace para restablecer tu contraseña.",
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
                    // Email Label
                    Text(
                        text = "Correo electrónico",
                        variant = TextVariant.P,
                        color = RikkaTheme.colors.onBackground,
                    )

                    Input(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "ejemplo@correo.com",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = Icons.Default.Email,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isSending
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isSending) {
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
                                    val isValidFormat =
                                        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                                    if (email.isBlank() || !isValidFormat) {
                                        scope.launch {
                                            toastState.show(
                                                message = "Por favor ingresa un correo electrónico válido",
                                                variant = ToastVariant.Destructive
                                            )
                                        }
                                    } else {
                                        scope.launch {
                                            isSending = true
                                            try {
                                                Supabase.client.auth.resetPasswordForEmail(
                                                    email = email.trim(),
                                                    redirectUrl = "interya://recovery"
                                                )
                                                toastState.show(
                                                    message = "Se envió un enlace de recuperación a tu correo",
                                                    variant = ToastVariant.Success
                                                )
                                            } catch (e: Exception) {
                                                toastState.show(
                                                    message = "Error: ${e.localizedMessage ?: "No se pudo enviar el enlace"}",
                                                    variant = ToastVariant.Destructive
                                                )
                                            } finally {
                                                isSending = false
                                            }
                                        }
                                    }
                            },
                            enabled = email.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            text = "Enviar enlace"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón auxiliar para ir a la pantalla de Reset Password para poder testearla
                    Button(
                        onClick = {
                            navController.navigate("reset_password")
                        },
                        variant = ButtonVariant.Outline,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.LockReset,
                                contentDescription = null,
                                tint = RikkaTheme.colors.onBackground
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ir a Restablecer Contraseña (Demo)",
                                variant = TextVariant.P,
                                color = RikkaTheme.colors.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}
