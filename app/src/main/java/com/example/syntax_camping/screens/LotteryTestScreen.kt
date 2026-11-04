
package com.example.syntax_camping
import com.google.firebase.Timestamp
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.TextField
import androidx.compose.runtime.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.syntax_camping.dataClasses.LotteryEntry
import com.example.syntax_camping.dataClasses.LotteryManager
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.dateConverterToLong

@Composable
fun LotteryTestScreen(navController: NavController) {
    // Start date and time
    var startDay by remember { mutableStateOf("") }
    var startMonth by remember { mutableStateOf("") }
    var startYear by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf("") }
    var startMinute by remember { mutableStateOf("") }
    var lottery: Lottery?
    lottery = null
    // Final date and time
    var finalDay by remember { mutableStateOf("") }
    var finalMonth by remember { mutableStateOf("") }
    var finalYear by remember { mutableStateOf("") }
    var finalHour by remember { mutableStateOf("") }
    var finalMinute by remember { mutableStateOf("") }
    val testCamp = Campsite("Test12", "Name", "Lop", "This is a description", 10, mutableListOf<String>(), rating = 4.5)
    val campsites = mutableListOf<Campsite>(testCamp)


    Column (
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Start date section
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Start Date and Time")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Day TextField
                TextField(
                    value = startDay,
                    onValueChange = { startDay = it },
                    label = { Text("Day") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // Month TextField
                TextField(
                    value = startMonth,
                    onValueChange = { startMonth = it },
                    label = { Text("Month") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Year TextField
                TextField(
                    value = startYear,
                    onValueChange = { startYear = it },
                    label = { Text("Year") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Hour TextField
                TextField(
                    value = startHour,
                    onValueChange = { startHour = it },
                    label = { Text("Hour") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // Minute TextField

                TextField(
                    value = startMinute,
                    onValueChange = { startMinute = it },
                    label = { Text("Minute") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        // Final date section
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Final Date and Time")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Day TextField
                TextField(
                    value = finalDay,
                    onValueChange = { finalDay = it },
                    label = { Text("Day") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // Month TextField
                TextField(
                    value = finalMonth,
                    onValueChange = { finalMonth = it },
                    label = { Text("Month") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // Year TextField
                TextField(
                    value = finalYear,
                    onValueChange = { finalYear = it },
                    label = { Text("Year") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Hour TextField
                TextField(
                    value = finalHour,
                    onValueChange = { finalHour = it },
                    label = { Text("Hour") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // Minute TextField
                TextField(
                    value = finalMinute,
                    onValueChange = { finalMinute = it },
                    label = { Text("Minute") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        //Convert everything back to int????? textfield only accepts strings
        Button(
            onClick = {
                //Turns everything to int or else its just null (wow cool functions kotlin)
                val startDayInt = startDay.toIntOrNull()
                val startMonthInt = startMonth.toIntOrNull()
                val startYearInt = startYear.toIntOrNull()
                val startHourInt = startHour.toIntOrNull()
                val startMinuteInt = startMinute.toIntOrNull()

                val finalDayInt = finalDay.toIntOrNull()
                val finalMonthInt = finalMonth.toIntOrNull()
                val finalYearInt = finalYear.toIntOrNull()
                val finalHourInt = finalHour.toIntOrNull()
                val finalMinuteInt = finalMinute.toIntOrNull()

                //If even a field is null its wraps and the button does nothing.
                if (
                    startDayInt == null || startMonthInt == null ||
                    startYearInt == null || startHourInt == null ||
                    startMinuteInt == null || finalDayInt == null ||
                    finalMonthInt == null || finalYearInt == null ||
                    finalHourInt == null || finalMinuteInt == null
                ) {
                    return@Button
                } else {
                    val testOwner = CampsiteOwner("Id1", "name1", "location.random")
                    val dateLongConverter = dateConverterToLong()
                    val endTimeLottery = dateLongConverter.convertDateToLong(
                        startYearInt,
                        startMonthInt,
                        startDayInt,
                        startHourInt,
                        startMinuteInt
                    )
                    val endTimeBooking = dateLongConverter.convertDateToLong(
                        finalYearInt,
                        finalMonthInt,
                        finalDayInt,
                        finalHourInt,
                        finalMinuteInt
                    )
                    lottery = Lottery(endTimeLottery, endTimeBooking, 1, campsites)
                    lottery.createLotteryTimer()

                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Something relating to lottery for Luke")
        }

        val context = LocalContext.current
        // Join Lottery button
        Button(
            onClick = {
                val db = Database()
                val deviceId = db.getDeviceId(context)
                //val testCamp = testCamp
                val entry1 = LotteryEntry(
                    userId = deviceId,
                    joined = Timestamp.now(),

                    )
                val entry2 = LotteryEntry(
                    "id2",
                    joined = Timestamp.now(),

                    )

                //val lotteryManager = testCamp.lotteryManager
                if (lottery != null) {
                    lottery.addEntry(entry1)
                    lottery.addEntry(entry2)
                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Join Lottery or sum")
        }


     //   val context = LocalContext.current
        Button(
            onClick = {
                val db = Database()
                val deviceId = db.getDeviceId(context)
                //val testCamp = testCamp
                val entry1 = LotteryEntry(
                    userId = deviceId,
                    joined = Timestamp.now(),

                    )


                //val lotteryManager = testCamp.lotteryManager
                if (lottery != null) {
                    lottery.removeEntry(entry1)

                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("delete a user")
        }
    }


}