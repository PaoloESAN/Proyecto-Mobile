package com.paoloesan.proyectomobile.data

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.functions.Functions
import io.ktor.client.plugins.HttpTimeout
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

/**
 * Cliente singleton de Supabase configurado con los módulos de Postgrest (Base de Datos),
 * Auth (Autenticación), Storage (Almacenamiento en Buckets), Realtime y Functions.
 */
object Supabase {
    val URL = com.paoloesan.proyectomobile.BuildConfig.SUPABASE_URL
    val ANON_KEY = com.paoloesan.proyectomobile.BuildConfig.SUPABASE_ANON_KEY

    @OptIn(SupabaseInternal::class)
    val client = createSupabaseClient(
        supabaseUrl = URL,
        supabaseKey = ANON_KEY
    ) {
        httpConfig {
            install(HttpTimeout) {
                requestTimeoutMillis = 60000L  // 60 segundos
                connectTimeoutMillis = 15000L  // 15 segundos
                socketTimeoutMillis = 60000L   // 60 segundos
            }
        }
        install(Postgrest)
        install(Auth)
        install(Storage)
        install(Realtime)
        install(Functions)
    }
}

/**
 * Retorna el usuario actual de Supabase Auth una vez completada la inicialización
 * asíncrona de la sesión en caché.
 */
suspend fun Auth.currentUserAwaitInit() = this.run {
    sessionStatus.filter { it !is SessionStatus.Initializing }.first()
    currentUserOrNull()
}

