package com.example.syntax_camping

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.syntax_camping.dataClasses.LotteryEntry
import com.example.syntax_camping.dataClasses.NotificationData
import com.example.syntax_camping.dataClasses.User
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val database = Database()

    val deviceId: String = database.getDeviceId(application)
    internal val notificationInbox = NotificationInbox(application, deviceId)
    private val joiningLotteries = mutableSetOf<String>()
    var currentUser by mutableStateOf<User?>(null)
        private set  //can only change current user through the refreshCurrentUser function

    init {
        refreshCurrentUser()
    }

    /** Displays the database result and saves it in the notification inbox. */
    fun showNotification(notification: NotificationData) {
        viewModelScope.launch { notificationInbox.showNotification(notification) }
    }

    internal fun joinLottery(lotteryId: String, campsiteId: String? = null, onComplete: (Boolean) -> Unit) {
        if (!joiningLotteries.add(lotteryId)) {
            onComplete(false)
            return
        }
        val entry = LotteryEntry(deviceId, Timestamp.now())
        database.registerLotteryEntry(lotteryId, entry, campsiteId) { notification ->
            joiningLotteries.remove(lotteryId)
            notification?.let(::showNotification)
            onComplete(notification != null)
        }
    }

    fun refreshCurrentUser() {
        database.fetchUserDetails(deviceId) { result ->
            result.onSuccess { user ->
                currentUser = user
                Log.d("UserDebug", "Successfully fetched current user")
            }.onFailure { error ->
                Log.e("UserDebug", "Failed to fetch user", error)
            }
        }
    }
}
