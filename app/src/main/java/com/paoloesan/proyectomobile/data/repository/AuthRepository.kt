package com.paoloesan.proyectomobile.data.repository

import android.content.Context
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.local.SessionManager
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object AuthRepository {

    /**
     * Inicia sesión con Supabase Auth y carga el perfil del usuario
     * desde la tabla `public.usuarios` usando el auth_id (UUID).
     *
     * @return [UserProfileModel] con los datos del usuario autenticado
     * @throws Exception si las credenciales son incorrectas o hay error de red
     */
    suspend fun login(context: Context, correo: String, password: String, rememberMe: Boolean): UserProfileModel {
        // 1. Autenticar con Supabase Auth
        Supabase.client.auth.signInWith(Email) {
            email = correo
            this.password = password
        }

        // 2. Obtener el UUID del usuario autenticado
        val authId = Supabase.client.auth.currentUserOrNull()!!.id

        // 3. Buscar el perfil en public.usuarios usando auth_id
        var perfil = Supabase.client.postgrest["usuarios"]
            .select {
                filter {
                    eq("auth_id", authId)
                }
            }
            .decodeSingle<UserProfileModel>()

        // 4. Verificar si el usuario está bloqueado
        if (perfil.estado == "Bloqueado") {
            val bloqueadoHastaInstant = perfil.bloqueadoHasta?.let {
                try {
                    Instant.parse(it)
                } catch (e: Exception) {
                    null
                }
            }

            if (bloqueadoHastaInstant != null && !bloqueadoHastaInstant.isAfter(Instant.now())) {
                // El bloqueo ya expiró. Desbloquear automáticamente.
                Supabase.client.postgrest["usuarios"]
                    .update({
                        set("estado", "Activo")
                        set("bloqueado_hasta", null as String?)
                    }) {
                        filter {
                            eq("usuario_id", perfil.usuarioId!!)
                        }
                    }
                perfil = perfil.copy(estado = "Activo", bloqueadoHasta = null)
            } else {
                // Sigue bloqueado (indefinido o en el futuro)
                Supabase.client.auth.signOut()
                val mensaje = if (bloqueadoHastaInstant != null) {
                    val localDateTime = LocalDateTime.ofInstant(bloqueadoHastaInstant, ZoneId.systemDefault())
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    "Su cuenta está bloqueada temporalmente hasta ${localDateTime.format(formatter)}."
                } else {
                    "Su cuenta ha sido bloqueada indefinidamente."
                }
                throw Exception(mensaje)
            }
        }

        // 5. Persistir sesión localmente
        SessionManager.saveRememberMe(context, rememberMe)
        SessionManager.saveToken(context, authId)
        SessionManager.saveProfileInfo(context, perfil.nombres, perfil.apellidos)

        // Sincronizar Token de Firebase Cloud Messaging
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    com.paoloesan.proyectomobile.data.local.MyFirebaseMessagingService.registrarTokenEnSupabase(token)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return perfil
    }

    /**
     * Registra un nuevo usuario en Supabase Auth y crea su perfil
     * correspondiente en la tabla `public.usuarios` de la base de datos.
     */
    suspend fun register(nombres: String, apellidos: String, correo: String, contrasenia: String) {
        // 1. Crear el usuario en Supabase Auth enviando los nombres y apellidos como metadatos
        Supabase.client.auth.signUpWith(Email) {
            email = correo
            password = contrasenia
            data = buildJsonObject {
                put("nombres", nombres)
                put("apellidos", apellidos)
            }
        }

        // No es necesario insertar manualmente en la tabla `usuarios` porque el trigger
        // `on_auth_user_created` de base de datos se encarga de crear el perfil de forma
        // automática a partir de los metadatos 'nombres' y 'apellidos'.
    }

    /**
     * Cierra la sesión activa en Supabase Auth y limpia la sesión local.
     */
    suspend fun logout(context: Context) {
        Supabase.client.auth.signOut()
        SessionManager.clearToken(context)
    }
}
