package com.example.syntax_camping.UserScreens

import android.Manifest
import android.R.id.checkbox
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions   // ADDED: runs the search when you press Done on the keyboard
import androidx.compose.foundation.text.KeyboardOptions   // ADDED: number keyboard
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction        // ADDED: the keyboard's Done button
import androidx.compose.ui.text.input.KeyboardType     // ADDED: number keyboard
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.userScreens.userCampsiteList.CampsiteListScreen
import com.example.syntax_camping.screens.tabs.TopBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.testTag
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.screens.CalenderVeiw
import com.example.syntax_camping.screens.CampsiteSearchMap
import com.example.syntax_camping.screens.campsiteHasRoom
import com.example.syntax_camping.screens.distanceKm
import com.example.syntax_camping.screens.fetchCurrentLocation
import com.example.syntax_camping.screens.hasLocationPermission
import com.example.syntax_camping.screens.parseSearchDate
import com.example.syntax_camping.screens.parseSearchLocalDate
import com.example.syntax_camping.screens.searchDaysBetween
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

@Composable
fun SearchHomeScreen(navController: NavController, userViewModel: UserViewModel){

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var  SearchInput by remember { mutableStateOf("")  }
    var FlitteredCampsiteList by remember { mutableStateOf(listOf<Campsite>()) }

    var realCampsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var displayFillereted by remember { mutableStateOf(false) }
    var StartDateRange by remember { mutableStateOf("") }
    var EndDateRange by remember { mutableStateOf("") }
    var numGuests by remember { mutableStateOf(1) }
    var selectedCamps by remember { mutableStateOf(setOf<String>()) }
    var showMap by remember { mutableStateOf(false) }   // false = the list, true = the map

    // "near me" search
    var nearMeOn by remember { mutableStateOf(false) }
    var radiusKm by remember { mutableStateOf(50) }
    var customKm by remember { mutableStateOf("") }   // ADDED: the distance typed in. Blank = use a chip.
    // ADDED: true when something is typed but it isn't a whole number from 1 to 1000
    val customKmInvalid = customKm.isNotEmpty() && (customKm.toIntOrNull()?.let { it !in 1..1000 } ?: true)
    var appliedNearMe by remember { mutableStateOf<Pair<Double, Double>?>(null) }   // where the user was when they applied it
    var appliedRadiusKm by remember { mutableStateOf<Int?>(null) }
    var locating by remember { mutableStateOf(false) }
    var filterMessage by remember { mutableStateOf("") }
    var waitingForPermission by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }

    // Calendar in the filter drawer: every day from today to a year ahead can be tapped
    val openDays = remember {
        val last = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 365) }
        searchDaysBetween(Calendar.getInstance(), last)
    }
    // the range typed or tapped so far, shown highlighted on the calendar
    val pickedStart = parseSearchLocalDate(StartDateRange)
    val pickedEnd = parseSearchLocalDate(EndDateRange)
    val rangeDays: List<LocalDate> = if (pickedStart == null) {
        emptyList()
    } else {
        val last = pickedEnd ?: pickedStart
        generateSequence(pickedStart) { it.plusDays(1) }.takeWhile { !it.isAfter(last) }.take(366).toList()
    }

    // first tap = start, second tap = end. Tapping again after that (or before the start) starts over.
    fun onCalendarTap(day: LocalDate) {
        filterMessage = ""
        val tappedText = "%02d/%02d/%04d".format(day.dayOfMonth, day.monthValue, day.year)
        if (pickedStart == null || pickedEnd != null || day.isBefore(pickedStart)) {
            StartDateRange = tappedText
            EndDateRange = ""
        } else {
            EndDateRange = tappedText
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionGranted = results.values.any { it }
        if (!permissionGranted) {
            waitingForPermission = false
            nearMeOn = false
            filterMessage = "Location permission was denied, so we can't search near you."
        }
    }

    // Combines everything: the search text, the location checkboxes, the dates, the guests and "near me".
    // Returns false (and sets a message) if the dates typed in aren't valid.
    fun applyAllFilters(pos: Pair<Double, Double>?): Boolean {
        var days: List<String>? = null
        if (StartDateRange.isNotBlank() || EndDateRange.isNotBlank()) {
            val start = parseSearchDate(StartDateRange)
            val end = if (EndDateRange.isBlank()) start else parseSearchDate(EndDateRange)
            if (start == null || end == null || end.before(start)) {
                filterMessage = "Dates must look like dd/mm/yyyy, and the end can't be before the start."
                return false
            }
            days = searchDaysBetween(start, end)
        }

        val base = if (realCampsites.isNotEmpty()) realCampsites else CampsiteRepository.getCampsites()
        val matches = base.filter { campsite ->
            (SearchInput.isBlank() ||
                    campsite.name.contains(SearchInput, ignoreCase = true) ||
                    campsite.location.contains(SearchInput, ignoreCase = true)) &&
                    (selectedCamps.isEmpty() || campsite.location in selectedCamps) &&
                    campsite.maxOcc >= numGuests &&
                    (days == null || campsiteHasRoom(campsite, days, numGuests)) &&
                    (pos == null || (campsite.latitude != null && campsite.longitude != null &&
                            distanceKm(pos.first, pos.second, campsite.latitude!!, campsite.longitude!!) <= radiusKm))
        }
        FlitteredCampsiteList = if (pos != null) {
            matches.sortedBy { distanceKm(pos.first, pos.second, it.latitude!!, it.longitude!!) }   // nearest first
        } else {
            matches
        }
        appliedNearMe = pos
        appliedRadiusKm = if (pos != null) radiusKm else null
        displayFillereted = true
        filterMessage = ""
        return true
    }

    // Called by the Apply Filter button and by the Near me chip on the map
    fun startFilter() {
        filterMessage = ""
        if (!nearMeOn) {
            if (applyAllFilters(null)) scope.launch { drawerState.close() }
            return
        }
        if (!hasLocationPermission(context)) {
            waitingForPermission = true
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
            return
        }
        locating = true
        fetchCurrentLocation(context) { pos ->
            locating = false
            if (pos == null) {
                filterMessage = "Couldn't get your location. Make sure location is turned on and try again."
            } else if (applyAllFilters(pos)) {
                scope.launch { drawerState.close() }
            }
        }
    }

    // after the user allows location in the popup, carry on with the search they asked for
    LaunchedEffect(permissionGranted) {
        if (permissionGranted && waitingForPermission) {
            waitingForPermission = false
            startFilter()
        }
    }

    // the phone's back button closes the filter drawer
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // swipe gestures are off while the drawer is open so swiping the calendar's months doesn't close it
        gesturesEnabled = !drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { scope.launch { drawerState.close() } },
                            modifier = Modifier.testTag("closeFilterDrawer")
                        ) {
                            Text("Close")
                        }
                    }
                    Text("Location ", modifier = Modifier.padding(start = 16.dp, top = 8.dp))
                    LazyColumn( modifier = Modifier.height(300.dp)) {
                        items(realCampsites) { campsite ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCamps = if (campsite.location in selectedCamps) {
                                            selectedCamps - campsite.location
                                        } else {
                                            selectedCamps + campsite.location
                                        }
                                    }
                            ) {
                                Checkbox(
                                    checked = campsite.location in selectedCamps,
                                    onCheckedChange = { checked ->
                                        selectedCamps = if (checked) selectedCamps + campsite.location
                                        else selectedCamps - campsite.location
                                    }
                                )
                                Text(text = campsite.location)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dates: tap the first and last day on the calendar (or type them below)
                    Text("Dates", modifier = Modifier.padding(start = 16.dp, top = 8.dp))
                    Text(
                        "Tap your first day, then your last day. Tap one day twice for a single night.",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    CalenderVeiw(
                        initialSelected = openDays,
                        selectedColor = Color(0xFF8DB8B4),
                        onPick = { onCalendarTap(it) },
                        pickedDates = rangeDays,
                        pickedColor = Color(0xFF1F4E4A)
                    )

                    Row() {
                        TextField(
                            value = StartDateRange,
                            onValueChange = { newValue -> StartDateRange = newValue },
                            label = { Text("Start (dd/mm/yyyy)") },
                            singleLine = true,
                            modifier = Modifier
                                .width(150.dp)
                                .padding(16.dp)
                                .testTag("filterStartDate")
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        TextField(
                            value = EndDateRange,
                            onValueChange = { newValue -> EndDateRange = newValue },
                            label = { Text("End (dd/mm/yyyy)") },
                            singleLine = true,
                            modifier = Modifier
                                .width(150.dp)
                                .padding(16.dp)
                                .testTag("filterEndDate")
                        )
                    }
                    TextButton(
                        onClick = {
                            StartDateRange = ""
                            EndDateRange = ""
                        },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .testTag("clearDatesButton")
                    ) {
                        Text("Clear dates")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Number of Guests",modifier = Modifier.padding(start = 16.dp, top = 16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center

                    ) {

                        IconButton(onClick =  {numGuests ++}){
                            Icon(
                                painter = painterResource(id = R.drawable.ic_plus),
                                contentDescription = "add",
                                tint = Color.Unspecified
                            )
                        }
                        Spacer(modifier = Modifier.width(40.dp))

                        Text(text = numGuests.toString())

                        Spacer(modifier = Modifier.width(40.dp))


                        IconButton(onClick =  {if(numGuests > 1) numGuests--  }){
                            Icon(
                                painter = painterResource(id = R.drawable.ic_minus),
                                contentDescription = "add",
                                tint = Color.Unspecified
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))


                    }

                    // Near me: only campsites within a chosen distance of where the user is now
                    Text("Near me", modifier = Modifier.padding(start = 16.dp, top = 16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { nearMeOn = !nearMeOn },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = nearMeOn,
                            onCheckedChange = { nearMeOn = it },
                            modifier = Modifier.testTag("nearMeCheckbox")
                        )
                        Text("Only campsites near my current location")
                    }
                    if (nearMeOn) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(10, 25, 50, 100, 200).forEach { km ->
                                FilterChip(
                                    selected = radiusKm == km,
                                    onClick = {
                                        radiusKm = km
                                        customKm = ""   // ADDED: picking a chip clears the typed distance
                                    },
                                    label = { Text("$km km") },
                                    modifier = Modifier.testTag("radiusChip_$km")
                                )
                            }
                        }

                        // ADDED: type your own distance. It becomes the search radius as soon as it's valid;
                        // press Apply Filter to search.
                        TextField(
                            value = customKm,
                            onValueChange = { typed ->
                                customKm = typed.filter { it.isDigit() }.take(4)   // digits only, at most 4
                                val km = customKm.toIntOrNull()
                                if (km != null && km in 1..1000) radiusKm = km
                            },
                            label = { Text("Or type your own distance (km)") },
                            isError = customKmInvalid,
                            supportingText = { if (customKmInvalid) Text("Enter a number from 1 to 1000") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("customKmField")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (locating) {
                        Text(
                            "Finding your location...",
                            modifier = Modifier.padding(horizontal = 16.dp).testTag("locatingText")
                        )
                    }
                    if (filterMessage.isNotEmpty()) {
                        Text(
                            filterMessage,
                            color = Color(0xFFA13F3F),
                            modifier = Modifier.padding(horizontal = 16.dp).testTag("filterMessage")
                        )
                    }

                    Row( modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center){
                        Button(
                            enabled = !locating,
                            onClick = { startFilter() }
                        ) {
                            Text(text = "Apply Fliter")
                        }

                    }
                }

            }

        }
    ) {
        Scaffold(

            modifier = Modifier
                .fillMaxSize()
                .background(Color.White), // Set the background color to white
            containerColor = Color.White,
            topBar = {
                TopBar(navController)
            },

            bottomBar = {
                CustomBottomBar(
                    navController = navController,
                    currentRoute = "Search",
                    backgroundColor = Color(0xFF3E6B68)
                )
            }
        ) { innerPadding ->

            //inside the scaffold, this would be the content of the screen, start editing screen content from in here

            Column(
                modifier = Modifier

                    .padding(innerPadding)
                    .padding(9.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(){

                    TextField(
                        value = SearchInput,
                        onValueChange = { newValue -> SearchInput = newValue },
                        label = { Text("Type in the location or Campsite Name") },

                        shape = RoundedCornerShape(30.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,   // Removes line when typing
                            unfocusedIndicatorColor = Color.Transparent, // Removes line when idle
                            disabledIndicatorColor = Color.Transparent   // Removes line if disabled
                        ),
                        modifier = Modifier
                            .weight(2f)
                            .testTag("search_input_field")
                            .padding(bottom = 16.dp)
                    )


                    Spacer(modifier = Modifier.width(8.dp))

                    LaunchedEffect(Unit) {
                        CampsiteRepository.refreshCampsiteCache { success ->
                            if (success) {
                                realCampsites = CampsiteRepository.getCampsites().toList()
                            }
                        }
                    }



                    IconButton(onClick = {
                        // keeps any near-me search that was already applied
                        applyAllFilters(appliedNearMe)
                    },
                        modifier = Modifier
                            .background(Color(0xFF3E6B68), shape = RoundedCornerShape(50))
                            .testTag("search_icon")

                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),

                            contentDescription = "Search",
                            tint = Color.Unspecified
                        )
                    }
                    IconButton(onClick = { scope.launch { drawerState.open() } })
                    {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu),
                            contentDescription = "Filter",
                            tint = Color.Unspecified
                        )
                    }

                }

                // List / Map toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showMap = false },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("showListButton"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showMap) Color(0xFF3E6B68) else Color.LightGray
                        )
                    ) { Text("List", color = Color.White) }
                    Button(
                        onClick = { showMap = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("showMapButton"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showMap) Color(0xFF3E6B68) else Color.LightGray
                        )
                    ) { Text("Map", color = Color.White) }
                }


