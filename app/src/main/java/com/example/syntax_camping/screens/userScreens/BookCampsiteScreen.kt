package com.example.syntax_camping.UserScreens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.Reservation
import com.example.syntax_camping.dataClasses.ReservationRepository
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

private val bookingFormat = DateTimeFormatter.ofPattern("dd/MM/yy")

/** True if this user already pressed Accept on the win popup for this lottery (saved on the phone by LotteryWinnerSection). */
fun winnerAccepted(context: Context, lotteryId: String, userId: String): Boolean =
    context.getSharedPreferences("lotteryAccepts", Context.MODE_PRIVATE)
        .getBoolean("accepted_${lotteryId}_$userId", false)

@Composable
fun BookCampsiteScreen(
    navController: NavController,
    campsiteId: String?,
    lotteryId: String?,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    var campsite by remember { mutableStateOf<Campsite?>(null) }
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var myStatus by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }

    // survive going to the rules / policy pages and coming back
    var startEpoch by rememberSaveable { mutableStateOf<Long?>(null) }
    var endEpoch by rememberSaveable { mutableStateOf<Long?>(null) }
    var onConfirmStage by rememberSaveable { mutableStateOf(false) }
    var guests by rememberSaveable { mutableIntStateOf(1) }
    var agreed by rememberSaveable { mutableStateOf(false) }
    var bookingName by rememberSaveable { mutableStateOf(userViewModel.currentUser?.name ?: "") }

    var error by remember { mutableStateOf("") }
    var tapped by remember { mutableStateOf("") }
    var booking by remember { mutableStateOf(false) }

    LaunchedEffect(campsiteId, lotteryId) {
        CampsiteRepository.refreshCampsiteCache {
            campsite = CampsiteRepository.getCampsites().find { it.documentId == campsiteId }
            if (!lotteryId.isNullOrBlank()) {
                lotteryRepository().getLottery(lotteryId) { l ->
                    lottery = l
                    lotteryRepository().getWinners(lotteryId) { list ->
                        myStatus = list.firstOrNull { it.userID == userViewModel.deviceId }?.status
                        loaded = true
                    }
                }
            } else {
                loaded = true
            }
        }
    }

    val c = campsite
    val lot = lottery
    val offered = remember(lot, c) {
        val fromLottery = lot?.lotteryDates
            ?.mapNotNull { runCatching { LocalDate.parse(it, bookingFormat) }.getOrNull() }
            ?.toSet() ?: emptySet()
        if (fromLottery.isNotEmpty()) fromLottery
        else c?.dateManager?.allDates
            ?.mapNotNull { runCatching { LocalDate.parse(it.dateString, bookingFormat) }.getOrNull() }
            ?.toSet() ?: emptySet()
    }
    // spots left per date, from the campsite's own dates (maxOcc - currentOcc)
    val spots = remember(c) {
        c?.dateManager?.allDates?.mapNotNull { d ->
            runCatching { LocalDate.parse(d.dateString, bookingFormat) }.getOrNull()
                ?.let { it to (d.maxOcc - d.currentOcc) }
        }?.toMap() ?: emptyMap()
    }
    fun spotsLeft(d: LocalDate) = spots[d] ?: 0
    fun bookable(d: LocalDate) = d in offered && !d.isBefore(LocalDate.now()) && spotsLeft(d) > 0

    val start = startEpoch?.let { LocalDate.ofEpochDay(it) }
    val end = endEpoch?.let { LocalDate.ofEpochDay(it) }
    val rangeDays: List<LocalDate> =
        if (start != null && end != null)
            generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }.toList()
        else if (start != null) listOf(start) else emptyList()
    val maxGuests = if (c == null || rangeDays.isEmpty()) 1
    else maxOf(1, minOf(c.maxOcc, rangeDays.minOf { spotsLeft(it) }))
    val shownGuests = guests.coerceIn(1, maxGuests)

    val windowOpen = lot != null && myStatus == "pending" &&
            winnerAccepted(context, lot.lotteryId, userViewModel.deviceId) &&
            System.currentTimeMillis() <= lot.endTimeBooking && lot.status != "complete"

    fun onDayTap(d: LocalDate) {
        error = ""
        tapped = "${d.format(bookingFormat)}: ${spotsLeft(d)} spot(s) left"
        val s = start
        if (s == null || end != null || d.isBefore(s)) {
            startEpoch = d.toEpochDay(); endEpoch = null
        } else {
            val days = generateSequence(s) { it.plusDays(1) }.takeWhile { !it.isAfter(d) }.toList()
            if (days.all { bookable(it) }) {
                endEpoch = d.toEpochDay()
            } else {
                error = "Every date in your stay must be available. Pick a different range."
                startEpoch = d.toEpochDay(); endEpoch = null
            }
        }
    }

    fun book() {
        val cs = c ?: return
        val l = lot ?: return
        val dateStrings = rangeDays.map { it.format(bookingFormat) }
        val numGuests = shownGuests
        booking = true
        error = ""
        val fs = FirebaseFirestore.getInstance()
        val ref = fs.collection("campsites").document(cs.documentId)
        fs.runTransaction { tx ->
            val snap = tx.get(ref)
            val raw = snap.get("dates") as? List<*> ?: emptyList<Any>()
            val updated = raw.map { item ->
                val m = item as? Map<*, *> ?: return@map item
                val d = m["date"] as? String ?: ""
                if (d in dateStrings) {
                    val max = (m["maxOcc"] as? Number)?.toInt() ?: 0
                    val cur = (m["currentOcc"] as? Number)?.toInt() ?: 0
                    if (cur + numGuests > max) {
                        throw FirebaseFirestoreException("Not enough spots", FirebaseFirestoreException.Code.ABORTED)
                    }
                    mapOf("date" to d, "maxOcc" to max, "currentOcc" to cur + numGuests)
                } else item
            }
            tx.update(ref, "dates", updated)
            null
        }.addOnSuccessListener {
            val reservation = Reservation(cs, dateStrings.toMutableList(), numGuests, userViewModel.deviceId, "upcoming")
            ReservationRepository().addReservation(cs.documentId, reservation) { saved ->
                if (!saved) {
                    booking = false
                    error = "Your dates were held but saving the reservation failed. Please try again."
                    return@addReservation
                }
                lotteryRepository().updateWinnerStatus(l.lotteryId, userViewModel.deviceId, "confirmed") {
                    userViewModel.refreshCurrentUser()
                    navController.navigate("Reservations") { popUpTo("Search") }
                }
            }
        }.addOnFailureListener {
            booking = false
            error = "Those dates just filled up. Please pick again."
            startEpoch = null; endEpoch = null; onConfirmStage = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF4E8D5),
        topBar = { TopBar(navController) },
        bottomBar = { CustomBottomBar(navController, "BookCampsite", Color(0xFF3E6B68)) }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when {
                !loaded -> CircularProgressIndicator()
                c == null || lot == null -> Text("Campsite not found.")
                !windowOpen -> {
                    Text(
                        "You can't book right now. Accept your win on the lottery page first, and book before the booking window closes.",
                        modifier = Modifier.testTag("bookingNotAllowedText")
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                    ) { Text("Back", color = Color.White) }
                }
                !onConfirmStage -> {
                    Text(c.name, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Tap your first date, then your last date. Dates must run one after another.")
                    Spacer(Modifier.height(8.dp))
                    BookingCalendar(
                        bookable = { bookable(it) },
                        selected = { it in rangeDays },
                        onTap = { onDayTap(it) }
                    )
                    if (tapped.isNotEmpty()) Text(tapped, color = Color.DarkGray, modifier = Modifier.testTag("spotsLeftText"))
                    if (error.isNotEmpty()) Text(error, color = Color(0xFFA13F3F), modifier = Modifier.testTag("bookingError"))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (start == null) "No dates picked yet"
                        else "From ${start.format(bookingFormat)} to ${(end ?: start).format(bookingFormat)} (${rangeDays.size} day(s))",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onConfirmStage = true },
                        enabled = rangeDays.isNotEmpty() && (end != null || start != null),
                        modifier = Modifier.fillMaxWidth().testTag("continueBookingButton"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                    ) { Text("Continue", color = Color.White) }
                }
                else -> {
                    Text("Confirm your booking", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(c.name, fontWeight = FontWeight.Bold)
                    Text("Dates: ${rangeDays.first().format(bookingFormat)} to ${rangeDays.last().format(bookingFormat)} (${rangeDays.size} day(s))")
                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Guests", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = { guests = (shownGuests - 1).coerceAtLeast(1) },
                            modifier = Modifier.testTag("guestsMinus")
                        ) { Text("−", fontSize = 22.sp) }
                        Text("$shownGuests", fontSize = 18.sp, modifier = Modifier.testTag("guestsCount"))
                        TextButton(
                            onClick = { guests = (shownGuests + 1).coerceAtMost(maxGuests) },
                            modifier = Modifier.testTag("guestsPlus")
                        ) { Text("+", fontSize = 22.sp) }
                    }
                    Text("At most $maxGuests guest(s) fit on every date you picked.", fontSize = 13.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = bookingName,
                        onValueChange = { bookingName = it },
                        label = { Text("Booking name") },
                        modifier = Modifier.fillMaxWidth().testTag("bookingNameInput")
                    )
                    Spacer(Modifier.height(12.dp))

                    CampsiteRulesLink(navController)
                    Spacer(Modifier.height(4.dp))
                    CancellationPolicyLink(navController)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = agreed,
                            onCheckedChange = { agreed = it },
                            modifier = Modifier.testTag("agreeRulesCheckbox")
                        )
                        Text("I agree to the campsite rules and cancellation policy")
                    }
                    if (error.isNotEmpty()) Text(error, color = Color(0xFFA13F3F), modifier = Modifier.testTag("bookingError"))
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { book() },
                        enabled = agreed && bookingName.isNotBlank() && !booking,
                        modifier = Modifier.fillMaxWidth().testTag("payNowButton"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                    ) { Text(if (booking) "Booking..." else "Pay now", color = Color.White) }
                    TextButton(
                        onClick = { onConfirmStage = false },
                        modifier = Modifier.testTag("changeDatesButton")
                    ) { Text("Change dates") }
                }
            }
        }
    }
}

