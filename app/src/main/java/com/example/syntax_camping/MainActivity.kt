package com.example.syntax_camping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.syntax_camping.ui.theme.SyntaxcampingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val db = Database()
        val deviceId = db.getDeviceId(this)

//        Startup(db, deviceId)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SyntaxcampingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SyntaxCamping(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
