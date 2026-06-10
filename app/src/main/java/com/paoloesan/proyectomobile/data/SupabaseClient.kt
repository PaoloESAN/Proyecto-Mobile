package com.paoloesan.proyectomobile.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.storage.Storage

/**
 * Cliente singleton de Supabase configurado con los módulos de Postgrest (Base de Datos),
 * Auth (Autenticación) y Storage (Almacenamiento en Buckets).
 */
object Supabase {
    const val URL = "https://kakfuayeosauhtzhbotf.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imtha2Z1YXllb3NhdWh0emhib3RmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk2NjY4NTAsImV4cCI6MjA5NTI0Mjg1MH0.axjjd1xpVPiLGufxkP3UUNyeuUoFN5X8OUUjXaUYYR4"

    val client = createSupabaseClient(
        supabaseUrl = URL,
        supabaseKey = ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
    }
}
