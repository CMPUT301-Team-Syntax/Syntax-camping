package com.example.syntax_camping.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.syntax_camping.R


// will figure this implementation out after midterm
@Composable
fun BottomBar(navController: NavController) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0xFF3E6B68))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = { /* TODO: Navigate to Search */ },
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = Color(0xFF3F6B68)
            )
        }

        IconButton(
            onClick = { /* TODO: Navigate to Reservations */ },
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.reservations),
                contentDescription = "Reservations",
                tint = Color.Unspecified
            )
        }

        IconButton(
            onClick = { /* TODO: Navigate to Settings */ },
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.account),
                contentDescription = "Account",
                tint = Color.Unspecified
            )
        }

        IconButton(
            onClick = { /* TODO: Navigate to Settings */ },
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "Settings",
                tint = Color.Unspecified,
            )
        }

    }
}