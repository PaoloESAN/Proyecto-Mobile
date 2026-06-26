package com.paoloesan.proyectomobile.presentation.verification

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.paoloesan.proyectomobile.data.local.SessionManager
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * IdentityVerificationScreen
 *
 * Pantalla de verificación de identidad (DNI)
 * Relacionada con funcionalidad IdentityVerificationScreen #44
 *
 * Permite al usuario seleccionar la imagen frontal y posterior de su DNI,
 * validar el formato y tamaño, y simular el envío para verificación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityVerificationScreen(
    onBack: () -> Unit,
    onVerificationComplete: () -> Unit = {}
) {
    var frontalUri by remember { mutableStateOf<Uri?>(null) }
    var posteriorUri by remember { mutableStateOf<Uri?>(null) }
    var frontalError by remember { mutableStateOf("") }
    var posteriorError by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val frontalPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val result = validateDniImage(context, it)
            if (result.isValid) {
                frontalUri = it
                frontalError = ""
            } else {
                frontalUri = null
                frontalError = result.errorMessage
            }
        }
    }

    val posteriorPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val result = validateDniImage(context, it)
            if (result.isValid) {
                posteriorUri = it
                posteriorError = ""
            } else {
                posteriorUri = null
                posteriorError = result.errorMessage
            }
        }
    }

    LaunchedEffect(isUploading) {
        if (isUploading) {
            delay(2000)
            SessionManager.saveVerified(context, true)
            isUploading = false
            showSuccess = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Verificación de DNI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                InstructionText()

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                DniImageCard(
                    title = "DNI Frontal",
                    imageUri = frontalUri,
                    errorMessage = frontalError,
                    onSelectImage = { frontalPickerLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                DniImageCard(
                    title = "DNI Posterior",
                    imageUri = posteriorUri,
                    errorMessage = posteriorError,
                    onSelectImage = { posteriorPickerLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                if (showSuccess) {
                    VerificationSuccessCard(
                        onContinue = onVerificationComplete
                    )
                } else {
                    SubmitButton(
                        isUploading = isUploading,
                        frontalSelected = frontalUri != null && frontalError.isEmpty(),
                        posteriorSelected = posteriorUri != null && posteriorError.isEmpty(),
                        onSubmit = {
                            if (frontalUri != null && posteriorUri != null) {
                                isUploading = true
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * InstructionText
 *
 * Muestra el texto explicativo para que el usuario suba las imágenes de su DNI.
 */
@Composable
private fun InstructionText() {
    Text(
        text = "Para verificar tu identidad, sube una foto del frente y el reverso de tu DNI.",
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(horizontal = 16.dp),
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp
    )
}

/**
 * DniImageCard
 *
 * Tarjeta que permite seleccionar una imagen del DNI (frontal o posterior).
 * Muestra placeholder si no hay imagen, preview si ya se seleccionó,
 * o mensaje de error si la validación falló.
 */
@Composable
private fun DniImageCard(
    title: String,
    imageUri: Uri?,
    errorMessage: String,
    onSelectImage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(0.9f)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable { onSelectImage() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    errorMessage.isNotEmpty() -> Color(0xFFFFEBEE)
                    imageUri != null -> MaterialTheme.colorScheme.surfaceContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (imageUri != null || errorMessage.isNotEmpty()) 4.dp else 0.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = when {
                            errorMessage.isNotEmpty() -> MaterialTheme.colorScheme.error
                            imageUri != null -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    errorMessage.isNotEmpty() -> {
                        ErrorContent(errorMessage = errorMessage)
                    }
                    imageUri != null -> {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        PlaceholderContent()
                    }
                }
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * PlaceholderContent
 *
 * Contenido mostrado cuando no se ha seleccionado ninguna imagen.
 */
@Composable
private fun PlaceholderContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Seleccionar imagen",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Toca para seleccionar imagen",
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * ErrorContent
 *
 * Contenido mostrado cuando la imagen seleccionada no pasa las validaciones.
 */
@Composable
private fun ErrorContent(
    errorMessage: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error en imagen",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * SubmitButton
 *
 * Botón para enviar la verificación. Se deshabilita si falta alguna imagen
 * o si está en proceso de carga. Muestra un indicador de progreso durante
 * la simulación de envío.
 */
@Composable
private fun SubmitButton(
    isUploading: Boolean,
    frontalSelected: Boolean,
    posteriorSelected: Boolean,
    onSubmit: () -> Unit
) {
    val isEnabled = frontalSelected && posteriorSelected && !isUploading

    Button(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        if (isUploading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Enviar verificación",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (!isUploading) {
        if (!frontalSelected || !posteriorSelected) {
            val missingParts = mutableListOf<String>()
            if (!frontalSelected) missingParts.add("frontal")
            if (!posteriorSelected) missingParts.add("posterior")
            val message = "Debe adjuntar la imagen ${missingParts.joinToString(" y ")} del DNI"
            Text(
                text = message,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * VerificationSuccessCard
 *
 * Mensaje de éxito mostrado después de completar la verificación simulada.
 * Incluye un botón para continuar.
 */
@Composable
private fun VerificationSuccessCard(
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.9f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verificación completada",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4CAF50)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Verificación completada",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tus datos han sido verificados exitosamente.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color(0xFF558B2F)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Continuar",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Constantes de validación
private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024  // 5MB en bytes
private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png")
private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png")

/**
 * Resultado de validación de la imagen del DNI
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String = ""
)

/**
 * Obtiene el nombre del archivo desde una Uri
 */
private fun getFileNameFromUri(context: Context, uri: Uri): String {
    return when {
        uri.scheme == "content" -> {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex("_display_name")
                it.moveToFirst()
                it.getString(nameIndex)
            } ?: "archivo"
        }
        uri.path != null -> {
            uri.path!!.substringAfterLast("/")
        }
        else -> "archivo"
    }
}

/**
 * Valida la imagen del DNI verificando:
 * - Formato: solo JPG, JPEG y PNG
 * - Tamaño: máximo 5MB
 */
private fun validateDniImage(context: Context, uri: Uri): ValidationResult {
    return try {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: ""
        val fileName = getFileNameFromUri(context, uri)
        val extension = fileName.substringAfterLast(".").lowercase()

        if (!ALLOWED_MIME_TYPES.contains(mimeType) && !ALLOWED_EXTENSIONS.contains(extension)) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Formato no permitido. Solo se aceptan JPG, JPEG y PNG."
            )
        }

        val fileSize = contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.available().toLong()
        } ?: 0L

        if (fileSize > MAX_FILE_SIZE_BYTES) {
            val sizeMB = String.format(Locale.US, "%.2f", fileSize / (1024f * 1024f))
            return ValidationResult(
                isValid = false,
                errorMessage = "La imagen es demasiado grande ($sizeMB MB). Máximo: 5 MB."
            )
        }

        ValidationResult(isValid = true)
    } catch (e: Exception) {
        ValidationResult(
            isValid = false,
            errorMessage = "Error al validar la imagen: ${e.message}"
        )
    }
}
