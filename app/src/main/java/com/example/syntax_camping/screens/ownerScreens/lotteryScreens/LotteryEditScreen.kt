package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import com.example.syntax_camping.screens.ownerScreens.lotteryScreens.validateAndCreateLottery
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.example.syntax_camping.screens.ownerScreens.LotteryEditScreen
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
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.testTag
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.Database
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.screens.filterOwnedCampsites
import com.example.syntax_camping.screens.ownerScreens.ownerCampsiteList.CampsiteSelectorDropdown
import java.util.Calendar
import com.example.syntax_camping.dataClasses.DateManager
import com.example.syntax_camping.dataClasses.Date
import com.example.syntax_camping.dataClasses.DateRepository
import com.example.syntax_camping.screens.CalenderVeiw
import com.example.syntax_camping.screens.ownerScreens.lotteryScreens.BOOKING_LENGTH_UNIT_MS


@Composable
fun LotteryEditScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    editingLottery: Lottery?,
    preselectedCampsite: Campsite? = null
) {
    var name by remember { mutableStateOf("") }
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
    var capacity by remember { mutableStateOf("") }
    var lotteryDates by remember { mutableStateOf<List<String>>(emptyList()) }
    var bookingLength by remember { mutableStateOf("") }
    var dateManager by remember{mutableStateOf<DateManager?>(null) }
    val selectedDatesString = remember { mutableStateListOf<String>() }
    val selectedDatesAsDate = remember { mutableStateListOf<Date>() }
    val db = Database()
    var newDate by remember { mutableStateOf<Date?>(null) }
    val drp = DateRepository()

    LaunchedEffect(editingLottery?.lotteryId) {
        if (editingLottery != null) {
            numWinners = editingLottery.numWinners.toString()
            name = editingLottery.name
            capacity = editingLottery.capacity.toString()
            lotteryDates = editingLottery.lotteryDates

            val startCal = Calendar.getInstance().apply {
                timeInMillis = if (editingLottery.startTime > 0) editingLottery.startTime else System.currentTimeMillis()
            }
            startDay = startCal.get(Calendar.DAY_OF_MONTH).toString()
            startMonth = (startCal.get(Calendar.MONTH) + 1).toString()
            startYear = startCal.get(Calendar.YEAR).toString()
            startHour = startCal.get(Calendar.HOUR_OF_DAY).toString()
            startMinute = startCal.get(Calendar.MINUTE).toString()

            val finalCal = Calendar.getInstance().apply { timeInMillis = editingLottery.endTimeLottery }
            bookingLength = ((editingLottery.endTimeBooking - editingLottery.endTimeLottery) / BOOKING_LENGTH_UNIT_MS).toString()
            finalDay = finalCal.get(Calendar.DAY_OF_MONTH).toString()
            finalMonth = (finalCal.get(Calendar.MONTH) + 1).toString()
            finalYear = finalCal.get(Calendar.YEAR).toString()
            finalHour = finalCal.get(Calendar.HOUR_OF_DAY).toString()
            finalMinute = finalCal.get(Calendar.MINUTE).toString()

            selectedCampsites.clear()
            selectedCampsites.addAll(editingLottery.campsites)


        }  else {
        selectedCampsites.clear()
        // CHANGED: a new lottery started from a campsite's page begins with that campsite already in the list
        preselectedCampsite?.let { selectedCampsites.add(it) }
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
                Text("Lottery Opens Date and Time")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryName"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = startDay,
                        onValueChange = { startDay = it },
                        label = { Text("Day") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryStartDay"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = startMonth,
                        onValueChange = { startMonth = it },
                        label = { Text("Month") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryStartMonth"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    TextField(
                        value = startYear,
                        onValueChange = { startYear = it },
                        label = { Text("Year") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryStartYear"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = startHour,
                        onValueChange = { startHour = it },
                        label = { Text("Hour") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryStartHour"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = startMinute,
                        onValueChange = { startMinute = it },
                        label = { Text("Minute") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryStartMinute"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // --- Final Date Sections ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Draw date and time (entries close)")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = finalDay,
                        onValueChange = { finalDay = it },
                        label = { Text("Day") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryFinalDay"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = finalMonth,
                        onValueChange = { finalMonth = it },
                        label = { Text("Month") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryFinalMonth"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = finalYear,
                        onValueChange = { finalYear = it },
                        label = { Text("Year") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryFinalYear"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = finalHour,
                        onValueChange = { finalHour = it },
                        label = { Text("Hour") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryFinalHour"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = finalMinute,
                        onValueChange = { finalMinute = it },
                        label = { Text("Minute") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryFinalMinute"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                TextField(
                    value = numWinners,
                    onValueChange = { numWinners = it },
                    label = { Text("Number of Winners/Spots Available") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lotteryWinners"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                TextField(
                    value = bookingLength,
                    onValueChange = { bookingLength = it },
                    label = { Text("Booking length after the draw (minutes)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lotteryBookingLength"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = capacity,
                        onValueChange = { capacity = it },
                        label = { Text("Capacity") },
                        modifier = Modifier
                            .weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dates offered (tap days, then press Confirm)")
                CalenderVeiw(initialSelected = lotteryDates, onConfirm = { lotteryDates = it })
                Text(
                    text = if (lotteryDates.isEmpty()) "No dates confirmed yet"
                    else "${lotteryDates.size} dates confirmed: ${lotteryDates.joinToString(", ")}",
                    modifier = Modifier.testTag("lotteryDatesSummary")
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
                        editingLottery?.lotteryId,
                        name,
                        startDay,
                        startMonth,
                        startYear,
                        startHour,
                        startMinute,
                        finalDay,
                        finalMonth,
                        finalYear,
                        finalHour,
                        finalMinute,
                        numWinners,
                        capacity,
                        selectedCampsites,
                        lotteryDates,
                        bookingLength
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
                Text(if (editingLottery != null) "Save Changes" else "Create Lottery")
            }
        }
    }
}