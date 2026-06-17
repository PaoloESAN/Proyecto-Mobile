# Contexto del Proyecto e Instrucciones para Agentes de IA

## 1. Visión General del Proyecto

Este proyecto es la plataforma FinTech de intercambio de divisas P2P (Peer-to-Peer) en versión móvil para Android. Permite a los usuarios realizar operaciones de compra/venta de dinero fiat directamente, eliminando intermediarios bancarios tradicionales. La aplicación actúa como un intermediario puramente informativo para facilitar la publicación de ofertas, el emparejamiento automático (matching), el chat en tiempo real, el intercambio de datos bancarios para transferencias directas P2P (externas a la app), la carga de comprobantes y la resolución de disputas mediante un panel de administración.

> [!IMPORTANT]
> Toda la persistencia de datos, la gestión de autenticación, la mensajería en tiempo real y el almacenamiento de archivos residen en **Supabase** (PostgreSQL + Auth + Storage + Realtime + Functions). La aplicación Android se integra de forma exclusiva y directa con Supabase mediante su SDK oficial para Kotlin (`io.github.jan-tennert.supabase`).

---

## 2. Stack Tecnológico & Skills del Proyecto

Para el desarrollo y mantenimiento de la aplicación móvil, el agente debe dominar y aplicar las siguientes tecnologías y habilidades ("skills"):

1. **Kotlin & Android Nativo (Jetpack Compose)**: Desarrollo modular con vistas declarativas en Compose, arquitectura MVVM/MVI, uso de ViewModels, y manejo de flujos asíncronos con `StateFlow` y `SharedFlow`.
2. **RikkaUI (Componentes UI personalizados)**: La aplicación utiliza una librería UI interna denominada RikkaUI para asegurar un diseño visual unificado (ej: `Button`, `Card`, `Text`, `ToastHost`, `RikkaTheme`).
3. **SessionManager (Persistencia Local)**: Gestión centralizada de sesión de usuario en SharedPreferences (`app/src/main/java/com/paoloesan/proyectomobile/data/local/SessionManager.kt`) para almacenar tokens de autenticación localmente, nombres del usuario y preferencias de tema.
4. **Integración con Supabase mediante el SDK de Kotlin (v3.x)**:
   - **Auth (`auth-kt`)**: Registro de usuarios (`signUpWith`), inicio de sesión (`signInWith`), cierre de sesión (`signOut`) y recuperación de contraseñas.
   - **Postgrest (`postgrest-kt`)**: Consultas directas, inserciones, actualizaciones y borrados en base de datos PostgreSQL en Supabase. Se deben usar clases serializables con `@Serializable` de `kotlinx.serialization` para mapear los payloads.
   - **Storage (`storage-kt`)**: Carga y descarga de imágenes desde buckets de Supabase Storage.
   - **Realtime (`realtime-kt`)**: Conexión al canal de Supabase Realtime para recibir notificaciones instantáneas e implementar el chat en tiempo real.
   - **Functions (`functions-kt`)**: Invocación de Supabase Edge Functions para ejecutar procesos en servidor, como lógica compleja o integraciones con Inteligencia Artificial.
5. **Manejo de Imágenes y Archivos (URIs)**:
   - Selección de archivos binarios de la galería o captura por cámara para KYC e imágenes de comprobantes de pago (vouchers).
   - Las imágenes deben ser JPG/PNG y pesar menos de 5MB antes de subirse al Storage de Supabase.
6. **Manejo de Formularios y Validaciones locales**:
   - Validar contraseñas fuertes (mínimo 8 caracteres, al menos una letra y un número).
   - Validar formatos de correos electrónicos y coincidencia de contraseñas.
   - Mantener una validación clara y directa dentro del `ViewModel` o en el componente UI y mostrar mensajes de retroalimentación mediante `ToastHost`.
