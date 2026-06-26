package com.paoloesan.proyectomobile.data.local

import android.content.Context

object SessionManager {
    private const val PREFS_NAME = "session_prefs"
    private const val KEY_VERIFIED = "identity_verified"

    fun saveVerified(context: Context, verified: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_VERIFIED, verified)
            .apply()
    }

    fun isVerified(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_VERIFIED, false)
    }
}
