package com.example.syntax_camping

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.syntax_camping.dataClasses.NotificationData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val deviceId = Database().getDeviceId(applicationContext)
        saveFcmToken(deviceId, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: return
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: return

        val deviceId = Database().getDeviceId(applicationContext)
        if (deviceId.isNotBlank()) {
            val id = remoteMessage.messageId ?: System.currentTimeMillis().toString()
            NotificationInbox(applicationContext, deviceId)
                .showNotification(NotificationData.Message(id, title, body))
        }

        showSystemNotification(title, body)
    }

    private fun showSystemNotification(title: String, body: String) {
        createNotificationChannel(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "default_notifications"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Lottery & Reservation Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for lottery wins, waitlists, and reservation updates"
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            }
        }

        fun syncCurrentToken(context: Context, deviceId: String) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    Log.d("FCM", "Fetched FCM token: $token")
                    saveFcmToken(deviceId, token)
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Failed to get FCM token", e)
                }
        }

        fun saveFcmToken(deviceId: String, token: String) {
            if (deviceId.isBlank() || token.isBlank()) return

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(deviceId)
                .set(
                    mapOf("fcmTokens" to FieldValue.arrayUnion(token)),
                    SetOptions.merge()
                )
                .addOnSuccessListener {
                    Log.d("FCM", "Saved FCM token to users/$deviceId")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Failed to save FCM token to Firestore", e)
                }
        }
    }
}