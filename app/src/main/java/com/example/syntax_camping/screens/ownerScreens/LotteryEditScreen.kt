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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.screens.CustomBottomBar
import com.example.syntax_camping.screens.TopBar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.Database
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.screens.filterOwnedCampsites


@Composable
fun LotteryEditScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    editingLottery: Lottery?
) {
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
    val selectedCampsites = remember { mutableStateListOf<Campsite>() }
    val db = Database()

    LaunchedEffect(editingLottery?.lotteryId) {
        if (editingLottery != null) {
            numWinners = editingLottery.numWinners.toString()

            val startCal = java.util.Calendar.getInstance().apply { timeInMillis = editingLottery.endTimeLottery }
            startDay = startCal.get(java.util.Calendar.DAY_OF_MONTH).toString()
            startMonth = (startCal.get(java.util.Calendar.MONTH) + 1).toString()
            startYear = startCal.get(java.util.Calendar.YEAR).toString()
            startHour = startCal.get(java.util.Calendar.HOUR_OF_DAY).toString()
            startMinute = startCal.get(java.util.Calendar.MINUTE).toString()

            val finalCal = java.util.Calendar.getInstance().apply { timeInMillis = editingLottery.endTimeBooking }
            finalDay = finalCal.get(java.util.Calendar.DAY_OF_MONTH).toString()
            finalMonth = (finalCal.get(java.util.Calendar.MONTH) + 1).toString()
            finalYear = finalCal.get(java.util.Calendar.YEAR).toString()
            finalHour = finalCal.get(java.util.Calendar.HOUR_OF_DAY).toString()
            finalMinute = finalCal.get(java.util.Calendar.MINUTE).toString()

            selectedCampsites.clear()
            selectedCampsites.addAll(editingLottery.campsites)


        } else {
            selectedCampsites.clear()
        }
    }

    val myOwnedCampsites = filterOwnedCampsites(
        CampsiteRepository.getCampsites(),
        userViewModel.currentUser?.ownerData
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4E8D5)),
        containerColor = Color(0xFFF4E8D5),
        topBar = {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                TextField(
                    value = numWinners,
                    onValueChange = { numWinners = it },
                    label = { Text("Number of Winners/Spots Available") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            CampsiteSelectorDropdown(
                myOwnedCampsites = myOwnedCampsites,
                selectedCampsites = selectedCampsites
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red
                )
            }

            Button(
                onClick = {
                    val (isSuccess, errorText) = validateAndCreateLottery(
                        startDay, startMonth, startYear, startHour, startMinute,
                        finalDay, finalMonth, finalYear, finalHour, finalMinute, numWinners,
                        selectedCampsites
                    )

                    if (isSuccess) {
                        errorMessage = ""
                        navController.popBackStack()
                    } else {
                        errorMessage = errorText
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Lottery")
            }
        }
    }
}