package com.paoloesan.proyectomobile.data.local

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREFS_NAME = "proyectomobile_session"
    private const val KEY_TOKEN = "auth_token"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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
}
