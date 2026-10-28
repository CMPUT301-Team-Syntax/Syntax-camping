package com.example.syntax_camping.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.R

@Composable
fun TopBar(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFFE2AFAF)), // sets the background color to pink lkie the figma mockups
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        //icon button for back button, template for adding icons
        IconButton(

            onClick = { /* TODO: Navigate to notifications */ },
            modifier = Modifier.size(100.dp) // the size is defaulted to 44.dp which is pretty small so you can change this if needed for other icons

        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "back button",
                tint = Color.Unspecified  //without it the icon would be black
            )
        }

        IconButton(onClick = { /* TODO: Navigate to notifications */ },
            modifier = Modifier.size(100.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.notification),
                contentDescription = "notifications",
                tint = Color.Unspecified
            )
        }
    }
}