@Composable
private fun BookingCalendar(
    bookable: (LocalDate) -> Boolean,
    selected: (LocalDate) -> Boolean,
    onTap: (LocalDate) -> Unit
) {
    val currentMonth = remember { YearMonth.now() }
    val scope = rememberCoroutineScope()
    val state = rememberCalendarState(
        startMonth = currentMonth,
        endMonth = currentMonth.plusMonths(24),
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek().first()
    )
    val visibleMonth = state.firstVisibleMonth.yearMonth

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { scope.launch { state.animateScrollToMonth(visibleMonth.minusMonths(1)) } }) { Text("<") }
            Text(
                "${visibleMonth.month.getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale)} ${visibleMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { scope.launch { state.animateScrollToMonth(visibleMonth.plusMonths(1)) } }) { Text(">") }
        }
        Row(Modifier.fillMaxWidth()) {
            daysOfWeek().forEach { dow ->
                Text(
                    dow.getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                val inMonth = day.position == DayPosition.MonthDate
                val ok = inMonth && bookable(day.date)
                val sel = inMonth && selected(day.date)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(if (sel) Color(0xFF3E6B68) else Color.Transparent)
                        .clickable(enabled = ok) { onTap(day.date) },
                    contentAlignment = Alignment.Center
                ) {
                    if (inMonth) {
                        Text(
                            day.date.dayOfMonth.toString(),
                            color = when {
                                sel -> Color.White
                                ok -> Color(0xFF3E6B68)
                                else -> Color.LightGray
                            },
                            fontWeight = if (ok) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        )
        Text("Green dates are open. Grey dates are full, past, or not part of this lottery.", fontSize = 13.sp, color = Color.DarkGray)
    }
}