7. **Gestor de Dependencias Obligatorio (Gradle)**:
   - Se utiliza **Gradle** (`build.gradle.kts` con Kotlin DSL) para la compilación, empaquetado y gestión de dependencias. Para instalar paquetes nuevos o sincronizar el proyecto se debe utilizar Gradle Sync en Android Studio.
8. **Uso Mandatario de Agent Skills**:
   - Es obligatorio que el agente lea y aplique las skills del asistente disponibles en el entorno de desarrollo (ubicadas en la carpeta `.agents/skills/` o configuradas en el proyecto). Esto asegura la aplicación de mejores prácticas de seguridad, políticas RLS, optimizaciones en base de datos y Compose.

---

## 3. Reglas de Negocio Críticas

- **Rol Intermediario P2P (Sin Custodia de Fondos)**: La aplicación no almacena, recibe ni custodia dinero. Las transferencias bancarias se realizan de forma externa y directa entre los usuarios de banco a banco (P2P). La app actúa únicamente como facilitador para mostrar la información bancaria de destino/recepción y los comprobantes de pago correspondientes.
- **Límites de Oferta**: Una oferta tiene un `monto_total` (inventario del usuario) y límites por transacción (`monto_minimo` y `monto_maximo`). El monto mínimo NUNCA puede superar al máximo.
- **Seguridad Transaccional**: El flujo de intercambio bloquea la edición/cancelación de una oferta en Supabase. Si una oferta tiene transacciones en estado `Pendiente`, `En Proceso`, `Pagado` o `Disputa`, no puede ser modificada (`update`) ni cancelada/eliminada (`delete`).
- **Flujo de Ofertas y Gestión ("Mis Ofertas")**:
  - Cada oferta en estado `"En Proceso"` se asocia con a lo más una transacción/solicitud activa (el emparejamiento es directo de 1 a 1).
  - En la lista de ofertas del usuario (`MyOffersScreen`), se ocultan las ofertas en estado `"Finalizada"`, mostrando únicamente las ofertas con estado `"Activa"` y `"En Proceso"`.
  - La edición y eliminación de una oferta solo está permitida si está en estado `"Activa"`. El flujo se realiza haciendo clic sobre la tarjeta de la oferta para abrir un modal de edición. Dicho modal contiene el botón de "Eliminar Oferta", el cual solicita confirmación a través de un diálogo adicional.
- **Ciclo de Transacciones (Doble Confirmación)**:
  1. `Pendiente`: El comprador inicia la transacción seleccionando su método/cuenta de recepción. Ambos participantes ven sus cuentas bancarias cruzadas en la aplicación.
  2. `Pagado` o `Confirmación Parcial`: Ambos participantes deben realizar sus transferencias cruzadas y subir sus respectivos comprobantes de pago al bucket `vouchers` de Supabase Storage, insertando un registro en la tabla `comprobantes` (almacenando `transaccion_id`, `usuario_id` de quien sube el voucher e `imagen_url` del voucher).
  3. `Finalizado` o `Disputa`: Ambos participantes deben verificar el comprobante de la contraparte y presionar "Confirmar Pago Correcto" (lo que actualiza `confirmado_comprador` o `confirmado_vendedor` a `true` en la tabla `transacciones`). Solo cuando ambos confirman, la transacción cambia al estado `Finalizado`. Cualquiera de las partes puede abrir un conflicto (`Disputa`) antes de confirmar.
- **Resolución de Disputas**: Exclusivo para administradores. La resolución es binaria en cuanto al estado informativo de la transacción dentro del sistema:
  - **A favor del comprador**: La transacción se cambia a estado `Cancelado` en Supabase y la oferta vuelve a estar `Activa` (disponible para recibir solicitudes de intercambio).
  - **A favor del vendedor**: La transacción se cambia a estado `Finalizado` (la transacción se marca como completada de forma definitiva en el sistema).
