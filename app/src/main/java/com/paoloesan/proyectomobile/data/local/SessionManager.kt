package com.paoloesan.proyectomobile.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

object SessionManager {

    private const val PREFS_NAME = "proyectomobile_session"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_VERIFIED = "user_verified"
    private const val KEY_NOMBRES = "user_nombres"
    private const val KEY_APELLIDOS = "user_apellidos"
    private const val KEY_REMEMBER_ME = "remember_me"

    private val _themeState = mutableStateOf("system")
    val themeState: State<String> = _themeState

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveRememberMe(context: Context, remember: Boolean) {
        prefs(context).edit().putBoolean(KEY_REMEMBER_ME, remember).apply()
    }

    fun shouldRememberMe(context: Context): Boolean =
        prefs(context).getBoolean(KEY_REMEMBER_ME, false)

    fun initSession(context: Context) {
        if (!shouldRememberMe(context)) {
            clearToken(context)
            prefs(context).edit()
                .remove(KEY_NOMBRES)
                .remove(KEY_APELLIDOS)
                .remove(KEY_VERIFIED)
                .apply()
        }
    }

    fun saveToken(context: Context, token: String) {
        prefs(context).edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? =
        prefs(context).getString(KEY_TOKEN, null)

    fun clearToken(context: Context) {
        prefs(context).edit().remove(KEY_TOKEN).apply()
    }

    fun isLoggedIn(context: Context): Boolean =
        getToken(context) != null

    fun initTheme(context: Context) {
        _themeState.value = getTheme(context)
    }

    fun saveTheme(context: Context, theme: String) {
        prefs(context).edit().putString(KEY_THEME, theme).apply()
        _themeState.value = theme
    }

    fun getTheme(context: Context): String =
        prefs(context).getString(KEY_THEME, "system") ?: "system"

    fun saveVerified(context: Context, isVerified: Boolean) {
        prefs(context).edit().putBoolean(KEY_VERIFIED, isVerified).apply()
    }

    fun isVerified(context: Context): Boolean =
        prefs(context).getBoolean(KEY_VERIFIED, false)

    fun saveProfileInfo(context: Context, nombres: String, apellidos: String) {
        prefs(context).edit()
            .putString(KEY_NOMBRES, nombres)
            .putString(KEY_APELLIDOS, apellidos)
            .apply()
    }

    fun getNombres(context: Context): String =
        prefs(context).getString(KEY_NOMBRES, "Freddy") ?: "Freddy"

    fun getApellidos(context: Context): String =
        prefs(context).getString(KEY_APELLIDOS, "Delgado") ?: "Delgado"
}

