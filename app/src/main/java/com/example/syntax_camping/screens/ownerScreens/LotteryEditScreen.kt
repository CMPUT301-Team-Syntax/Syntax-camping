package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.syntax_camping.Campsite
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.Color
import com.example.syntax_camping.screens.CustomBottomBar
import com.example.syntax_camping.screens.TopBar


@Composable
fun LotteryEditScreen(navController: NavController) {
    var startDay by remember { mutableStateOf("") }
    var startMonth by remember { mutableStateOf("") }
    var startYear by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf("") }
    var startMinute by remember { mutableStateOf("") }

    var finalDay by remember { mutableStateOf("") }
    var finalMonth by remember { mutableStateOf("") }
    var finalYear by remember { mutableStateOf("") }
    var finalHour by remember { mutableStateOf("") }
    var finalMinute by remember { mutableStateOf("") }
    var numWinners by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }

    val testCamp = remember {
        Campsite(
            "Test12",
            "Name",
            "Lop",
            "This is a description",
            10,
            mutableListOf(),
            rating = 4.5
        )
    }

    val campsites = remember { mutableListOf<Campsite>(testCamp) }

    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4E8D5)), // Set the background color to white
        containerColor = Color(0xFFF4E8D5),
        topBar = { // top bar with back button and notification icon
            TopBar(navController)
        },

        bottomBar = {
            CustomBottomBar(
                navController = navController,
                currentRoute = "CampsiteDetails",
                backgroundColor = Color(0xFF3E6B68)
            )
        }
    ) { innerPadding ->

    Column (
        modifier = Modifier
            .fillMaxSize().padding(16.dp)
            .padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage,
                color = androidx.compose.ui.graphics.Color.Red)
        }

        // --- Start Date Sections ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Start Date and Time")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = startDay,
                    onValueChange = { startDay = it },
                    label = { Text("Day") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = startMonth,
                    onValueChange = { startMonth = it },
                    label = { Text("Month") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                TextField(
                    value = startYear,
                    onValueChange = { startYear = it },
                    label = { Text("Year") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                TextField(
                    value = startHour,
                    onValueChange = { startHour = it },
                    label = { Text("Hour") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = startMinute,
                    onValueChange = { startMinute = it },
                    label = { Text("Minute") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

            }
        }

        // --- Final Date Sections ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Final Date and Time")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = finalDay,
                    onValueChange = { finalDay = it },
                    label = { Text("Day") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = finalMonth,
                    onValueChange = { finalMonth = it },
                    label = { Text("Month") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = finalYear,
                    onValueChange = { finalYear = it },
                    label = { Text("Year") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = finalHour,
                    onValueChange = { finalHour = it },
                    label = { Text("Hour") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = finalMinute,
                    onValueChange = { finalMinute = it },
                    label = { Text("Minute") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }

            TextField(
                value = numWinners,
                onValueChange = { numWinners = it },
                label = { Text("Number of Winners/Spots Available") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Button(
            onClick = {
                // Call your separated functional validator!
                val (isSuccess, errorText) = validateAndCreateLottery(
                    startDay, startMonth, startYear, startHour, startMinute,
                    finalDay, finalMonth, finalYear, finalHour, finalMinute, numWinners,
                    campsites
                )

                if (isSuccess) {
                    errorMessage = ""
                    navController.popBackStack() // Smooth navigation fallback
                } else {
                    errorMessage = errorText // Surface error message safely to UI
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Lottery")
        }
    }
}
}