- **Calificación del Usuario**: Cada usuario posee un atributo `calificacion` (promedio del 1.00 al 5.00) en la tabla `usuarios`. Este promedio se actualiza de forma automática cada vez que otro usuario registra una nueva calificación para él en la tabla `calificaciones`.
- **Verificación de Vouchers con IA**:
  - La validación de los comprobantes de pago subidos por el comprador se procesa automáticamente mediante Inteligencia Artificial (por ejemplo, modelos de visión como Gemini 3.1 Flash Lite ejecutados a través de una Supabase Edge Function: `verificar-voucher-ia`).
  - La Edge Function analiza la imagen en busca de datos clave: banco emisor, cuenta de origen y destino, monto transferido, número de operación y fecha/hora.
  - **Lógica de Validación**: Los datos del voucher deben contrastarse contra el `monto_operacion` de la transacción y la cuenta del método de pago del vendedor. Si el monto y destinatario coinciden, la IA valida y aprueba la operación, registrando el resultado en la tabla `verificaciones_ia` y cambiando automáticamente la transacción al estado `Pagado`. En caso de discrepancias, se marca para revisión manual.

---

## 4. Estructura de Base de Datos y Modelos Kotlin

Toda la base de datos de Supabase PostgreSQL debe estar mapeada en la carpeta `com.paoloesan.proyectomobile.data.model` con la anotación `@Serializable` de kotlinx.serialization.

### A. Modelos de Datos en Kotlin

#### Perfil de Usuario (`UserProfileModel`)

Mapea a la tabla `public.usuarios` en Supabase. Los perfiles de usuario se enlazan opcionalmente con Supabase Auth.

```kotlin
package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileModel(
    @SerialName("usuario_id") val usuarioId: Int? = null, // PK Autogenerada
    @SerialName("auth_id") val authId: String? = null, // UUID de Supabase Auth
    @SerialName("nombres") val nombres: String,
    @SerialName("apellidos") val apellidos: String,
    @SerialName("correo") val correo: String,
    @SerialName("rol") val rol: String = "Usuario", // "Usuario" | "Administrador"
    @SerialName("estado") val estado: String = "Activo", // "Activo" | "Suspendido" | "Bloqueado"
    @SerialName("fecha_registro") val fechaRegistro: String? = null, // ISO String / timestamptz
    @SerialName("es_verificado") val esVerificado: Boolean = false,
    @SerialName("dni_frontal_url") val dniFrontalUrl: String? = null,
    @SerialName("dni_posterior_url") val dniPosteriorUrl: String? = null,
    @SerialName("calificacion") val calificacion: Double = 5.00 // Rango 1.00 a 5.00, promedio auto-calculado
)
```

#### Método de Pago (`PaymentMethodModel`)

Mapea a la tabla `public.metodos_pago`.

```kotlin
package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodModel(
    @SerialName("metodo_pago_id") val metodoPagoId: Int? = null, // PK Autogenerada
    @SerialName("usuario_id") val usuarioId: Int, // FK a usuarios
    @SerialName("banco") val banco: String,
    @SerialName("numero_cuenta") val numeroCuenta: String,
    @SerialName("nombre_titular") val nombreTitular: String,
    @SerialName("tipo_moneda") val tipoMoneda: String, // "USD" | "PEN"
    @SerialName("estado") val estado: String = "Activo", // "Activo" | "Inactivo"
    @SerialName("fecha_creacion") val fechaCreacion: String? = null
)
```

#### Oferta (`OfferModel`)

Mapea a la tabla `public.ofertas`.

