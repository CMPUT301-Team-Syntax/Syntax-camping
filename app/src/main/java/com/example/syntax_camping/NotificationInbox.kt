package com.example.syntax_camping

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.syntax_camping.dataClasses.NotificationData
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal data class AppNotification(val id: String, val title: String, val message: String)

internal class NotificationInbox(context: Context, userId: String) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences("notifications_$userId", Context.MODE_PRIVATE)

    var notifications by mutableStateOf(loadNotifications())
        private set

    var hasUnread by mutableStateOf(preferences.getBoolean("unread", false))
        private set

    var banner by mutableStateOf<AppNotification?>(null)
        private set

    fun showNotification(data: NotificationData) {
        if (notifications.any { it.id == data.id }) return

        val notification = when (data) {
            is NotificationData.LotteryJoined -> registrationNotification(data)
            is NotificationData.Message -> AppNotification(data.id, data.title, data.message)
        }
        val updated = listOf(notification) + notifications
        val stored = JSONArray()
        for (item in updated) {
            stored.put(JSONObject().put("id", item.id).put("title", item.title).put("message", item.message))
        }
        preferences.edit().putString("updates", stored.toString()).putBoolean("unread", true).apply()
        notifications = updated
        hasUnread = true
        banner = notification
    }

    fun dismissBanner(id: String) {
        if (banner?.id == id) banner = null
    }

    fun markRead() {
        preferences.edit().putBoolean("unread", false).apply()
        hasUnread = false
    }

    private fun loadNotifications(): List<AppNotification> {
        val stored = preferences.getString("updates", null) ?: return emptyList()
        val data = try {
            JSONArray(stored)
        } catch (_: JSONException) {
            return emptyList()
        }
        return (0 until data.length()).mapNotNull { index ->
            val item = data.optJSONObject(index) ?: return@mapNotNull null
            val id = item.optString("id").takeIf(String::isNotBlank) ?: return@mapNotNull null
            if (item.has("title") && item.has("message")) {
                return@mapNotNull AppNotification(id, item.optString("title"), item.optString("message"))
            }
            val joinedWaitlist = when (item.optString("registration")) {
                "LOTTERY" -> false
                "WAITLIST" -> true
                else -> return@mapNotNull null
            }
            registrationNotification(NotificationData.LotteryJoined(id, item.optString("subject"), joinedWaitlist))
        }
    }

    private fun registrationNotification(data: NotificationData.LotteryJoined): AppNotification {
        val title = if (data.subject.isBlank()) {
            appContext.getString(
                if (data.joinedWaitlist) R.string.notification_waitlist_joined else R.string.notification_lottery_joined
            )
        } else {
            appContext.getString(
                if (data.joinedWaitlist) R.string.notification_waitlist_joined_for else R.string.notification_lottery_joined_for,
                data.subject
            )
        }
        val message = appContext.getString(
            if (data.joinedWaitlist) R.string.notification_waitlist_joined_body else R.string.notification_lottery_joined_body
        )
        return AppNotification(data.id, title, message)
    }
}
