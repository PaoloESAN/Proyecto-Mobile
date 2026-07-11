# Documentación Completa del Proyecto - P2P Currency Exchange Mobile App

## Tabla de Contenidos

1. [Visión General](#1-visión-general)
2. [Stack Tecnológico](#2-stack-tecnológico)
3. [Estructura del Proyecto](#3-estructura-del-proyecto)
4. [Arquitectura](#4-arquitectura)
5. [Data Layer](#5-data-layer)
6. [Modelos de Datos](#6-modelos-de-datos)
7. [Navegación](#7-navegación)
8. [Pantallas y Funcionalidades](#8-pantallas-y-funcionalidades)
9. [Reglas de Negocio](#9-reglas-de-negocio)
10. [Flujo de Transacciones P2P](#10-flujo-de-transacciones-p2p)
11. [Sistema de Notificaciones](#11-sistema-de-notificaciones)
12. [Integración con Supabase](#12-integración-con-supabase)
13. [Configuración de Gradle](#13-configuración-de-gradle)
14. [UI y Temas](#14-ui-y-temas)
15. [Seguridad](#15-seguridad)
16. [Referencias de Base de Datos](#16-referencias-de-base-de-datos)

---

## 1. Visión General

Plataforma FinTech de intercambio de divisas P2P (Peer-to-Peer) para Android. Permite a usuarios realizar operaciones de compra/venta de dinero fiat directamente, eliminando intermediarios bancarios tradicionales. La aplicación actúa como facilitador informativo para la publicación de ofertas, emparejamiento automático (matching), chat en tiempo real, intercambio de datos bancarios, carga de comprobantes y resolución de disputas.

### 1.1. Propósito

- Facilitar el intercambio de divisas (USD/PEN) entre personas sin intermediación bancaria.
- Proveer un sistema de confianza mediante calificaciones, verificación de identidad y resolución de disputas.
- Automatizar la verificación de comprobantes de pago mediante Inteligencia Artificial.

### 1.2. Público Objetivo

- Usuarios que necesitan cambiar divisas de forma P2P.
- Administradores que gestionan disputas y suspenden usuarios infractores.

---

## 2. Stack Tecnológico

### 2.1. Lenguaje y Framework

| Componente           | Tecnología                        |
| -------------------- | --------------------------------- |
| Lenguaje             | Kotlin 1.9.x                      |
| UI Framework         | Jetpack Compose (BOM 2024.12.01)  |
| Minimum SDK          | API 26 (Android 8.0)              |
| Target SDK           | API 35 (Android 15)               |
| Build System         | Gradle con Kotlin DSL             |

### 2.2. Backend y Servicios Externos

| Servicio        | Uso                                               |
| --------------- | ------------------------------------------------- |
| Supabase        | Backend principal (Auth, DB, Storage, Realtime, Functions) |
| Firebase Cloud Messaging | Notificaciones push Android                    |
| Ktor            | Cliente HTTP (para Supabase SDK)                  |

### 2.3. Librerías Principales

| Dependencia                              | Propósito                                    |
| ---------------------------------------- | -------------------------------------------- |
| `io.github.jan-tennert.supabase:*`       | SDK oficial de Supabase para Kotlin          |
| `androidx.compose.material3`             | Material Design 3                            |
| `androidx.navigation.compose`            | Navegación Compose                           |
| `androidx.lifecycle.viewmodel-compose`   | ViewModel Integration                        |
| `org.koin.compose`                       | Inyección de dependencias                    |
| `io.coil-kt.coil.compose`                | Carga de imágenes                            |
| `com.google.firebase:firebase-messaging` | Notificaciones push                          |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | Serialización JSON            |
| `com.google.accompanist:accompanist-permissions` | Permisos runtime                       |

### 2.4. RikkaUI (Librería de Diseño Interna)

La aplicación utiliza una librería UI personalizada llamada **RikkaUI** para componentes visuales unificados:
- `Button`, `Card`, `Text`, `TextField`
- `ToastHost` y `ToastScope` para notificaciones temporales
- `RikkaTheme` para el tema global
- Iconos, colores y tipografía predefinidos

Ubicación de la dependencia en `libs.versions.toml`:
```toml
rikka = "1.1.6"
rikka-ui = { group = "com.rickandmorty.rikka", name = "ui", version.ref = "rikka" }
```

---

## 3. Estructura del Proyecto

```
ProyectoMobile/
├── app/
│   ├── build.gradle.kts                 # Módulo app
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/paoloesan/proyectomobile/
│       │   ├── MainActivity.kt          # Entry point
│       │   ├── ProyectoApp.kt           # Application class
│       │   ├── data/
│       │   │   ├── SupabaseClient.kt    # Configuración Supabase
│       │   │   ├── AuthEventChannel.kt  # Event bus de auth
│       │   │   ├── local/
│       │   │   │   ├── SessionManager.kt
│       │   │   │   └── MyFirebaseMessagingService.kt
│       │   │   ├── model/               # Data models (@Serializable)
│       │   │   ├── remote/              # Repositorios Supabase
│       │   │   └── repository/          # Repositorios
│       │   ├── di/
│       │   │   └── AppModule.kt         # Módulo Koin DI
│       │   ├── navigation/
│       │   │   └── AppNavigation.kt     # Navegación principal
│       │   ├── presentation/
│       │   │   ├── navigation/          # NavHost, extensiones
│       │   │   ├── auth/                # Login, Registro, Passwords
│       │   │   ├── p2p/                 # Marketplace, Mis Ofertas, Publicar, Match
│       │   │   ├── transaction/         # Estado, Detalle, Bancos, Chat
│       │   │   ├── profile/             # Perfil, Editar, Configuración
│       │   │   ├── history/             # Historial de transacciones
│       │   │   ├── admin/               # Panel de administración
│       │   │   ├── disputa/             # Sistema de disputas
│       │   │   ├── verification/        # Verificación de identidad
│       │   │   ├── components/          # Componentes reutilizables
│       │   │   └── debug/               # Pantalla de depuración
│       │   └── ui/theme/
│       │       ├── AppTheme.kt          # Tema Material3
│       │       ├── Color.kt
│       │       └── Theme.kt
│       └── res/
├── build.gradle.kts                     # Build raíz
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   └── libs.versions.toml               # Catálogo de versiones
├── AGENTS.md                            # Instrucciones para asistentes IA
└── DOCS.md                              # Este documento
```

---

## 4. Arquitectura

### 4.1. Patrón MVVM

La aplicación sigue el patrón **Model-View-ViewModel**:

- **Model**: Modelos de datos en `data/model/` con `@Serializable` de kotlinx.serialization + repositorios en `data/repository/` y `data/remote/` que encapsulan llamadas a Supabase.
- **ViewModel**: Almacena el estado de UI mediante `StateFlow` y `SharedFlow`. Expone eventos y acciones a la vista. Se inyectan dependencias via Koin.
- **View**: Compose Screens que observan el estado del ViewModel y reaccionan a eventos.

### 4.2. Flujo de Datos

```
UI (Composable)
  ↕ observa StateFlow / emite eventos
ViewModel
  ↕ llama a repositorios
Repository / Supabase Remote
  ↕ operaciones asíncronas
Supabase SDK (Auth, Postgrest, Storage, Realtime, Functions)
  ↕ HTTP / WebSocket
Supabase Cloud (PostgreSQL + Edge Functions + Realtime)
```

### 4.3. Inyección de Dependencias (Koin)

Definida en `di/AppModule.kt` con módulos:
- **authModule**: AuthRepository
- **userModule**: UserRepository
- **supabaseModule**: Cliente Supabase singleton
- **appModule**: SessionManager, ProyectoApp context

---

## 5. Data Layer

### 5.1. SupabaseClient.kt

Configuración centralizada del cliente Supabase en `data/SupabaseClient.kt`:

```kotlin
val Supabase.client: SupabaseClient by lazy {
    createSupabaseClient(
        supabaseUrl = "https://jhqnddkkcosvtdhrrxue.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    ) {
        install(Auth) { /* sessionManager personalizado */ }
        install(Postgrest) { /* serializers personalizados */ }
        install(Realtime)
        install(Storage)
        install(Functions)
    }
}
```

Funciones de extensión:
- `currentUserAwaitInit()`: Retorna el usuario actual esperando a que el SDK termine de cargar la sesión del caché local. Previene condiciones de carrera al inicio de la app.

### 5.2. SessionManager.kt

Gestión de sesión local en `data/local/SessionManager.kt` usando `SharedPreferences`:
- **saveToken()**: Guarda access token, refresh token, nombre de usuario, email y preferencia de tema oscuro.
- **getToken()**: Recupera el access token.
- **getUserName()**: Recupera el nombre del usuario logueado.
- **clearSession()**: Limpia toda la sesión al cerrar sesión.
- **isDarkMode() / setDarkMode()**: Preferencia de tema del dispositivo.
- **isLoggedIn()**: Verifica si hay sesión activa.

### 5.3. AuthRepository.kt

Operaciones de autenticación en `data/repository/AuthRepository.kt`:
- `login(email, password)`: Inicio de sesión con Email provider, almacena token y nombre.
- `register(email, password, nombres, apellidos)`: Registro + creación de perfil en tabla `usuarios`.
- `logout()`: Cierre de sesión + limpieza de datos locales.
- `recoverPassword(email)`: Envío de correo de recuperación.
- `resetPassword(newPassword)`: Restablecimiento de contraseña.

### 5.4. UserRepository.kt

Repositorio para operaciones de perfil en `data/repository/UserRepository.kt`:
- `getUserProfile(authId)`: Obtiene perfil por `auth_id`.
- `updateUserProfile()`: Actualiza datos del perfil.
- `getActiveUsers()`: Lista de usuarios activos (para admin).
- `blockUser()` / `unblockUser()`: Gestión de bloqueos.
- `uploadProfilePhoto()`: Subida de foto de perfil a Supabase Storage.
- `getPhotoUrl()`: Obtiene URL pública de foto de perfil.
- `searchUsers(query)`: Búsqueda de usuarios.
- `getUserRating()`: Obtiene calificación promedio.
- `isUserBlocked()`: Verifica si un usuario está bloqueado.

### 5.5. MyFirebaseMessagingService.kt

Servicio de Firebase Cloud Messaging en `data/local/MyFirebaseMessagingService.kt`:
- **onNewToken**: Al obtener un nuevo token FCM, lo envía al endpoint `/api/tokens/register` (probablemente una Edge Function de Supabase).
- **onMessageReceived**: Al recibir una notificación, extrae datos y muestra una notificación local con canales específicos.

### 5.6. AuthEventChannel.kt

Event bus para eventos de autenticación usando `Channel` y `SharedFlow`:
- Emite eventos `AuthEvent` (login, logout, session_expired).
- Otros componentes se suscriben para reaccionar a cambios de sesión.

---

## 6. Modelos de Datos

Todos los modelos están en `data/model/` y utilizan `@Serializable` de `kotlinx.serialization`.

### 6.1. UserProfileModel - `usuarios`

| Campo                | Tipo      | Descripción                              |
| -------------------- | --------- | ---------------------------------------- |
| `usuario_id`         | Int?      | PK autogenerada                          |
| `auth_id`            | String?   | UUID de Supabase Auth                    |
| `nombres`            | String    | Nombres del usuario                      |
| `apellidos`          | String    | Apellidos del usuario                    |
| `correo`             | String    | Correo electrónico                       |
| `rol`                | String    | "Usuario" o "Administrador"              |
| `estado`             | String    | "Activo", "Suspendido", "Bloqueado"      |
| `fecha_registro`     | String?   | ISO timestamp de registro                |
| `es_verificado`      | Boolean   | Verificación de identidad completada     |
| `dni_frontal_url`    | String?   | URL de foto frontal del DNI              |
| `dni_posterior_url`  | String?   | URL de foto posterior del DNI            |
| `calificacion`       | Double    | Promedio 1.00-5.00                       |
| `bloqueos_anteriores`| Int       | Contador de bloqueos                     |
| `bloqueado_hasta`    | String?   | Fecha fin de bloqueo (null = indefinido) |

### 6.2. OfferModel - `ofertas`

| Campo                | Tipo      | Descripción                              |
| -------------------- | --------- | ---------------------------------------- |
| `oferta_id`          | Int?      | PK autogenerada                          |
| `usuario_creador_id` | Int       | FK a usuarios                            |
| `metodo_pago_id`     | Int       | FK a metodos_pago                        |
| `tipo_operacion`     | String    | "Compra" o "Venta"                       |
| `moneda`             | String    | "USD" o "PEN"                            |
| `monto_total`        | Double    | Monto total disponible                    |
| `monto_minimo`       | Double    | Monto mínimo por transacción             |
| `monto_maximo`       | Double    | Monto máximo por transacción             |
| `tipo_cambio`        | Double    | Tipo de cambio aplicado                  |
| `estado`             | String    | "Activa", "En Proceso", "Inactiva", "Cancelada", "Finalizada" |
| `fecha_publicacion`  | String?   | ISO timestamp                            |

### 6.3. TransactionModel - `transacciones`

| Campo                     | Tipo    | Descripción                              |
| ------------------------- | ------- | ---------------------------------------- |
| `transaccion_id`          | Int?    | PK autogenerada                          |
| `oferta_id`               | Int     | FK a ofertas                             |
| `usuario_comprador_id`    | Int     | FK a usuarios                            |
| `usuario_vendedor_id`     | Int     | FK a usuarios                            |
| `metodo_pago_comprador_id`| Int?    | FK a metodos_pago                        |
| `monto_operacion`         | Double  | Monto de la operación                    |
| `tipo_cambio_aplicado`    | Double  | Tipo de cambio al momento de la transacción |
| `estado`                  | String  | "Pendiente", "En Proceso", "Pagado", "Finalizado", "Disputa", "Cancelado" |
| `confirmado_comprador`    | Boolean | Confirmación del comprador               |
| `confirmado_vendedor`     | Boolean | Confirmación del vendedor                |
| `ya_calificado`           | Boolean | Si ya fue calificada                     |
| `fecha_inicio`            | String? | ISO timestamp                            |
| `fecha_actualizacion`     | String? | Última actualización                     |

### 6.4. PaymentMethodModel - `metodos_pago`

| Campo               | Tipo    | Descripción                  |
| ------------------- | ------- | ---------------------------- |
| `metodo_pago_id`    | Int?    | PK autogenerada              |
| `usuario_id`        | Int     | FK a usuarios                |
| `banco`             | String  | Nombre del banco             |
| `numero_cuenta`     | String  | Número de cuenta/CCI         |
| `nombre_titular`    | String  | Nombre del titular           |
| `tipo_moneda`       | String  | "USD" o "PEN"                |
| `estado`            | String  | "Activo" o "Inactivo"        |
| `fecha_creacion`    | String? | ISO timestamp                |

### 6.5. ComprobanteModel - `comprobantes`

| Campo              | Tipo    | Descripción                              |
| ------------------ | ------- | ---------------------------------------- |
| `comprobante_id`   | Int?    | PK autogenerada                          |
| `transaccion_id`   | Int     | FK a transacciones                       |
| `usuario_id`       | Int     | FK a usuarios (quien sube el voucher)    |
| `imagen_url`       | String  | URL pública del voucher en Storage       |
| `fecha_subida`     | String? | ISO timestamp                            |

### 6.6. ChatMessageModel - `mensajes_chat`

| Campo               | Tipo    | Descripción                  |
| ------------------- | ------- | ---------------------------- |
| `mensaje_id`        | Int?    | PK autogenerada              |
| `transaccion_id`    | Int     | FK a transacciones           |
| `remitente_id`      | Int     | FK a usuarios                |
| `contenido`         | String  | Contenido del mensaje        |
| `fecha_envio`       | String? | ISO timestamp                |

### 6.7. DisputeModel - `disputas`

| Campo                   | Tipo    | Descripción                              |
| ----------------------- | ------- | ---------------------------------------- |
| `disputa_id`            | Int?    | PK autogenerada                          |
| `transaccion_id`        | Int     | FK a transacciones                       |
| `usuario_reportador_id` | Int     | FK a usuarios                            |
| `estado`                | String  | "Abierta" o "Resuelta"                   |
| `resolucion`            | String? | "A favor del comprador" o "A favor del vendedor" |
| `fecha_apertura`        | String? | ISO timestamp                            |
| `fecha_cierre`          | String? | ISO timestamp                            |

### 6.8. VerificacionIaModel - `verificaciones_ia`

| Campo               | Tipo    | Descripción                              |
| ------------------- | ------- | ---------------------------------------- |
| `verificacion_id`   | Int?    | PK autogenerada                          |
| `comprobante_id`    | Int     | FK a comprobantes                        |
| `transaccion_id`    | Int     | FK a transacciones                       |
| `es_valido`         | Boolean | Resultado de la validación               |
| `datos_extraidos`   | String  | JSON con datos extraídos del voucher     |
| `mensaje_error`     | String? | Mensaje de error si aplica               |
| `fecha_analisis`    | String? | ISO timestamp                            |

### 6.9. CalificacionModel - `calificaciones`

| Campo                  | Tipo    | Descripción                  |
| ---------------------- | ------- | ---------------------------- |
| `calificacion_id`      | Int?    | PK autogenerada              |
| `transaccion_id`       | Int     | FK a transacciones           |
| `usuario_evaluador_id` | Int     | FK a usuarios                |
| `usuario_evaluado_id`  | Int     | FK a usuarios                |
| `puntaje`              | Int     | Rango 1-5                    |
| `comentario`           | String? | Comentario opcional          |
| `fecha_calificacion`   | String? | ISO timestamp                |

### 6.10. AlertaCambioModel - `alertas_cambio`

| Campo                  | Tipo    | Descripción                  |
| ---------------------- | ------- | ---------------------------- |
| `alerta_id`            | Int?    | PK autogenerada              |
| `usuario_id`           | Int     | FK a usuarios                |
| `moneda`               | String  | "USD" o "PEN"                |
| `tipo_cambio_deseado`  | Double  | Tipo de cambio objetivo      |
| `estado`               | String  | "Activa" o "Inactiva"        |
| `fecha_creacion`       | String? | ISO timestamp                |

### 6.11. Modelos Auxiliares (`MatchModels.kt`)

| Modelo              | Propósito                                    |
| ------------------- | -------------------------------------------- |
| `OfertaMatch`       | Representa una oferta que hace match con las preferencias del comprador |
| `MatchParams`       | Parámetros para realizar emparejamiento (moneda, tipo_operacion, monto) |
| `MatchResult`       | Resultado del proceso de matching            |
| `VerificarVoucherRequest` | Payload para invocar la Edge Function `verificar-voucher-ia` |
| `VerificarVoucherResponse` | Respuesta de la Edge Function            |
| `CalificarRequest`  | Payload para enviar una calificación         |
| `UserRow`           | Fila de usuario para la lista de admin       |
| `BlockRequest`      | Payload para bloquear usuario                |
| `DisputeResolveRequest` | Payload para resolver disputa            |

---

## 7. Navegación

### 7.1. Entry Point - MainActivity.kt

`MainActivity.kt` es la actividad principal que configura:
1. **Edge-to-edge**: Uso de `enableEdgeToEdge()`.
2. **Tema**: `AppTheme(darkMode)` con acceso a `SessionManager`.
3. **NavHost**: Enrutamiento principal entre pantallas.
4. **Auth Event Observer**: Observa `AuthEventChannel` para acciones globales como cierre de sesión por expiración.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sessionManager = SessionManager(context = this@MainActivity)
            val darkMode = sessionManager.isDarkMode().collectAsState(initial = false)
            
            AppTheme(darkMode = darkMode.value) {
                AppNavigation(context = this@MainActivity)
            }
        }
    }
}
```

### 7.2. AppNavigation.kt

Sistema de navegación en `navigation/AppNavigation.kt`:
- **Rutas de autenticación**: LoginScreen, RegistroScreen, RecoverPasswordScreen, ResetPasswordScreen.
- **Deep link**: `interya://recovery` para recuperación de contraseña.
- **Pantalla principal**: MainScreen (con bottom navigation).
- **Pantallas sin bottom nav**: Pantallas de detalle de transacción, chat, etc.
- **Condición de inicio**: Si hay sesión → MainScreen; si no → LoginScreen.

### 7.3. AppNav.kt - Navegación Inferior

`presentation/navigation/AppNav.kt` define el Bottom Navigation con 5 pestañas:

| Icono | Ruta       | Pantalla            |
| ----- | ---------- | ------------------- |
| Home  | `market`   | MarketplaceScreen   |
| Swap  | `match`    | MatchScreen         |
| +     | `publish`  | PublishOfferScreen  |
| Clock | `history`  | HistoryScreen       |
| User  | `profile`  | ProfileScreen       |

- La pestaña de publicar (publish) actúa como FAB (no como navegación de pestaña).
- El Bottom Bar se oculta en pantallas de detalle (chat, transacción, etc.).

### 7.4. Estructura de Rutas

```
LoginScreen → RegistroScreen
LoginScreen → RecoverPasswordScreen → ResetPasswordScreen (deep link)

MainScreen (Bottom Navigation)
├── MarketplaceScreen → OfferDetailScreen → TransactionStatusScreen
│                                           ├── BankDetailsScreen
│                                           ├── ChatScreen
├── MatchScreen → MatchScreen (resultados)
├── PublishOfferScreen
├── HistoryScreen → TransactionStatusScreen
├── ProfileScreen → SettingsScreen
│                 → EditProfileScreen
│                 → IdentityVerificationScreen

Admin:
├── AdminUsersScreen → (bloqueo de usuarios)
├── DisputaListaScreen → DisputaDetalleScreen → TransactionStatusScreen
```

---

## 8. Pantallas y Funcionalidades

### 8.1. Autenticación

#### LoginScreen.kt | LoginViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Inicio de sesión de usuarios                         |
| Campos          | Email, Contraseña                                    |
| Validaciones    | Email válido, contraseña no vacía                    |
| Estados         | Idle, Loading, Success (navega a MainScreen), Error  |
| Flujo           | 1. Validar formato email → 2. Verificar contraseña en Supabase Auth → 3. Verificar si usuario está bloqueado → 4. Auto-recuperación si bloqueo expiró → 5. Redirigir a MainScreen |
| Seguridad       | La verificación de bloqueo ocurre **después** de validar la contraseña (protección contra enumeración de emails) |
| Guardado local  | Token, nombre de usuario, email en SessionManager    |

#### RegistroScreen.kt | RegistroViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Registro de nuevo usuario                            |
| Campos          | Nombres, Apellidos, Email, Contraseña, Confirmar Contraseña |
| Validaciones    | Contraseña: mínimo 8 caracteres, al menos 1 letra y 1 número. Emails coinciden. Contraseñas coinciden. |
| Flujo           | 1. Validar campos → 2. `signUpWith(Email)` → 3. Insertar perfil en `usuarios` → 4. Redirigir a Login |

#### RecoverPasswordScreen.kt

- Solicita correo electrónico.
- Envía email de recuperación mediante Supabase Auth.
- Deep link `interya://recovery` para abrir la app desde el correo.

#### ResetPasswordScreen.kt

- Capturada mediante deep link `interya://recovery`.
- Permite al usuario establecer una nueva contraseña.
- Usa `supabase.auth.updateUser()` para actualizar la contraseña.

### 8.2. Marketplace (P2P)

#### MarketplaceScreen.kt | MarketplaceViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Listado de ofertas activas disponibles               |
| Filtros         | Tipo de operación (Compra/Venta), Moneda (USD/PEN)   |
| Ordenamiento    | Por tipo de cambio (mejor precio)                    |
| Cards           | Muestran: usuario creador, calificación, precio, monto mínimo/máximo, banco |
| Acción          | Tap → navega a OfferDetailScreen                     |
| Actualización   | Pull-to-refresh                                      |

#### OfferDetailScreen.kt | OfferDetailViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Detalle de una oferta y formulario para crear transacción |
| Campos          | Monto a operar (validado entre monto_minimo y monto_maximo) |
| Info mostrada   | Creador, calificación, total de transacciones, tipo de cambio, banco, titular |
| Flujo           | Ingresar monto → Recalcular monto recibido → Confirmar → Crear transacción |
| Estado          | Loading, Success (navega a TransactionStatusScreen), Error |

#### PublishOfferScreen.kt | PublishOfferViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Crear una nueva oferta de intercambio                |
| Campos          | Tipo (Compra/Venta), Moneda (USD/PEN), Monto total, Monto mínimo, Monto máximo, Tipo de cambio, Método de pago |
| Validaciones    | Monto mínimo < Monto máximo. Monto total > 0. Tipo de cambio > 0 |
| Flujo           | 1. Seleccionar tipo y moneda → 2. Ingresar montos → 3. Seleccionar/crear método de pago → 4. Publicar |
| Estados         | Idle, Loading, Success, Error                        |

#### MyOffersScreen.kt | MyOffersViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Lista de ofertas del usuario actual                  |
| Filtro          | Muestra solo ofertas "Activa" y "En Proceso" (oculta "Finalizada") |
| Acciones        | Tap → Modal de edición con campos editables          |
| Eliminación     | Botón dentro del modal → Diálogo de confirmación     |
| Restricción     | Solo se puede editar/eliminar ofertas en estado "Activa" |
| Estados         | Cargando, Vacío, Lista de ofertas                    |

### 8.3. Match Screen

#### MatchScreen.kt | MatchViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Buscar ofertas que coincidan con las preferencias del usuario |
| Filtros         | Tipo de operación, Moneda, Monto                     |
| Funcionamiento  | Llama a una función RPC en Supabase (`buscar_ofertas_match`) que ejecuta lógica del lado del servidor |
| Resultado       | Lista de `OfertaMatch` con ofertas compatibles       |
| Estados         | Idle, Loading, Results, NoResults, Error             |

### 8.4. Transacciones

#### TransactionStatusScreen.kt | TransactionViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Seguimiento del estado de una transacción activa     |
| Estados         | Pendiente, En Proceso, Pagado, Finalizado, Disputa, Cancelado |
| Acciones        | Subir comprobante (voucher), Confirmar pago, Abrir disputa |
| Información     | Monto, tipo de cambio, datos bancarios cruzados, estado, comprobantes |
| Condición confirmación | Botón "Confirmar Pago Correcto" habilitado solo si la contraparte ya subió su comprobante |
| Chat            | Botón para navegar al ChatScreen                     |
| IA              | Al subir voucher, invoca automáticamente `verificar-voucher-ia` |
| Subida voucher  | Usa `ActivityResultContracts.GetContent()` para seleccionar imagen de galería |

#### BankDetailsScreen.kt | BankViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Mostrar datos bancarios cruzados de ambas partes     |
| Info mostrada   | Cuenta destino (contraparte) y tu cuenta de recepción, con montos exactos a enviar/recibir |
| Botones         | "Copiar número de cuenta", "Ir a mi banco" (abre app externa) |

#### ChatScreen.kt | ChatViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Chat en tiempo real entre comprador y vendedor        |
| Tecnología      | Supabase Realtime (canal `chat_transaccion_$id`)     |
| Funcionamiento  | 1. Crear canal Realtime → 2. Suscribirse a inserts en `mensajes_chat` → 3. Mostrar mensajes en Flow |
| Envío           | Insertar mensaje en tabla `mensajes_chat` mediante Postgrest |
| UI              | Burbujas de chat estilo WhatsApp, campo de texto + botón enviar |

### 8.5. Perfil

#### ProfileScreen.kt | ProfileViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Mostrar perfil del usuario logueado                  |
| Info            | Foto de perfil (URL desde Supabase Storage), nombre, email, calificación, verificación KYC |
| Acciones        | Ir a Editar Perfil, Configuración, Verificación KYC, Cerrar Sesión |
| Estado KYC      | Muestra si el usuario está verificado o pendiente    |
| Foto            | Cargada con Coil desde Supabase Storage              |

#### EditProfileScreen.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Editar datos del perfil (nombres, apellidos, email)  |
| Acción          | Guardar cambios → `updateUserProfile()`              |

#### SettingsScreen.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Configuración de la aplicación                       |
| Opciones        | Tema (Claro/Oscuro/Sistema), Cerrar sesión           |
| Tema            | Persiste preferencia en SessionManager               |

### 8.6. Historial

#### HistoryScreen.kt | HistoryViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Mostrar historial de transacciones del usuario       |
| Lista           | Transacciones con estado, contraparte, monto, fecha  |
| Acción          | Tap → Navega a TransactionStatusScreen (solo lectura) |
| Filtro          | Por estado (todas, pendientes, finalizadas, etc.)    |
| Calificación    | Si la transacción está finalizada y no calificada, permite calificar a la contraparte (RatingBar 1-5 + comentario opcional) |

### 8.7. Administración

#### AdminUsersScreen.kt | AdminUsersViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Panel de administración para gestionar usuarios      |
| Acceso          | Solo usuarios con rol "Administrador"                |
| Lista           | Usuarios con nombre, email, estado, calificación     |
| Búsqueda        | Filtro por nombre o email                            |
| Acción          | Tap → Modal de bloqueo con duración (1h, 24h, 7d, 1m, 3m, indefinido) |
| Estados         | Loading, Success, Error, Empty                       |

### 8.8. Disputas

#### DisputaListaScreen.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Lista de disputas abiertas (solo admin)              |
| Acción          | Tap → DisputaDetalleScreen                           |

#### DisputaDetalleScreen.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Resolución de una disputa específica                 |
| Acciones        | Resolver a favor del comprador (→ Cancelado) o del vendedor (→ Finalizado) |
| Resolución      | Actualiza estado de la transacción y de la disputa   |

### 8.9. Verificación de Identidad (KYC)

#### IdentityVerificationScreen.kt | IdentityVerificationViewModel.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Subir fotos del DNI para verificación de identidad   |
| Pasos           | 1. Subir foto frontal del DNI → 2. Subir foto posterior del DNI → 3. Auto-verificación |
| Almacenamiento  | Subida a Supabase Storage bucket `kyc-documents`     |
| Finalización    | Marca `es_verificado = true` en `usuarios`           |
| Estados         | Idle, FrontUploaded, BackUploaded, Success, Error    |

### 8.10. Componentes Compartidos

#### OfflineScreen.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Pantalla mostrada cuando no hay conexión a internet  |
| Detección       | Monitoreo de ConnectivityManager                     |
| UI              | Mensaje "Sin conexión" + animación + botón reintentar|

#### DebugScreen.kt

| Aspecto         | Detalle                                              |
| --------------- | ---------------------------------------------------- |
| Propósito       | Pantalla de depuración para desarrolladores           |
| Función         | Muestra información de estado de la app, sesión, etc.|
| Acceso          | Desde el perfil (oculta)                             |

---

## 9. Reglas de Negocio

### 9.1. Rol de Intermediario P2P

- La aplicación **no custodia, almacena ni maneja dinero** de los usuarios.
- Las transferencias bancarias se realizan directamente entre usuarios (banco a banco).
- La app solo facilita: publicación de ofertas, emparejamiento, intercambio de datos bancarios, subida de comprobantes, chat y resolución de disputas.

### 9.2. Límites de Oferta

- `monto_minimo` NUNCA puede ser mayor que `monto_maximo`.
- `monto_total` es el inventario disponible del usuario para esa oferta.
- Las transacciones deben estar dentro del rango [monto_minimo, monto_maximo].

### 9.3. Seguridad Transaccional

- Una oferta con transacciones en estado `Pendiente`, `En Proceso`, `Pagado` o `Disputa` **no puede ser modificada ni eliminada**.
- Solo ofertas en estado `Activa` pueden ser editadas o eliminadas.

### 9.4. Flujo de Ofertas y Gestión

- Una oferta en estado `"En Proceso"` se asocia a una sola transacción activa (relación 1:1).
- Las ofertas en estado `"Finalizada"` se ocultan en `MyOffersScreen`. Solo se muestran `"Activa"` y `"En Proceso"`.
- La edición/eliminación solo está permitida en estado `"Activa"`.
- El modal de edición contiene un botón de "Eliminar Oferta" con confirmación.

### 9.5. Ciclo de Transacciones (Doble Confirmación)

1. **Pendiente**: El comprador inicia seleccionando su método de recepción. Ambos ven cuentas bancarias cruzadas.
2. **Pagado / En Proceso**: Ambos realizan transferencias y suben comprobantes al bucket `vouchers`.
3. **Finalizado o Disputa**: Ambos confirman pago correcto (`confirmado_comprador`/`confirmado_vendedor`). Solo cuando ambos confirman → `Finalizado`. Cualquiera puede abrir disputa antes de confirmar.

### 9.6. Resolución de Disputas

- Exclusivo para administradores.
- **A favor del comprador**: Transacción → `Cancelado`, Oferta → `Activa`.
- **A favor del vendedor**: Transacción → `Finalizado`.

### 9.7. Calificación de Usuarios

- `calificacion` (1.00-5.00) se actualiza automáticamente cada vez que se inserta un nuevo registro en `calificaciones`.
- `ya_calificado` evita calificaciones duplicadas en una misma transacción.

### 9.8. Verificación de Vouchers con IA

- Al subir un voucher, se invoca la Edge Function `verificar-voucher-ia`.
- La IA extrae: banco emisor, cuenta origen/destino, monto, número de operación, fecha/hora.
- Si los datos coinciden con la transacción → se marca como `Pagado`.
- Si hay discrepancias → se marca para revisión manual.

### 9.9. Bloqueo y Suspensión de Usuarios

- Duración: 1 hora, 24h, 7 días, 1 mes, 3 meses o permanente (indefinido).
- Se actualiza `estado` a `"Bloqueado"`, incrementa `bloqueos_anteriores`, establece `bloqueado_hasta`.
- **Seguridad**: La verificación de bloqueo se realiza **después** de que la contraseña es correcta (protección contra enumeración de emails).
- **RLS restrictivas**: Políticas `AS RESTRICTIVE` que llaman a `es_usuario_activo()` en todas las tablas transaccionales.
- **Auto-recuperación**: Al iniciar sesión, si el plazo de bloqueo venció, se desbloquea automáticamente.

---

## 10. Flujo de Transacciones P2P

### 10.1. Inicio de Transacción

1. Comprador encuentra oferta en MarketPlaceScreen.
2. Tap → OfferDetailScreen: Ingresa monto a operar.
3. Validación: monto entre mínimo y máximo de la oferta.
4. Confirmación → Se crea transacción en estado `Pendiente`.
5. Redirección a TransactionStatusScreen.

### 10.2. Cuentas Bancarias Cruzadas

| Rol        | Cuenta Destino                        | Cuenta de Recepción                |
| ---------- | ------------------------------------- | ---------------------------------- |
| Comprador  | Cuenta del vendedor (donde enviar $) | Su propia cuenta configurada       |
| Vendedor   | Cuenta del comprador (donde enviar $)| Su propia cuenta en la oferta      |

### 10.3. Subida de Comprobantes

1. Usuario selecciona imagen de galería (ActivityResultContracts.GetContent).
2. La imagen se sube a Supabase Storage bucket `vouchers` con ruta `{transaccion_id}/{usuario_id}/{filename}`.
3. Se inserta registro en `comprobantes`.
4. Si el subidor es el comprador, se invoca Edge Function `verificar-voucher-ia`.
5. Si la IA valida → estado cambia a `Pagado`.

### 10.4. Confirmación Final

1. Confirmación del comprador: `confirmado_comprador = true`.
2. Confirmación del vendedor: `confirmado_vendedor = true`.
3. Cuando ambos confirman → `estado = "Finalizado"`.
4. Botón de confirmación habilitado solo si la contraparte subió su comprobante.

---

## 11. Sistema de Notificaciones

### 11.1. Firebase Cloud Messaging

- `MyFirebaseMessagingService` maneja tokens FCM y notificaciones entrantes.
- Los tokens se registran en el endpoint `/api/tokens/register` de Supabase (tabla `tokens_push`).
- Canales de notificación: `transacciones_channel`, `chat_channel`, `default`.

### 11.2. Eventos que Disparan Notificaciones (Server-Side Triggers)

| Evento                          | Destinatario     | Título                                  | Mensaje                                                      |
| ------------------------------- | ---------------- | --------------------------------------- | ------------------------------------------------------------ |
| INSERT transacciones            | Vendedor         | ¡Nueva solicitud de intercambio!        | [Comprador] quiere iniciar un intercambio contigo.           |
| INSERT mensajes_chat            | Contraparte      | Mensaje de [Remitente]                  | [Primeros 100 caracteres del mensaje]                        |
| UPDATE a 'Pagado'               | Vendedor         | Transacción Marcada como Pagada         | El comprador subió el comprobante. Confirma la recepción.    |
| UPDATE a 'Finalizado'           | Comprador        | Transacción Finalizada                  | El vendedor confirmó tu pago. ¡Intercambio completado!       |
| UPDATE a 'Disputa'              | Ambos            | Disputa Iniciada                        | La transacción #[id] ha entrado en disputa.                  |

### 11.3. Manejo en la App

- Al recibir la notificación, los datos extras (`transaccion_id`) permiten navegar directamente a la pantalla correspondiente.
- No se envían notificaciones push para fallos de verificación de IA (solo alertas in-app).

---

## 12. Integración con Supabase

### 12.1. Autenticación (Auth)

```kotlin
// Login
Supabase.client.auth.signInWith(Email) {
    email = correo
    password = contrasenia
}

// Registro
Supabase.client.auth.signUpWith(Email) {
    email = correo
    password = contrasenia
}

// Obtener usuario actual (seguro, espera inicialización)
Supabase.client.auth.currentUserAwaitInit()?.id

// Recuperación de contraseña
Supabase.client.auth.resetPasswordForEmail(email)

// Restablecer contraseña
Supabase.client.auth.updateUser {
    password = newPassword
}
```

### 12.2. Base de Datos (Postgrest)

```kotlin
// SELECT con filtros
Supabase.client.postgrest["ofertas"]
    .select {
        filter { eq("moneda", moneda) }
    }.decodeList<OfferModel>()

// INSERT
Supabase.client.postgrest["ofertas"]
    .insert(oferta) { select() }
    .decodeSingle<OfferModel>()

// UPDATE
Supabase.client.postgrest["ofertas"]
    .update({ set("monto_total", nuevoMonto) }) {
        filter { eq("oferta_id", ofertaId) }
    }

// DELETE
Supabase.client.postgrest["ofertas"]
    .delete {
        filter { eq("oferta_id", ofertaId) }
    }
```

### 12.3. Almacenamiento (Storage)

- **Bucket `vouchers`**: Comprobantes de pago, ruta `{transaccion_id}/{usuario_id}/{filename}`.
- **Bucket `kyc-documents`**: Documentos de identidad (DNI) para verificación KYC.
- **Bucket `avatars`**: Fotos de perfil de usuario.

```kotlin
// Subir archivo
bucket.upload(path = ruta, data = fileBytes) { upsert = true }

// Obtener URL pública
val url = bucket.publicUrl(ruta)
```

### 12.4. Tiempo Real (Realtime)

```kotlin
// Crear canal y escuchar inserts en mensajes_chat
val channel = Supabase.client.realtime.createChannel("chat_transaccion_$transaccionId")
channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
    table = "mensajes_chat"
    filter = "transaccion_id=eq.$transaccionId"
}
channel.join()
```

### 12.5. Edge Functions (Functions)

```kotlin
// Invocar verificación de voucher con IA
Supabase.client.functions.invoke<VerificarVoucherResponse>(
    function = "verificar-voucher-ia",
    body = VerificarVoucherRequest(comprobanteId, transaccionId)
)
```

---

## 13. Configuración de Gradle

### 13.1. Catálogo de Versiones (`gradle/libs.versions.toml`)

Versiones clave:

| Librería                          | Versión        |
| --------------------------------- | -------------- |
| Kotlin                            | 1.9.24         |
| AGP                               | 8.7.3          |
| Compose BOM                       | 2024.12.01     |
| Compose Compiler                  | 1.5.14         |
| Supabase Kotlin SDK (BOM)         | 3.0.2          |
| Koin                              | 4.0.1          |
| Coil                              | 2.7.0          |
| Firebase Messaging                | 24.1.0         |
| Kotlinx Serialization             | 1.6.3          |
| Ktor (para networking)            | 3.0.3          |

### 13.2. Plugins

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
}
```

### 13.3. Permisos en AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.CAMERA" />
```

---

## 14. UI y Temas

### 14.1. AppTheme.kt

- Utiliza Material Design 3 con slots de color personalizados:
  - `md_theme_light`: Tema claro con tonos azul-verde.
  - `md_theme_dark`: Tema oscuro con tonos azul-verde.
- Esquema de color dinámico Android 12+ desactivado (usa colores fijos).
- `darkMode` configurable desde SettingsScreen (Claro/Oscuro/Sistema).

### 14.2. Edge-to-Edge

- `enableEdgeToEdge()` en `MainActivity` para que la app se dibuje detrás de las barras del sistema.
- Padding manual en cada pantalla para evitar solapamiento con status bar y navigation bar.
- Sistema de padding centralizado usado en casi todas las pantallas con `Modifier.padding(top = statusBarDp.dp, bottom = navBarDp.dp)`.

### 14.3. RikkaUI

- Es el sistema de diseño principal: botones, tarjetas, textos, campos de texto.
- `ToastHost` se usa en todas las pantallas para mensajes temporales.
- `RikkaTheme` envuelve la aplicación.

---

## 15. Seguridad

### 15.1. Políticas RLS (Row Level Security) en Supabase

- **`AS RESTRICTIVE`**: Políticas restrictivas en tablas transaccionales (`ofertas`, `transacciones`, etc.) que llaman a `es_usuario_activo()`.
- La función `es_usuario_activo()` verifica que el usuario autenticado no esté bloqueado.
- Usuarios bloqueados no pueden leer ni escribir datos aunque tengan sesión activa en Auth.

### 15.2. Protección contra Enumeración de Emails

- El bloqueo se verifica **después** de que la contraseña sea correcta.
- Si la contraseña es incorrecta, solo se muestra error genérico de credenciales.

### 15.3. Manejo de Sesiones

- El token de acceso se almacena en SharedPreferences (no en DataStore).
- `currentUserAwaitInit()` previene condiciones de carrera al restaurar la sesión del caché de Supabase.
- Al cerrar sesión, se limpian todos los datos locales.

### 15.4. Subida de Seguridad

- Las imágenes deben ser JPG/PNG (< 5MB).
- Se utiliza el ActivityResultContracts para seleccionar archivos de manera segura.

---

## 16. Referencias de Base de Datos

### 16.1. Tablas PostgreSQL en Supabase

| Tabla               | Propósito                                    |
| ------------------- | -------------------------------------------- |
| `usuarios`          | Perfiles de usuario                          |
| `metodos_pago`      | Cuentas bancarias de usuarios                |
| `ofertas`           | Ofertas de intercambio P2P                   |
| `transacciones`     | Transacciones entre comprador y vendedor     |
| `comprobantes`      | Vouchers/comprobantes de pago subidos        |
| `mensajes_chat`     | Mensajes de chat en tiempo real              |
| `disputas`          | Disputas abiertas sobre transacciones        |
| `verificaciones_ia` | Resultados de verificación de IA en vouchers |
| `calificaciones`    | Calificaciones de usuarios por transacción   |
| `alertas_cambio`    | Alertas de tipo de cambio                    |
| `tokens_push`       | Tokens de Firebase Cloud Messaging           |

### 16.2. Buckets de Storage

| Bucket            | Propósito                          |
| ----------------- | ---------------------------------- |
| `vouchers`        | Comprobantes de pago               |
| `kyc-documents`   | Documentos de identidad (KYC)      |
| `avatars`         | Fotos de perfil                    |

### 16.3. Edge Functions

| Función                  | Propósito                                    |
| ------------------------ | -------------------------------------------- |
| `verificar-voucher-ia`   | Validación automática de comprobantes con IA |
| `send-push-notification` | Envío de notificaciones push transaccionales |

---

*Documentación generada a partir del código fuente y la configuración del proyecto. Última actualización: Julio 2026.*