//realCampsites is the actual firebase list of campsites.
                if (showMap) {
                    // the map shows the same campsites the list would (all of them, or the search/filter result)
                    Box(modifier = Modifier.weight(1f)) {
                        CampsiteSearchMap(
                            campsites = if (displayFillereted) FlitteredCampsiteList else realCampsites,
                            onOpenCampsite = { clickedId ->
                                navController.navigate("campsiteDetails/$clickedId")
                            },
                            userLat = appliedNearMe?.first,
                            userLng = appliedNearMe?.second,
                            radiusKm = appliedRadiusKm
                        )

                        // "Near me" right on the map: tap it to search around your current location
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically   // ADDED: keeps the text box level with the chips
                            ) {
                                FilterChip(
                                    selected = nearMeOn,
                                    onClick = {
                                        if (nearMeOn) {
                                            nearMeOn = false
                                            applyAllFilters(null)
                                        } else {
                                            nearMeOn = true
                                            startFilter()
                                        }
                                    },
                                    label = { Text("Near me") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color.White,
                                        selectedContainerColor = Color(0xFF3E6B68),
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("mapNearMeChip")
                                )
                                if (nearMeOn) {
                                    listOf(10, 25, 50, 100, 200).forEach { km ->
                                        FilterChip(
                                            selected = radiusKm == km,
                                            onClick = {
                                                radiusKm = km
                                                customKm = ""   // ADDED: picking a chip clears the typed distance
                                                val known = appliedNearMe
                                                if (known != null) applyAllFilters(known) else startFilter()
                                            },
                                            label = { Text("$km km") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = Color.White,
                                                selectedContainerColor = Color(0xFF3E6B68),
                                                selectedLabelColor = Color.White
                                            ),
                                            modifier = Modifier.testTag("mapRadiusChip_$km")
                                        )
                                    }

                                    // ADDED: type your own distance, then press Done on the keyboard to search
                                    TextField(
                                        value = customKm,
                                        onValueChange = { typed -> customKm = typed.filter { it.isDigit() }.take(4) },
                                        label = { Text("km") },
                                        isError = customKmInvalid,
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(onDone = {
                                            val km = customKm.toIntOrNull()
                                            if (km != null && km in 1..1000) {
                                                radiusKm = km
                                                val known = appliedNearMe
                                                if (known != null) applyAllFilters(known) else startFilter()
                                            }
                                        }),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .width(110.dp)
                                            .testTag("mapCustomKmField")
                                    )
                                }
                            }
                            if (locating) {
                                Text(
                                    "Finding your location...",
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                                        .padding(6.dp)
                                        .testTag("mapLocatingText")
                                )
                            }
                            if (filterMessage.isNotEmpty()) {
                                Text(
                                    filterMessage,
                                    color = Color(0xFFA13F3F),
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                                        .padding(6.dp)
                                        .testTag("mapFilterMessage")
                                )
                            }
                        }
                    }
                }
                else if (displayFillereted) {
                    Box(modifier = Modifier.weight(1f)) {

                        if (FlitteredCampsiteList.isEmpty()) {//database info
                            // Show this if the database is empty or failed to load
                            Text("No campsites found in database.")
                        } else {
                            // Show the actual list of cards!
                            CampsiteListScreen(
                                campsiteList = FlitteredCampsiteList,
                                onItemClick = { clickedId ->
                                    navController.navigate("campsiteDetails/$clickedId") //navigates to the campsite details information for the card clicked
                                }
                            )
                        }

                    }
                }
                else{
                    Box(modifier = Modifier.weight(1f)) {

                        if (realCampsites.isEmpty()) {//database info
                            // Show this if the database is empty or failed to load
                            Text("No campsites found in database.")
                        } else {
                            // Show the actual list of cards!
                            CampsiteListScreen(
                                campsiteList = realCampsites,
                                onItemClick = { clickedId ->
                                    navController.navigate("campsiteDetails/$clickedId") //navigates to the campsite details information for the card clicked
                                }
                            )
                        }

                    }
                }

            }
        }
    }
}


/// hello