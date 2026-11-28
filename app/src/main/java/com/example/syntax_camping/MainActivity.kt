package com.example.syntax_camping

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.syntax_camping.dataClasses.EntrantData
import com.example.syntax_camping.dataClasses.OwnerData
import com.example.syntax_camping.dataClasses.User
import com.example.syntax_camping.screens.NotificationBannerHost
import com.example.syntax_camping.screens.SyntaxCamping
import com.example.syntax_camping.screens.tabs.LocalHasUnreadNotifications
import com.example.syntax_camping.ui.theme.SyntaxcampingTheme

var globalDeviceId = ""

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**Ask user for name and account type, and then initialize User val with it.
         *we'll have to connect the register screen to this.
         *var name by remember { mutableStateOf("") }
         *var userId by remember { mutableIntStateOf(0) }
         *val user = User(name, userId)
         */

        CampsiteRepository.refreshCampsiteCache { success ->
            if (success) {
                Log.d("CampsiteRepository", "Successfully loaded Campsites from database!")
            } else {
                Log.e("CampsiteRepository", "Could not fetch Campsites from database!")
            }
        }

        // get deviceId from android
        val db = Database()
        val deviceId = db.getDeviceId(this) // "907ab032fed2e813"
        globalDeviceId = deviceId

        Log.d("devId", deviceId)

        // Create notification channel & ask for permission on Android 13+
        AppFirebaseMessagingService.createNotificationChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        ConversationRepository.refreshConversationCache(
            deviceId,
            onError = {
                Log.e("ConversationRepository", "Couldn't fetch conversations for user ${deviceId}!")
            },
            onSuccess = {
                Log.d("ConversationRepository", "Loaded conversations!")
            }
        )

        // For now, I just have a test user.
        val testEntrantData = EntrantData(
            mutableListOf(),
            mutableListOf(),
            mutableListOf()
        )
        val testOwnerData = OwnerData(
            mutableListOf(),
            mutableListOf()
        )
        val testUser = User(
            deviceId,
            "Default",
            "Default@gmail.com",
            "1234567890",
            testEntrantData,
            testOwnerData
        )

        // See if user already has account (by deviceId), then create it if not, and sync FCM token
        db.userExistsByDeviceId(deviceId) { exists ->
            if (exists) {
                println("Welcome back, User!")
                AppFirebaseMessagingService.syncCurrentToken(this, deviceId)
            } else {
                db.createNewUser(deviceId, testUser) { result ->
                    if (result.isSuccess) {
                        println("User created!")
                        AppFirebaseMessagingService.syncCurrentToken(this, deviceId)
                    } else {
                        println("Error: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            val userViewModel: UserViewModel = viewModel()
            Log.d("UserDebug", "User saved in view model: ${userViewModel.deviceId}")

            SyntaxcampingTheme {
                //we need something to differentiate campsite user and campsite owner? we neeed a create account screen
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CompositionLocalProvider(
                        LocalHasUnreadNotifications provides userViewModel.notificationInbox.hasUnread
                    ) {
                        NotificationBannerHost(
                            inbox = userViewModel.notificationInbox,
                            modifier = Modifier.fillMaxSize().padding(innerPadding)
                        ) {
                            SyntaxCamping(Modifier.fillMaxSize(), userViewModel)
                        }
                    }
                }
            }
        }
    }
}