```kotlin
package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfferModel(
    @SerialName("oferta_id") val offerId: Int? = null, // PK Autogenerada
    @SerialName("usuario_creador_id") val usuarioCreadorId: Int, // FK a usuarios
    @SerialName("metodo_pago_id") val metodoPagoId: Int, // FK a metodos_pago
    @SerialName("tipo_operacion") val tipoOperacion: String, // "Compra" | "Venta"
    @SerialName("moneda") val currency: String, // "USD" | "PEN"
    @SerialName("monto_total") val montoTotal: Double,
    @SerialName("monto_minimo") val montoMinimo: Double,
    @SerialName("monto_maximo") val montoMaximo: Double,
    @SerialName("tipo_cambio") val price: Double, // Tipo de cambio aplicado
    @SerialName("estado") val estado: String = "Activa", // "Activa" | "En Proceso" | "Inactiva" | "Cancelada" | "Finalizada"
    @SerialName("fecha_publicacion") val fechaPublicacion: String? = null
)
```

#### Transacción (`TransactionModel`)

Mapea a la tabla `public.transacciones`.

```kotlin
package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionModel(
    @SerialName("transaccion_id") val transactionId: Int? = null, // PK Autogenerada
    @SerialName("oferta_id") val offerId: Int, // FK a ofertas
    @SerialName("usuario_comprador_id") val usuarioCompradorId: Int, // FK a usuarios
    @SerialName("usuario_vendedor_id") val usuarioVendedorId: Int, // FK a usuarios
    @SerialName("metodo_pago_comprador_id") val metodoPagoCompradorId: Int? = null, // FK a metodos_pago del comprador
    @SerialName("monto_operacion") val amount: Double,
    @SerialName("tipo_cambio_aplicado") val tipoCambioAplicado: Double,
    @SerialName("estado") val status: String = "Pendiente", // "Pendiente" | "En Proceso" | "Pagado" | "Finalizado" | "Disputa" | "Cancelado"
    @SerialName("confirmado_comprador") val confirmadoComprador: Boolean = false,
    @SerialName("confirmado_vendedor") val confirmadoVendedor: Boolean = false,
    @SerialName("ya_calificado") val yaCalificado: Boolean = false,
    @SerialName("fecha_inicio") val createDate: String? = null,
    @SerialName("fecha_actualizacion") val fechaActualizacion: String? = null
)
```

#### Comprobante (`ComprobanteModel`)

Mapea a la tabla `public.comprobantes`.

```kotlin
package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComprobanteModel(
    @SerialName("comprobante_id") val comprobanteId: Int? = null, // PK Autogenerada
    @SerialName("transaccion_id") val transaccionId: Int, // FK a transacciones
    @SerialName("usuario_id") val usuarioId: Int, // FK a usuarios (quien sube el voucher)
    @SerialName("imagen_url") val imagenUrl: String, // URL pública del voucher en Storage
    @SerialName("fecha_subida") val fechaSubida: String? = null
)
```

#### Mensaje de Chat (`ChatMessageModel`)

Mapea a la tabla `public.mensajes_chat`.

```kotlin
package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageModel(
    @SerialName("mensaje_id") val mensajeId: Int? = null, // PK Autogenerada
    @SerialName("transaccion_id") val transaccionId: Int, // FK a transacciones
    @SerialName("remitente_id") val remitenteId: Int, // FK a usuarios
    @SerialName("contenido") val contenido: String,
    @SerialName("fecha_envio") val fechaEnvio: String? = null
)
```

#### Alerta de Cambio (`AlertaCambioModel`)

Mapea a la tabla `public.alertas_cambio`.

```kotlin
package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlertaCambioModel(
    @SerialName("alerta_id") val alertaId: Int? = null, // PK Autogenerada
    @SerialName("usuario_id") val usuarioId: Int, // FK a usuarios
    @SerialName("moneda") val moneda: String, // Ej: "USD"
    @SerialName("tipo_cambio_deseado") val tipoCambioDeseado: Double,
    @SerialName("estado") val estado: String = "Activa", // "Activa" | "Inactiva"
    @SerialName("fecha_creacion") val fechaCreacion: String? = null
)
```

#### Calificación (`CalificacionModel`)

Mapea a la tabla `public.calificaciones`.

