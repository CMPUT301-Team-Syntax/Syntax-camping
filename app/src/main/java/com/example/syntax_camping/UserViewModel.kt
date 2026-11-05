package com.example.syntax_camping

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val database = Database()

    val deviceId: String = database.getDeviceId(application)

}