package com.paoloesan.proyectomobile.data.repository

import android.content.Context
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.local.SessionManager
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

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
        val perfil = Supabase.client.postgrest["usuarios"]
            .select {
                filter {
                    eq("auth_id", authId)
                }
            }
            .decodeSingle<UserProfileModel>()

        // 4. Persistir sesión localmente
        SessionManager.saveRememberMe(context, rememberMe)
        SessionManager.saveToken(context, authId)
        SessionManager.saveProfileInfo(context, perfil.nombres, perfil.apellidos)

        return perfil
    }

    /**
     * Cierra la sesión activa en Supabase Auth y limpia la sesión local.
     */
    suspend fun logout(context: Context) {
        Supabase.client.auth.signOut()
        SessionManager.clearToken(context)
    }
}
