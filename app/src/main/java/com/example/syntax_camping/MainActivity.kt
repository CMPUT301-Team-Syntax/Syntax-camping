package com.example.syntax_camping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.syntax_camping.screens.CampsiteOwnerScreen
import com.example.syntax_camping.screens.SyntaxCamping
import com.example.syntax_camping.ui.theme.SyntaxcampingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        /**Ask user for name and account type, and then initialize User val with it.
         *we'll have to connect the register screen to this.
         *var name by remember { mutableStateOf("") }
         *var userId by remember { mutableIntStateOf(0) }
         *var location by remember { mutableStateOf("") }
         *val user = User(name, userId)
         */

        val db = Database()
        val deviceId = db.getDeviceId(this)
        //For now, I just have a test user.
        val testUser = User("HelloWorld", 0, "Alberta")

        //See if user already has account, then create it if not.
        db.userExistsByDeviceId(deviceId) { exists ->
            if (exists) {
                println("Welcome back, User!")
            }
            else {
                db.createNewUser(deviceId, testUser)
            }
        }

        var campsiteRepository: CampsiteRepository

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SyntaxcampingTheme {

                val isCampsiteOwner = false // change this if you want to test a different screen

                if (isCampsiteOwner) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        CampsiteOwnerScreen((Modifier.padding(innerPadding)))
                    }
                }

                else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        SyntaxCamping((Modifier.padding(innerPadding))) }
                }


            }
        }
    }
}
