package com.example.syntax_camping

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.syntax_camping.dataClasses.User

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val database = Database()

    val deviceId: String = database.getDeviceId(application)
    var currentUser by mutableStateOf<User?>(null)
        private set  //can only change current user through the refreshCurrentUser function

    init {
        refreshCurrentUser()
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
