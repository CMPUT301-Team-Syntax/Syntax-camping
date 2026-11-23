package com.example.syntax_camping.screens



import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.collections.sorted
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalLocale

@Composable
fun CalenderVeiw(
    onConfirm: (List<String>) -> Unit = {},
    initialSelected: List<String> = emptyList(),   // ADDED: days that start out selected
    readOnly: Boolean = false,
    selectedColor: Color = Color.Red,       // colour of picked dates
    startLocked: Boolean = false,           // start already confirmed (read-only with an Edit button)
    onEdit: () -> Unit = {},               // called when Edit is tapped
    onPick: ((LocalDate) -> Unit)? = null,   // booking mode: tapping a lit date calls this instead of toggling it
    pickedDates: List<LocalDate> = emptyList(),
    pickedColor: Color = Color(0xFF1565C0)

// ADDED: view only, no tapping, no Confirm button
) {
    val currentMonth = remember { YearMonth.now() }
    val daysOfWeek = remember { daysOfWeek() }   // starts on the locale's first day
    val selectedDate = remember(initialSelected) {
        mutableStateListOf<LocalDate>().apply {
            val f = DateTimeFormatter.ofPattern("dd/MM/yy")
            addAll(initialSelected.mapNotNull { runCatching { LocalDate.parse(it, f) }.getOrNull() })
        }
    }
    var confirmedDate by remember { mutableStateOf<List<String>>(emptyList()) }
    var locked by remember { mutableStateOf(startLocked) }   // true after Confirm, until Edit is tapped
    val isReadOnly = readOnly || locked
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yy") }
    val scope = rememberCoroutineScope()

    val state = rememberCalendarState(
        startMonth = currentMonth,
        endMonth = currentMonth.plusMonths(24),
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek().first()

    )

    val visibleMonth = state.firstVisibleMonth.yearMonth

    Column(modifier = Modifier.padding(16.dp)) {

        // Month title with arrows
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = {
                scope.launch { state.animateScrollToMonth(visibleMonth.minusMonths(1)) }
            }) { Text("<") }

            Text(
                text = "${visibleMonth.month.getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale)} ${visibleMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = {
                scope.launch { state.animateScrollToMonth(visibleMonth.plusMonths(1)) }
            }) { Text(">") }
        }

        // Sun Mon Tue ... labels
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek().forEach { dow ->
                Text(
                    text = dow.getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                val picked = day.date in pickedDates
                Day(
                    day = day,
                    isSelected = picked || day.date in selectedDate,
                    selectedColor = if (picked) pickedColor else selectedColor,
                    onClick = {
                        if (onPick != null) {
                            if (it.date in selectedDate) onPick(it.date)
                        } else if (!isReadOnly) {
                            if (it.date in selectedDate) selectedDate.remove(it.date)
                            else selectedDate.add(it.date)
                        }
                    }
                )
            }
        )

        if (!readOnly && onPick == null) {   // CHANGED: no buttons at all in a permanently view-only calendar
            Spacer(Modifier.height(12.dp))

            if (!locked) {
                Button(
                    onClick = {
                        val resulted = selectedDate.sorted().map { it.format(formatter) }
                        confirmedDate = resulted
                        locked = true
                        onConfirm(resulted)
                    },
                    enabled = selectedDate.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().testTag("calendarConfirmButton")
                ) {
                    Text("Confirm")
                }
            } else {
                Button(
                    onClick = { locked = false; onEdit() },
                    modifier = Modifier.fillMaxWidth().testTag("calendarEditButton")
                ) {
                    Text("Edit")
                }
            }
        }
    }

}

@Composable
fun Day(day: CalendarDay, isSelected: Boolean, onClick: (CalendarDay) -> Unit, selectedColor: Color = Color.Red) {    val inMonth = day.position == DayPosition.MonthDate
    val isPast = day.date.isBefore(LocalDate.now())   // CHANGED
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(enabled = inMonth && !isPast) { onClick(day) },   // CHANGED
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = when {
                isSelected -> selectedColor
                isPast -> Color.LightGray   // CHANGED
                inMonth -> Color.Blue
                else -> Color.Black
            }
        )
    }
}




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
//// pass date filter
//
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
@Preview(showBackground = true)
@Composable
fun CalenderViewPreview() {
    CalenderVeiw()
}