```kotlin
package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalificacionModel(
    @SerialName("calificacion_id") val calificacionId: Int? = null, // PK Autogenerada
    @SerialName("transaccion_id") val transaccionId: Int, // FK a transacciones
    @SerialName("usuario_evaluador_id") val usuarioEvaluadorId: Int, // FK a usuarios
    @SerialName("usuario_evaluado_id") val usuarioEvaluadoId: Int, // FK a usuarios
    @SerialName("puntaje") val puntaje: Int, // Rango 1 - 5
    @SerialName("comentario") val comentario: String? = null,
    @SerialName("fecha_calificacion") val fechaCalificacion: String? = null
)
```

#### Disputa (`DisputeModel`)

Mapea a la tabla `public.disputas`.

```kotlin
package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DisputeModel(
    @SerialName("disputa_id") val disputaId: Int? = null, // PK Autogenerada
    @SerialName("transaccion_id") val transaccionId: Int, // FK a transacciones
    @SerialName("usuario_reportador_id") val usuarioReportadorId: Int, // FK a usuarios
    @SerialName("estado") val estado: String = "Abierta", // "Abierta" | "Resuelta"
    @SerialName("resolucion") val resolucion: String? = null, // "A favor del comprador" | "A favor del vendedor"
    @SerialName("fecha_apertura") val fechaApertura: String? = null,
    @SerialName("fecha_cierre") val fechaCierre: String? = null
)
```

#### Verificación de IA (`VerificacionIaModel`)

Mapea a la tabla `public.verificaciones_ia` que almacena los resultados devueltos por el motor de IA.

```kotlin
package com.paoloesan.proyectomobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerificacionIaModel(
    @SerialName("verificacion_id") val verificacionId: Int? = null, // PK Autogenerada
    @SerialName("comprobante_id") val comprobanteId: Int, // FK a comprobantes
    @SerialName("transaccion_id") val transaccionId: Int, // FK a transacciones
    @SerialName("es_valido") val esValido: Boolean,
    @SerialName("datos_extraidos") val datosExtraidos: String, // String formato JSON de campos leídos (banco, monto, nro_operacion, etc.)
    @SerialName("mensaje_error") val mensajeError: String? = null,
    @SerialName("fecha_analisis") val fechaAnalisis: String? = null
)
```

---

### B. Ejemplos de Lógica en Kotlin con el SDK de Supabase

Cualquier operación que interactúe con el backend debe implementarse de forma asíncrona mediante Coroutines en los ViewModels.

#### 1. Autenticación con `Supabase.client.auth`

##### Registrar Usuario

```kotlin
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

suspend fun registrarUsuario(correo: String, contrasenia: String) {
    Supabase.client.auth.signUpWith(Email) {
        email = correo
        password = contrasenia
    }
}
```

##### Iniciar Sesión y Persistir Token

```kotlin
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

suspend fun iniciarSesion(context: Context, correo: String, contrasenia: String) {
    val session = Supabase.client.auth.signInWith(Email) {
        email = correo
        password = contrasenia
    }
    // Almacenar el token localmente a través del SessionManager
    SessionManager.saveToken(context, session.accessToken)
}
```

#### 2. Operaciones de Base de Datos con `Supabase.client.postgrest`

##### Obtener Ofertas Filtradas

```kotlin
import io.github.jan.supabase.postgrest.postgrest

suspend fun obtenerOfertas(moneda: String, tipoOperacion: String): List<OfferModel> {
    return Supabase.client.postgrest["ofertas"]
        .select {
            filter {
                eq("moneda", moneda)
                eq("tipo_operacion", tipoOperacion)
                eq("estado", "Activa")
            }
        }.decodeList<OfferModel>()
}
```

##### Crear una Nueva Oferta (Insert)

```kotlin
import io.github.jan.supabase.postgrest.postgrest

suspend fun crearOferta(oferta: OfferModel): OfferModel {
    return Supabase.client.postgrest["ofertas"]
        .insert(oferta) {
            select()
        }.decodeSingle<OfferModel>()
}
```

