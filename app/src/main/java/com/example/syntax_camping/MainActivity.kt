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

        Startup(db, deviceId)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SyntaxcampingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = deviceId,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Startup routine:
// Called by MainActivity. Takes reference to Database instance and the user's DeviceId

fun Startup(db: Database, devId: String) {

    db.userExistsByDeviceId(devId) { exists ->
        if (exists) {

            // Do whatever we need to do when a user already exists

        } else {

            // Create a new user and upload to database
            val aNewUser = User("New User", 0)
            db.createNewUser(devId, aNewUser)

        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {


    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SyntaxcampingTheme {
        Greeting("Android")
    }
}