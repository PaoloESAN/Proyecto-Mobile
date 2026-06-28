package com.paoloesan.proyectomobile.data.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.paoloesan.proyectomobile.MainActivity
import com.paoloesan.proyectomobile.data.Supabase
import com.paoloesan.proyectomobile.data.currentUserAwaitInit
import com.paoloesan.proyectomobile.data.model.UserProfileModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.put

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        registrarTokenEnSupabase(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"]
        val transaccionId = remoteMessage.data["transaccion_id"]
        val type = remoteMessage.data["type"]

        if (title != null && body != null) {
            mostrarNotificacionSistema(title, body, transaccionId, type)
        }
    }

    private fun mostrarNotificacionSistema(
        title: String,
        body: String,
        transaccionId: String?,
        type: String?
    ) {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "p2p_notifications_channel"

        val channel = NotificationChannel(
            channelId,
            "Mensajería Transaccional",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones instantáneas de transacciones P2P y chats activos"
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("transaccion_id", transaccionId)
            putExtra("type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.paoloesan.proyectomobile.R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        fun registrarTokenEnSupabase(token: String) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 1. Obtener la sesión segura (espera el caché)
                    val authId = Supabase.client.auth.currentUserAwaitInit()?.id ?: run {
                        android.util.Log.w(
                            "FCM_REGISTRATION",
                            "No hay sesión activa de Supabase Auth para registrar el token."
                        )
                        return@launch
                    }

                    // 2. Encontrar el usuario_id interno
                    val userProfile = Supabase.client.postgrest["usuarios"]
                        .select {
                            filter { eq("auth_id", authId) }
                        }.decodeSingleOrNull<UserProfileModel>()

                    val usuarioId = userProfile?.usuarioId ?: run {
                        android.util.Log.w(
                            "FCM_REGISTRATION",
                            "No se encontró el perfil de usuario para authId: $authId"
                        )
                        return@launch
                    }

                    // 3. Upsert del token en la base de datos como JsonObject
                    val payload = kotlinx.serialization.json.buildJsonObject {
                        put("usuario_id", usuarioId)
                        put("fcm_token", token)
                        put("dispositivo_info", "Android " + Build.VERSION.RELEASE)
                    }

                    Supabase.client.postgrest["tokens_push"].upsert(payload)
                    android.util.Log.i(
                        "FCM_REGISTRATION",
                        "Token FCM registrado exitosamente en Supabase: $token"
                    )
                } catch (e: Exception) {
                    android.util.Log.e(
                        "FCM_REGISTRATION",
                        "Error al registrar el token en la base de datos",
                        e
                    )
                }
            }
        }
    }
}
