package com.example.syntax_camping

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.syntax_camping.dataClasses.EntrantData
import com.example.syntax_camping.dataClasses.OwnerData
import com.example.syntax_camping.dataClasses.User
import com.example.syntax_camping.screens.SyntaxCamping
import com.example.syntax_camping.ui.theme.SyntaxcampingTheme
import kotlin.collections.emptyList
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        /**Ask user for name and account type, and then initialize User val with it.
         *we'll have to connect the register screen to this.
         *var name by remember { mutableStateOf("") }
         *var userId by remember { mutableIntStateOf(0) }
         *val user = User(name, userId)
         */



        CampsiteRepository.refreshCampsiteCache { success ->
            if (success) {
                Log.d("MainActivity", "Successfully loaded Campsites from database!")
            } else {
                Log.e("MainActivity", "Could not fetch Campsites from database!")
            }
        }


        // get deviceId from android

        val db = Database()
        val deviceId = db.getDeviceId(this)


        // For now, I just have a test user.
        val testEntrantData = EntrantData(mutableListOf(), mutableListOf())
        val testOwnerData = OwnerData(mutableListOf(), mutableListOf())
        val testUser = User(deviceId, "HelloWorld", "sorachat@ualberta.ca", "7802349700", testEntrantData, testOwnerData)

        // See if user already has account (by deviceId), then create it if not.
        db.userExistsByDeviceId(deviceId) { exists ->
            if (exists) {
                println("Welcome back, User!")
            }
            else {
                db.createNewUser(deviceId, testUser) { result ->
                    if (result.isSuccess) {
                        println("User created!")
                    } else {
                        println("Error: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userViewModel: UserViewModel = viewModel()
            Log.d("UserDebug", "User saved in view model: ${userViewModel.deviceId}")

            SyntaxcampingTheme {
                //we need something to differentiate campsite user and campsite owner? we neeed a create account screen
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        SyntaxCamping(
                            (Modifier.padding(innerPadding)),
                            userViewModel
                        )


            }
        }
    }
}
}