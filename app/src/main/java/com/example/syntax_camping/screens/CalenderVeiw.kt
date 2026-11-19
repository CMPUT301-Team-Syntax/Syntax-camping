//package com.example.syntax_camping.screens
//
//import android.icu.text.SimpleDateFormat
//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.DatePicker
//import androidx.compose.material3.DateRangePicker
//import androidx.compose.material3.Text
//import androidx.compose.material3.rememberDatePickerState
//import androidx.compose.material3.rememberDateRangePickerState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import java.time.Instant
//
//
//
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.tooling.preview.Preview
//import java.time.ZoneOffset
//import java.time.format.DateTimeFormatter
//import java.util.Locale
//import java.time.LocalDate
//
//import kotlin.streams.toList
//
//
//
//
//
//
//
//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun CalenderVeiw() {
//    val datePickerState = rememberDateRangePickerState()
//
//    var formattedDateList by remember { mutableStateOf<List<String>>(emptyList()) }
//    Column(modifier = Modifier.padding(16.dp)) {
//
//        Column(modifier = Modifier.padding(16.dp)) {
//            DateRangePicker(state = datePickerState)
//
//            Button(
//                onClick = {
//                    val selectedStartDate = datePickerState.selectedStartDateMillis
//                    val selectedEndDate = datePickerState.selectedEndDateMillis
//
//                    if (selectedStartDate != null && selectedEndDate != null) {
//                        formattedDateList = getDatesInRange(selectedStartDate, selectedEndDate)
//                    }
//                }
//            ) {
//                Text("confirm Range")
//            }
//
//
//        }
//
//    }
//}
//
//@RequiresApi(Build.VERSION_CODES.O)
//
//fun getDatesInRange(startDateMillis: Long?, endDateMillis: Long?): List<String> {
//    val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
//
//
//    if (startDateMillis == null || endDateMillis == null) return emptyList()
//
//    val start = Instant.ofEpochMilli(startDateMillis).atZone(ZoneOffset.UTC).toLocalDate()
//    val end = Instant.ofEpochMilli(endDateMillis).atZone(ZoneOffset.UTC).toLocalDate()
//
//
//    return generateSequence(start) { it.plusDays(1) }
//        .takeWhile { !it.isAfter(end) }
//        .map { it.format(formatter) }
//        .toList()
//}
//
//@Preview(showBackground = true)
//@Composable
//fun CalenderViewPreview() {
//    CalenderVeiw()
//}
