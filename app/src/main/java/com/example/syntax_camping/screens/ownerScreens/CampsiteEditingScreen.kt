package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.NavController
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import com.example.syntax_camping.Campsite
import kotlin.String

import com.example.syntax_camping.Database
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.syntax_camping.PhotoCarousel
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.OwnerData
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.OutlinedButton
import com.example.syntax_camping.dataClasses.DateRepository
import com.example.syntax_camping.screens.CalenderVeiw
import com.example.syntax_camping.screens.ownerScreens.geocodeAddress
import com.example.syntax_camping.screens.ownerScreens.suggestAddresses
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//import androidx.compose.ui.Modifier
/*
* This screen is called when the plus to add campsite is pressed in the my campsites page, or when
*Teh update campsite button is pressed in CampsiteOwnerDetails or campsite owner campsiteview, three dot drop down.
*It works with the database to add a campsite to the database, or update one
* */
@Composable
fun CampsiteEditingScreen(navController: NavController, userViewModel: UserViewModel, campsite: Campsite?){

    //Note that campsite inof has two variable representations, one is for the labels of text fields and is labelled as display before what it displays, the others are for actually storing and changing what that variable display
    //ie newCampName stores the new name that will be passed when the camp is updated/created, while displayName is fo rthe text field label and determines what it displays(As it may be please enter name here or the old name)
    //A
    val db = Database()
    //var newCampsite: Campsite
    var documentID: String
    var qrCodeChecked by remember(campsite?.documentId) {
        mutableStateOf(campsite?.hasQRCode ?: false)
    }
    var nameDisplay by remember { mutableStateOf("") }
    var locationDisplay by remember { mutableStateOf("") }
    var descriptionDisplay by remember { mutableStateOf("") }
    var maxOccDisplay by remember {mutableStateOf("")}
    val scrollState = rememberScrollState()
    var error by remember {mutableStateOf("")}
    var intNewMaxOcc: Int?
    var newCampName by remember { mutableStateOf("") }
    var newCampLocation by remember { mutableStateOf("") }
    var newCampDescription by remember { mutableStateOf("") }
    var newMaxOcc by remember {mutableStateOf("")}
    val newTags = remember {mutableStateListOf<String>()}
    val newPhotos = remember {mutableStateListOf<Uri>()}
    var newTag  by remember { mutableStateOf("") }
    var page by rememberSaveable { mutableIntStateOf(1) }
    var pickMode by rememberSaveable { mutableStateOf("available") }   // "available" or "unavailable"
    var pickedDates by remember { mutableStateOf<List<String>>(emptyList()) }
    var datesConfirmed by remember { mutableStateOf(false) }
    var newLat by remember { mutableStateOf<Double?>(null) }          // map position of the address
    var newLng by remember { mutableStateOf<Double?>(null) }
    var geocodedFor by remember { mutableStateOf<String?>(null) }     // the address text newLat/newLng belong to
    var locating by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<Triple<String, Double, Double>>>(emptyList()) }   // address suggestions as the owner types
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currImage: Uri?
    currImage = null

    LaunchedEffect(page) { scrollState.scrollTo(0) }

    // As the owner types the address, wait a moment after they stop, then look up suggestions.
    // Nothing is suggested when the text is already a chosen/found address.
    LaunchedEffect(newCampLocation) {
        if (newCampLocation.length < 3 || newCampLocation == geocodedFor) {
            suggestions = emptyList()
        } else {
            delay(500)
            val typed = newCampLocation
            suggestions = withContext(Dispatchers.IO) { suggestAddresses(context, typed) }
        }
    }
    //Note the following is a list of selected photos, initialized as empty
    // var  selectedPhotos by remember {
    //    mutableStateOf<List<Uri>>(emptyList())
    // }
    //Note this launcher is used for photo selection, it activates androids built in photo selector, and stores the selected URI's to a list(selected photos)
    val launcher =  rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    )
    { uris ->
        newPhotos.addAll(uris.filter {it !in newPhotos })
    }
    var photoCarousel = PhotoCarousel(newPhotos)
    //This launched effect is to ensure that certain values are only initialized, not cconstantly set to a new value everytime the ccomposable is called(prevents bug where you could not actually
    //edit text fields since they ckept getting reset to campsite name values)
    LaunchedEffect(campsite?.documentId){
        //If we are creating a cmapsite
        if (campsite == null){
            locationDisplay = "Start typing the real address"
            nameDisplay = "Enter campsite Name"
            descriptionDisplay = "Enter campsite Description"
            maxOccDisplay = "Enter campsite Maximum Occupancy"
        }
        //If a campsite already ecxists
        else{
            newCampLocation  = campsite.location
            locationDisplay = newCampLocation
            newCampName = campsite.name
            nameDisplay = campsite.name
            newCampDescription = campsite.description
            descriptionDisplay = newCampDescription
            newMaxOcc = campsite.maxOcc.toString()
            maxOccDisplay = newMaxOcc

            newLat = campsite.latitude
            newLng = campsite.longitude
            geocodedFor = if (campsite.latitude != null && campsite.longitude != null) campsite.location else null

            newPhotos.clear()
            newPhotos.addAll(campsite.images)

            newTags.clear()
            newTags.addAll(campsite.tags)
            pickedDates = campsite.dateManager?.allDates?.map { it.dateString } ?: emptyList()
            pickMode = "available"
            datesConfirmed = pickedDates.isNotEmpty()

        }
    }
    /**
     * Accepts the images the user wants to keep.
     * Determines which images are new.
     * Uploads new images to Firebase Storage.
     * Waits for every upload to finish.
     * Returns the complete list of download URL strings, including existing images.
     */
    fun uploadNewImages(
        campsiteId: String,
        originalImages: List<Uri>,
        imagesToSave: List<Uri>,
        callback: (Result<List<String>>) -> Unit
    ) {
        val newImages = imagesToSave.filter { it !in originalImages }

        if (newImages.isEmpty()) {
            callback(Result.success(imagesToSave.map { it.toString() }))
            return
        }

        val uploadedUrls = mutableMapOf<Uri, String>()
        var completedUploads = 0
        var uploadFailed = false

        for (image in newImages) {
            db.uploadCampsiteImage(campsiteId, image) { result ->
                if (!uploadFailed) {
                    result.onSuccess { downloadUrl ->
                        uploadedUrls[image] = downloadUrl
                        completedUploads++

                        if (completedUploads == newImages.size) {
                            val finalUrls = imagesToSave.map { uri ->
                                uploadedUrls[uri] ?: uri.toString()
                            }

                            callback(Result.success(finalUrls))
                        }
                    }

                    result.onFailure { exception ->
                        uploadFailed = true
                        callback(Result.failure(exception))
                    }
                }
            }
        }
    }

    //var displayTag by remember{mutableStateOf(newTags.joinToString())}
    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Set the background color to white
        containerColor = Color.White,
        topBar = { // top bar with back button and notification icon
            TopBar(
                navController
            )
        },

        bottomBar = {
            CustomBottomBar(navController,
                "add")
        }
    )
    { innerPadding ->

        //inside the scaffold, this would be the content of the screen, start editing screen content from in here
        //THis column handles all of the main display stuff
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)

                .padding(16.dp),

            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (page == 1) {
                //These first few rows handle the Name and location textboxes
                Row(
                    modifier = Modifier.fillMaxWidth()

                ) {
                    OutlinedTextField(
                        value = newCampName,
                        onValueChange = { newCampName = it },
                        label = { Text(nameDisplay) },
                        modifier = Modifier.weight(1f).testTag("campsiteNameInput")
                    )
                    //The location box is the real address. Suggestions appear under it as you type.
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = newCampLocation,
                        onValueChange = { newCampLocation = it },
                        label = { Text(locationDisplay) },
                        modifier = Modifier.weight(1f).testTag("campsiteLocationInput")
                    )
                }

                // Address suggestions: tap one to use it (this also saves its map position)
                if (suggestions.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .background(Color(0xFFF4E8D5))
                            .testTag("addressSuggestions")
                    ) {
                        suggestions.forEachIndexed { index, suggestion ->
                            Text(
                                text = suggestion.first,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        newCampLocation = suggestion.first
                                        newLat = suggestion.second
                                        newLng = suggestion.third
                                        geocodedFor = suggestion.first
                                        suggestions = emptyList()
                                        error = ""
                                    }
                                    .padding(12.dp)
                                    .testTag("addressSuggestion_$index")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()

                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()


                            .padding(4.dp),

                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        //Embeded row to allow the Qr Code and max occupancy to stack on top of each other like in our planning images
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ){
                            OutlinedTextField(
                                value = newMaxOcc ,
                                onValueChange = { newMaxOcc = it },
                                label = { Text(maxOccDisplay) },
                                modifier = Modifier.weight(1f).testTag("campsiteMaximumOccupantInput")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            //A filler Image for map until map is updated later down the road
                            /*
                            Image(
                                painter = painterResource(id = R.drawable.map_filler_image),
                                contentDescription = "A description of the image for accessibility",
                                modifier = Modifier.size(150.dp)
                            )
                            */

                        }
                        //A checkbox for Qr code, hopefully implemented
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ){
                            Checkbox(
                                checked = qrCodeChecked,
                                onCheckedChange = { checked ->
                                    qrCodeChecked = checked // Update the state when clicked
                                }
                            )
                            Text(text = "Generate QR Code")
                        }
                    }


                }
                //This is a scrollable text boxx with a set space on screen for acmp description. If description is too long, the text box can be scrolled through
                Row(
                    modifier = Modifier.fillMaxWidth()

                ) {
                    OutlinedTextField(
                        value = newCampDescription,
                        onValueChange = { newCampDescription = it },
                        label = { Text(descriptionDisplay) },
                        modifier = Modifier.weight(1f)
                            .heightIn(min = 350.dp, max = 350.dp)
                            // Enables the vertical scroll behavior inside the text box
                            .verticalScroll(rememberScrollState())
                            .testTag("campsiteDescriptionInput")

                    )
                }
                //THe following few rows handle tags, displaying current tags and letting users add and remove tags at their whim
                // Tags: type one and press +. Every added tag gets its own line with a − to remove it.
                Text(text = "Tags", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        label = { Text("Enter a tag") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("campsiteTagInput")
                    )
                    Button(
                        modifier = Modifier.padding(start = 8.dp).testTag("addTagButton"),
                        onClick = {
                            val cleaned = newTag.replace(",", "").trim()
                            if (cleaned.isNotBlank() && cleaned !in newTags) {
                                newTags.add(cleaned)
                            }
                            newTag = ""
                        }
                    ) {
                        Text("+")
                    }
                }
                newTags.toList().forEach { tag ->
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("tagRow_$tag"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = tag)
                        TextButton(
                            onClick = { newTags.remove(tag) },
                            modifier = Modifier.testTag("removeTag_$tag")
                        ) {
                            Text("−", fontSize = 22.sp)
                        }
                    }
                }
                //The following will Handle images

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Please select images you would like to upload by tapping below"
                    )
                }
                //This row calls photo carousel display funciton, passing an argument of the photocarousel onject created on this page
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {

                    currImage =   photoCarousel.displayCarousel(Modifier,photoCarousel)

                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center

                ) {
                    Button(
                        modifier = Modifier.padding(vertical = 12.dp),
                        //This onclick handles the launcher for Android's built in image selector.
                        onClick = {
                            launcher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts
                                        .PickVisualMedia.ImageOnly
                                )
                            )


                        }
                    ) {
                        Text("Add Images")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center

                ) {
                    Button(
                        modifier = Modifier.padding(vertical = 12.dp),
                        //This onclick handles the launcher for Android's built in image selector.
                        onClick = {
                            if((newPhotos.isNotEmpty()) && (currImage != null)){
                                newPhotos.remove(currImage)
                                photoCarousel = PhotoCarousel(newPhotos)
                            }
                        }
                    ) {
                        Text("Remove Image")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier.padding(vertical = 12.dp).testTag("nextPageButton"),
                        enabled = !locating,
                        onClick = {
                            if (newTags.isNotEmpty() && newCampName != "" && newCampLocation != "" &&
                                newCampDescription != "" && newMaxOcc != ""
                            ) {
                                if (newMaxOcc.toIntOrNull() == null) {
                                    error = "Max occupancy must be a number"
                                } else if (geocodedFor == newCampLocation && newLat != null && newLng != null) {
                                    // this address was already found on the map (picked from the suggestions or found before)
                                    error = ""
                                    page = 2
                                } else {
                                    error = ""
                                    locating = true
                                    val addressToFind = newCampLocation
                                    scope.launch {
                                        val found = withContext(Dispatchers.IO) {
                                            geocodeAddress(context, addressToFind)
                                        }
                                        locating = false
                                        if (found == null) {
                                            error = "We couldn't find that address on the map. Pick one from the suggestions or try a fuller address."
                                        } else {
                                            newLat = found.first
                                            newLng = found.second
                                            geocodedFor = addressToFind
                                            error = ""
                                            page = 2
                                        }
                                    }
                                }
                            } else {
                                error = "All fields must be filled in"
                            }
                        }
                    ) {
                        Text(if (locating) "Finding address..." else "Next")
                    }
                }
            }   // end of page 1

            if (page == 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = qrCodeChecked,
                        onCheckedChange = { checked -> qrCodeChecked = checked }
                    )
                    Text(text = "Generate QR Code")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Availability", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Choose what kind of dates you will tap, then tap them and press Confirm.")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { if (pickMode != "available") { pickMode = "available"; pickedDates = emptyList() } },
                        enabled = !datesConfirmed,
                        modifier = Modifier.weight(1f).testTag("pickAvailableMode"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pickMode == "available") Color(0xFF2E7D32) else Color.LightGray,
                            disabledContainerColor = if (pickMode == "available") Color(0xFF2E7D32) else Color.LightGray
                        )
                    ) { Text("Pick available dates", color = Color.White) }
                    Button(
                        onClick = { if (pickMode != "unavailable") { pickMode = "unavailable"; pickedDates = emptyList() } },
                        enabled = !datesConfirmed,
                        modifier = Modifier.weight(1f).testTag("pickUnavailableMode"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pickMode == "unavailable") Color(0xFFA13F3F) else Color.LightGray,
                            disabledContainerColor = if (pickMode == "unavailable") Color(0xFFA13F3F) else Color.LightGray
                        )
                    ) { Text("Pick unavailable dates", color = Color.White) }
                }
                Text(
                    if (pickMode == "available")
                        "Green dates are available. Every other date is unavailable."
                    else
                        "Red dates are unavailable. Every other date in the next 6 months is available.",
                    modifier = Modifier.testTag("availabilityHint")
                )
                CalenderVeiw(
                    initialSelected = pickedDates,
                    selectedColor = if (pickMode == "available") Color(0xFF2E7D32) else Color(0xFFA13F3F),
                    startLocked = datesConfirmed,
                    onConfirm = { pickedDates = it; datesConfirmed = true },
                    onEdit = { datesConfirmed = false }
                )
                TextButton(
                    onClick = { page = 1 },
                    modifier = Modifier.testTag("backToDetailsButton")
                ) { Text("Back") }
            }
            //The following is to handle updating/creating the cmapsite
            //Each button has a couple of if statemnets to make sure none of the required fields are blank/max occupancy entered is a number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (page == 2) {
                    if(campsite == null) {


                        Button(
                            modifier = Modifier.padding(vertical = 12.dp)
                                .testTag("createCampsiteButton")
                            ,
                            onClick = {
                                if (!datesConfirmed || pickedDates.isEmpty()) {
                                    error = "Confirm at least one date on the calendar first"
                                    return@Button
                                }
                                if (newTags.isNotEmpty() and (newCampName != "") and (newCampLocation != "") and (newCampDescription !="") and (newMaxOcc != "")) {
                                    val intNewMaxOcc = newMaxOcc.toIntOrNull()
                                    error = ""
                                    if (intNewMaxOcc == null) {
                                        error = "Max Int must be a string"
                                    }

                                    else {
                                        error = ""
                                        documentID = FirebaseFirestore.getInstance()
                                            .collection("campsites")
                                            .document()
                                            .id


                                        val user = userViewModel.currentUser

                                        if (user == null) {
                                            error = "Your account hasn't loaded yet. Please try again."
                                        } else {
                                            uploadNewImages(
                                                campsiteId = documentID,
                                                originalImages = emptyList(),
                                                imagesToSave = newPhotos.toList()
                                            ) { result ->

                                                result.onSuccess { imageUrls ->
                                                    val newCampsite = Campsite(
                                                        documentId = documentID,
                                                        name = newCampName,
                                                        location = newCampLocation,
                                                        description = newCampDescription,
                                                        maxOcc = intNewMaxOcc,
                                                        tags = newTags.toMutableList(),
                                                        rating = 0.0,
                                                        images = imageUrls.toMutableList()
                                                    )
                                                    newCampsite.updateCoordinates(newLat, newLng)
                                                    newCampsite.updateDateManager(
                                                        buildCampsiteDates(null, datesForMode(pickMode, pickedDates), intNewMaxOcc)
                                                    )
                                                    newCampsite.updateQRCode(qrCodeChecked)  //New QR Code update.
                                                    db.createNewCampsite(newCampsite)

                                                    val oldOwnerData = user.ownerData ?: OwnerData()

                                                    val updatedUser = user.copy(
                                                        ownerData = oldOwnerData.copy(
                                                            campsiteIds = (
                                                                    oldOwnerData.campsiteIds +
                                                                            newCampsite.documentId
                                                                    ).toMutableList()
                                                        )
                                                    )

                                                    db.updateUser(
                                                        userViewModel.deviceId,
                                                        updatedUser
                                                    ) { updateResult ->
                                                        updateResult.onSuccess {
                                                            userViewModel.refreshCurrentUser()
                                                            navController.popBackStack()
                                                        }

                                                        updateResult.onFailure { exception ->
                                                            error = exception.message
                                                                ?: "Failed to update owner data"
                                                        }
                                                    }
                                                }

                                                result.onFailure { exception ->
                                                    error = exception.message
                                                        ?: "Failed to upload campsite images"
                                                }
                                            }
                                        }

                                    }
                                }
                                else{
                                    error = "All fields must be filled in"
                                }
                            }
                        ) {
                            Text("create campsite")
                        }
                    }
                    else{


                        Button(
                            modifier = Modifier.padding(vertical = 12.dp).testTag("createCampsiteButton"),
                            onClick = {
                                if (!datesConfirmed || pickedDates.isEmpty()) {
                                    error = "Confirm at least one date on the calendar first"
                                    return@Button
                                }
                                val db = Database()
                                if (newTags.isNotEmpty() and (newCampName != "") and (newCampLocation != "") and (newCampDescription !="") and (newMaxOcc != "")) {
                                    intNewMaxOcc = newMaxOcc.toIntOrNull()
                                    error = ""
                                    if (intNewMaxOcc == null) {
                                        error = "Max Int must be an int"
                                    } else {
                                        error = ""

                                        val originalImages = campsite.images.toList()
                                        val imagesToSave = newPhotos.toList()

                                        uploadNewImages(
                                            campsiteId = campsite.documentId,
                                            originalImages = originalImages,
                                            imagesToSave = imagesToSave
                                        ) { result ->

                                            result.onSuccess { imageUrls ->
                                                campsite.updateName(newCampName)
                                                campsite.updateLocation(newCampLocation)
                                                campsite.updateCoordinates(newLat, newLng)
                                                campsite.updateDescription(newCampDescription)
                                                campsite.updateMaxOcc(intNewMaxOcc!!)
                                                campsite.updateTag(newTags.toMutableList())
                                                campsite.updateQRCode(qrCodeChecked)  //QR CODE
                                                val newManager = buildCampsiteDates(
                                                    campsite.dateManager,
                                                    datesForMode(pickMode, pickedDates),
                                                    intNewMaxOcc!!
                                                )
                                                campsite.updateDateManager(newManager)
                                                DateRepository().addDateManager(campsite.documentId, newManager)

                                                // Replace the old image list with the final URLs.
                                                campsite.images.clear()

                                                for (url in imageUrls) {
                                                    campsite.addImage(Uri.parse(url))
                                                }

                                                // Save the updated campsite to Firestore.
                                                db.updateCampsite(campsite)
                                                navController.popBackStack()
                                            }

                                            result.onFailure { exception ->
                                                error = exception.message
                                                    ?: "Failed to upload campsite images"
                                            }
                                        }

                                    }
                                }
                                else{
                                    error = "All fields must be filled in"
                                }
                            }
                        ) {
                            Text("update campsite")
                        }
                    }
                    //If there is na error in the info entered by the user, tells them


                }
            }
            if( (error != "") ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$error"
                    )
                }
            }
        }


    }
}