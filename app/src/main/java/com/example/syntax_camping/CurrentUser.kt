package com.example.syntax_camping
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

//Singleton that's accessible everywhere to store the current user's id.

object CurrentUser {
    private var _deviceId: String = ""
    val deviceId: String
        get() = _deviceId

    fun setDeviceId(id: String) {
        _deviceId = id
    }
}