##### Actualizar Oferta (Update)

```kotlin
import io.github.jan.supabase.postgrest.postgrest

suspend fun actualizarOferta(ofertaId: Int, nuevoMonto: Double) {
    Supabase.client.postgrest["ofertas"]
        .update({
            set("monto_total", nuevoMonto)
        }) {
            filter {
                eq("oferta_id", ofertaId)
            }
        }
}
```

#### 3. Almacenamiento y Registro de Comprobantes (Storage + Postgrest)

##### Subida de Comprobante de Pago y Creación de Registro

```kotlin
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.postgrest.postgrest

suspend fun subirYCrearComprobante(transaccionId: Int, usuarioId: Int, fileBytes: ByteArray, fileName: String): ComprobanteModel {
    val bucket = Supabase.client.storage["vouchers"]
    // 1. Subir archivo binario al storage de Supabase en la ruta por transacción y usuario
    bucket.upload(path = "$transaccionId/$usuarioId/$fileName", data = fileBytes) {
        upsert = true
    }
    // 2. Retornar la URL pública del voucher
    val publicUrl = bucket.publicUrl("$transaccionId/$usuarioId/$fileName")

    // 3. Crear el modelo de datos para insertar
    val comprobante = ComprobanteModel(
        transaccionId = transaccionId,
        usuarioId = usuarioId,
        imagenUrl = publicUrl
    )

    // 4. Registrar en la tabla public.comprobantes
    return Supabase.client.postgrest["comprobantes"]
        .insert(comprobante) {
            select()
        }.decodeSingle<ComprobanteModel>()
}
```

#### 4. Invocación de Edge Functions para Verificación con IA (`Supabase.client.functions`)

##### Invocación de Edge Function `verificar-voucher-ia`

```kotlin
import io.github.jan.supabase.functions.functions
import kotlinx.serialization.Serializable

@Serializable
data class VerificarVoucherRequest(
    val comprobanteId: Int,
    val transaccionId: Int
)

@Serializable
data class VerificarVoucherResponse(
    val esValido: Boolean,
    val mensaje: String,
    val verificacionId: Int
)

suspend fun solicitarVerificacionIa(comprobanteId: Int, transaccionId: Int): VerificarVoucherResponse {
    val request = VerificarVoucherRequest(comprobanteId, transaccionId)
    // Invocar la Edge Function para que analice el voucher y actualice la transacción
    return Supabase.client.functions.invoke<VerificarVoucherResponse>(
        function = "verificar-voucher-ia",
        body = request
    )
}
```

#### 5. Doble Confirmación de la Transacción (Postgrest)

##### Confirmación de Pago por Participante

Cada participante de la transacción (comprador y vendedor) debe confirmar el pago. Cuando ambos confirmen (`confirmadoComprador` y `confirmadoVendedor` sean `true`), el estado de la transacción pasará automáticamente a `Finalizado`.

```kotlin
import io.github.jan.supabase.postgrest.postgrest

suspend fun confirmarPagoTransaccion(transaccionId: Int, esComprador: Boolean): TransactionModel {
    val campoConfirmacion = if (esComprador) "confirmado_comprador" else "confirmado_vendedor"
    
    // 1. Actualizar el campo de confirmación correspondiente
    val txActualizada = Supabase.client.postgrest["transacciones"]
        .update({
            set(campoConfirmacion, true)
        }) {
            filter {
                eq("transaccion_id", transaccionId)
            }
            select()
        }.decodeSingle<TransactionModel>()

    // 2. Si ambos han confirmado, actualizar el estado global de la transacción a "Finalizado"
    if (txActualizada.confirmadoComprador && txActualizada.confirmadoVendedor) {
        return Supabase.client.postgrest["transacciones"]
            .update({
                set("estado", "Finalizado")
            }) {
                filter {
                    eq("transaccion_id", transaccionId)
                }
                select()
            }.decodeSingle<TransactionModel>()
    }

    return txActualizada
}
```

