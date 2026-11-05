package com.example.syntax_camping
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

//Singleton that's accessible everywhere to store the current user's id.

object CurrentUser {
    private var _deviceId: String = ""

    //Stores user Id. (To get the id, just use "CurrentUser.deviceId")
    val deviceId: String
        get() = _deviceId

    //Sets device Id. (To use this, do "CurrentUser.setDeviceId(some device Id")
    fun setDeviceId(id: String) {
        _deviceId = id
    }
}