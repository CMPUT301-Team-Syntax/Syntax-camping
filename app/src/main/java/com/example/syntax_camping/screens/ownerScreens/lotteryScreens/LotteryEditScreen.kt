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


@Composable
fun LotteryEditScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    editingLottery: Lottery?,
    preselectedCampsite: Campsite? = null
) {
    var name by remember { mutableStateOf("") }
    // draw (the countdown ends here)
    var drawDay by remember { mutableStateOf("") }
    var drawMonth by remember { mutableStateOf("") }
    var drawYear by remember { mutableStateOf("") }
    var drawHour by remember { mutableStateOf("") }
    var drawMinute by remember { mutableStateOf("") }
    // last day to book (winners and waitlisters)
    var bookByDay by remember { mutableStateOf("") }
    var bookByMonth by remember { mutableStateOf("") }
    var bookByYear by remember { mutableStateOf("") }
    var bookByHour by remember { mutableStateOf("") }
    var bookByMinute by remember { mutableStateOf("") }
    var numWinners by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val selectedCampsites = remember { mutableStateListOf<Campsite>() }
    var capacity by remember { mutableStateOf("") }
    var lotteryDates by remember { mutableStateOf<List<String>>(emptyList()) }
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

            val drawCal = Calendar.getInstance().apply { timeInMillis = editingLottery.endTimeLottery }
            drawDay = drawCal.get(Calendar.DAY_OF_MONTH).toString()
            drawMonth = (drawCal.get(Calendar.MONTH) + 1).toString()
            drawYear = drawCal.get(Calendar.YEAR).toString()
            drawHour = drawCal.get(Calendar.HOUR_OF_DAY).toString()
            drawMinute = drawCal.get(Calendar.MINUTE).toString()

            val bookByCal = Calendar.getInstance().apply { timeInMillis = editingLottery.endTimeBooking }
            bookByDay = bookByCal.get(Calendar.DAY_OF_MONTH).toString()
            bookByMonth = (bookByCal.get(Calendar.MONTH) + 1).toString()
            bookByYear = bookByCal.get(Calendar.YEAR).toString()
            bookByHour = bookByCal.get(Calendar.HOUR_OF_DAY).toString()
            bookByMinute = bookByCal.get(Calendar.MINUTE).toString()

            selectedCampsites.clear()
            selectedCampsites.addAll(editingLottery.campsites)


        }  else {
            selectedCampsites.clear()
            // a new lottery started from a campsite's page begins with that campsite already in the list
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
            // --- Draw date and time: the countdown ends here ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Draw date and time (the countdown ends here)")

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
                        value = drawDay,
                        onValueChange = { drawDay = it },
                        label = { Text("Day") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryStartDay"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = drawMonth,
                        onValueChange = { drawMonth = it },
                        label = { Text("Month") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryStartMonth"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    TextField(
                        value = drawYear,
                        onValueChange = { drawYear = it },
                        label = { Text("Year") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryStartYear"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = drawHour,
                        onValueChange = { drawHour = it },
                        label = { Text("Hour") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryStartHour"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = drawMinute,
                        onValueChange = { drawMinute = it },
                        label = { Text("Minute") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryStartMinute"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // --- Last day to book: winners and waitlisters ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Last day and time to book (winners and waitlisters)")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = bookByDay,
                        onValueChange = { bookByDay = it },
                        label = { Text("Day") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryFinalDay"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = bookByMonth,
                        onValueChange = { bookByMonth = it },
                        label = { Text("Month") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryFinalMonth"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = bookByYear,
                        onValueChange = { bookByYear = it },
                        label = { Text("Year") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryFinalYear"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = bookByHour,
                        onValueChange = { bookByHour = it },
                        label = { Text("Hour") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryFinalHour"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = bookByMinute,
                        onValueChange = { bookByMinute = it },
                        label = { Text("Minute") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lotteryFinalMinute"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // --- Winners and capacity ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = numWinners,
                    onValueChange = { numWinners = it },
                    label = { Text("Number of Winners/Spots Available") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lotteryWinners"),
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
                        drawDay,
                        drawMonth,
                        drawYear,
                        drawHour,
                        drawMinute,
                        bookByDay,
                        bookByMonth,
                        bookByYear,
                        bookByHour,
                        bookByMinute,
                        numWinners,
                        capacity,
                        selectedCampsites,
                        lotteryDates
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