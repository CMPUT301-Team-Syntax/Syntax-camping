package com.example.syntax_camping

import android.app.ProgressDialog.show
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Horizontal
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun Notification(title: String, body: String) {

    Column() {

        Row(
            // modifier = Modifier
        ) {

            Text(title)

        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            Text(body)

        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {

            Text("1m ago")

        }

    }

}

@Preview(showBackground = true)
@Composable
private fun NotificationPreview() {

    Notification("New notification", "Example Notification Body")

}