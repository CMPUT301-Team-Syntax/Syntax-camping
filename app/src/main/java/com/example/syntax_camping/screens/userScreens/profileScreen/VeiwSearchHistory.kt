package com.example.syntax_camping.screens.userScreens.profileScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun VeiwSearchHistory(history: List<String>, onWordClick: (String) -> Unit,navController: NavController)  {
    LazyColumn{
        items(history.size) { word ->
            Text(
                text = history[word],
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onWordClick(history[word]) }
            )

        }

    }

}