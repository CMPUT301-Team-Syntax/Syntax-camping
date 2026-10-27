package com.example.syntax_camping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.syntax_camping.screens.CampsiteOwnerScreen
import com.example.syntax_camping.screens.SyntaxCamping
import com.example.syntax_camping.ui.theme.SyntaxcampingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val db = Database()
        val deviceId = db.getDeviceId(this)

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