---

## 5. Integración del Chat en Tiempo Real (Supabase Realtime)

La aplicación móvil habilita la mensajería instantánea en el chat detallado de la transacción utilizando los canales en tiempo real de Supabase.

### Escuchar Mensajes del Chat en Tiempo Real

Para recibir nuevos mensajes instantáneamente en la pantalla de chat, nos suscribimos al canal de Supabase Realtime y escuchamos los cambios en la tabla `mensajes_chat` que correspondan a la transacción actual:

```kotlin
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

suspend fun escucharChat(transaccionId: Int): Flow<ChatMessageModel> {
    val channel = Supabase.client.realtime.createChannel("chat_transaccion_$transaccionId")

    val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
        table = "mensajes_chat"
        filter = "transaccion_id=eq.$transaccionId"
    }

    // Unirse al canal en tiempo real
    channel.join()

    return flow.mapNotNull { action ->
        // Deserializar el nuevo registro insertado en la tabla mensajes_chat
        action.record?.let { row ->
            Supabase.client.postgrest.serializer.decodeFromString<ChatMessageModel>(row.toString())
        }
    }
}
```

### Enviar Mensaje de Chat

El envío de mensajes se realiza simplemente insertando un nuevo registro en la tabla `mensajes_chat` en Supabase mediante Postgrest. Todos los participantes conectados a través de Realtime recibirán el evento instantáneamente:

```kotlin
suspend fun enviarMensaje(mensaje: ChatMessageModel) {
    Supabase.client.postgrest["mensajes_chat"].insert(mensaje)
}
```

---

## 6. Flujo de Transacción P2P, Cuentas Bancarias Cruzadas y Doble Confirmación

La vista detallada de la transacción en la aplicación Android (pantallas como `TransactionStatusScreen` y `BankDetailsScreen`) implementa un flujo interactivo estructurado y seguro para ambas partes:

### A. Cuentas Bancarias Cruzadas
Para facilitar el intercambio fiat sin confusiones sobre a dónde enviar el dinero, el sistema calcula y muestra de forma cruzada las cuentas bancarias de las partes:
1. **Cuenta Destino (Enviar Pago)**: Representa la cuenta bancaria de la **contraparte**. El usuario actual debe transferir fondos a este banco, número de cuenta y titular.
2. **Tu Cuenta de Recepción (Recibir Pago)**: Representa la cuenta del **usuario actual** configurada para esta transacción. Es el destino donde la contraparte depositará los fondos del intercambio.

### B. Desglose de Montos Exactos
La interfaz de usuario calcula dinámicamente y muestra de manera clara:
* **Monto a Enviar**: En la moneda destino, aplicando el tipo de cambio pactado en la oferta si la divisa es distinta al activo base.
* **Monto a Recibir**: El monto y divisa esperada en la cuenta receptora del usuario.
Esto asegura que ambos usuarios sepan exactamente cuántas unidades de fiat enviar y recibir.

### C. Flujo de Doble Confirmación y Voucher
1. **Envío de Comprobante**: Ambas partes deben realizar su transferencia correspondiente y subir su comprobante de pago (mediante `subirYCrearComprobante`).
2. **Habilitación de Confirmación**: El botón **"Confirmar Pago Correcto"** (que marca `confirmadoComprador` o `confirmadoVendedor` en `true` en la transacción) está estrictamente condicionado a que la contraparte ya haya subido su comprobante (`contraparteVoucher != null`). Si no lo ha hecho, se muestra un banner de advertencia animando a esperar. Esto previene liberaciones accidentales de fondos antes de verificar el recibo.
3. **Mecanismo de Disputa**: El botón **"Abrir Disputa"** permanece habilitado e interactivo en todo momento si surge algún inconveniente, y debe contar con un **Modal de Confirmación** para evitar aperturas accidentales.
