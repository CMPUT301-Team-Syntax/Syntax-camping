package com.example.syntax_camping.UserScreens

import android.content.Context
import com.example.syntax_camping.ui.components.PromptCsvDownload
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.R
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.Reservation
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.syntax_camping.screens.CalenderVeiw

private val bookingFormat = DateTimeFormatter.ofPattern("dd/MM/yy")
private val typedFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

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
    var confirmedCampsite by rememberSaveable { mutableStateOf<String?>(null) }
    var bookingName by rememberSaveable { mutableStateOf(userViewModel.currentUser?.name ?: "") }
    var csvToDownload by remember { mutableStateOf<String?>(null) }
    var csvFileName by remember { mutableStateOf("reservation.csv") }
    var error by remember { mutableStateOf("") }
    var tapped by remember { mutableStateOf("") }
    var booking by remember { mutableStateOf(false) }
    var startText by rememberSaveable { mutableStateOf("") }
    var endText by rememberSaveable { mutableStateOf("") }
    var rangeError by remember { mutableStateOf("") }

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
            val cap = if (d.maxOcc > 0) d.maxOcc else (c.maxOcc)
            runCatching { LocalDate.parse(d.dateString, bookingFormat) }.getOrNull()
                ?.let { it to (cap - d.currentOcc) }
        }?.toMap() ?: emptyMap()
    }

    fun spotsLeft(d: LocalDate) = spots[d] ?: (c?.maxOcc ?: 0)
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
        error = ""; rangeError = ""
        tapped = "${d.format(bookingFormat)}: ${spotsLeft(d)} spot(s) left"
        val s = start
        if (s == null || end != null || d.isBefore(s)) {
            startEpoch = d.toEpochDay(); endEpoch = null
            startText = d.format(typedFormat); endText = ""
        } else {
            val days = generateSequence(s) { it.plusDays(1) }.takeWhile { !it.isAfter(d) }.toList()
            if (days.all { bookable(it) }) {
                endEpoch = d.toEpochDay(); endText = d.format(typedFormat)
            } else {
                error = "Every date in your stay must be available. Pick a different range."
                startEpoch = d.toEpochDay(); endEpoch = null
                startText = d.format(typedFormat); endText = ""
            }
        }
    }

    // called when the user types in the start / end boxes
    fun onDatesTyped() {
        val s = runCatching { LocalDate.parse(startText, typedFormat) }.getOrNull()
        val e = runCatching { LocalDate.parse(endText, typedFormat) }.getOrNull()
        startEpoch = null; endEpoch = null; error = ""
        if (s == null || e == null) { rangeError = "Enter both dates as dd/mm/yyyy."; return }
        if (e.isBefore(s)) { rangeError = "The end date can't be before the start date."; return }
        val days = generateSequence(s) { it.plusDays(1) }.takeWhile { !it.isAfter(e) }.toList()
        if (days.any { it !in offered }) { rangeError = "Every date must be offered by this lottery, with no gaps."; return }
        if (days.any { !bookable(it) }) { rangeError = "Some of those dates are full or in the past."; return }
        rangeError = ""
        startEpoch = s.toEpochDay(); endEpoch = e.toEpochDay()
    }

    fun book() {
        val cs = c ?: return
        val l = lot ?: return
        if (!agreed || booking || bookingName.isBlank() || !windowOpen ||
            start == null || end == null || rangeDays.any { !bookable(it) }
        ) return

        val dates = rangeDays.map { it.format(bookingFormat) }.toMutableList()
        val reservation = Reservation(cs, dates, shownGuests, userViewModel.deviceId, "upcoming")
        reservation.bookingName = bookingName.trim()
        booking = true
        error = ""
        reservation.reservationRepository.confirmLotteryBooking(
            reservation = reservation,
            lotteryId = l.lotteryId,
            rulesAcknowledged = agreed
        ) { saved, csv ->
            booking = false
            if (saved) {
                confirmedCampsite = cs.name
                csvFileName = "reservation_${reservation.reservationId}.csv"
                csvToDownload = csv
                userViewModel.refreshCurrentUser()
            } else {
                error = context.getString(R.string.reservation_confirmation_error)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF4E8D5),
        topBar = { TopBar(navController) },
        bottomBar = { CustomBottomBar(navController, "BookCampsite", Color(0xFF3E6B68)) }
    ) { innerPadding ->
        PromptCsvDownload(
            csvContent = csvToDownload,
            defaultFileName = csvFileName,
            onDismiss = { csvToDownload = null }
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            val confirmedName = confirmedCampsite
            when {
                confirmedName != null && start != null && end != null -> {
                    ReservationConfirmedContent(
                        campsiteName = confirmedName,
                        checkIn = start.format(bookingFormat),
                        checkOut = end.plusDays(1).format(bookingFormat),
                        onReservations = { navController.navigate("Reservations") { popUpTo("Search") } }
                    )
                }
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
                    val bookableStrings = offered.filter { bookable(it) }.sorted().map { it.format(bookingFormat) }
                    CalenderVeiw(
                        initialSelected = bookableStrings,
                        selectedColor = Color(0xFF2E7D32),
                        onPick = { onDayTap(it) },
                        pickedDates = rangeDays
                    )
                    Text("Green dates are open. Grey dates are full, past, or not part of this lottery. Tap your first date, then your last date.", fontSize = 13.sp, color = Color.DarkGray)
                    if (tapped.isNotEmpty()) Text(tapped, color = Color.DarkGray, modifier = Modifier.testTag("spotsLeftText"))
                    if (error.isNotEmpty()) Text(error, color = Color(0xFFA13F3F), modifier = Modifier.testTag("bookingError"))
                    Spacer(Modifier.height(12.dp))

                    Text("Your stay", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startText,
                            onValueChange = { startText = it; onDatesTyped() },
                            label = { Text("Start dd/mm/yyyy") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("startDateInput")
                        )
                        OutlinedTextField(
                            value = endText,
                            onValueChange = { endText = it; onDatesTyped() },
                            label = { Text("End dd/mm/yyyy") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("endDateInput")
                        )
                    }
                    if (rangeError.isNotEmpty()) Text(rangeError, color = Color(0xFFA13F3F), modifier = Modifier.testTag("rangeError"))
                    if (start != null && end != null) Text("${rangeDays.size} day(s) selected", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onConfirmStage = true },
                        enabled = start != null && end != null,
                        modifier = Modifier.fillMaxWidth().testTag("continueBookingButton"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                    ) { Text("Confirm dates", color = Color.White) }
                }
                else -> {
                    Text(stringResource(R.string.reservation_confirm), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(c.name, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.reservation_check_in, rangeDays.first().format(bookingFormat)))
                    Text(stringResource(R.string.reservation_check_out, rangeDays.last().plusDays(1).format(bookingFormat)))
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

                    ReservationConfirmationControls(
                        navController = navController,
                        acknowledged = agreed,
                        onAcknowledgementChange = { agreed = it },
                        bookingNameEntered = bookingName.isNotBlank(),
                        submitting = booking,
                        onConfirm = ::book
                    )
                    if (error.isNotEmpty()) {
                        Text(error, color = Color(0xFFA13F3F), modifier = Modifier.testTag("bookingError"))
                    }
                    TextButton(
                        onClick = {
                            onConfirmStage = false
                            agreed = false
                        },
                        enabled = !booking,
                        modifier = Modifier.testTag("changeDatesButton")
                    ) { Text("Change dates") }
                }
            }
        }
    }
}
