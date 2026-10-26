package com.example.syntax_camping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
//TO be added to the correct Navigation calss (ie syntax camping
/*
At Top var selectedCampsite by remember {
        mutableStateOf<Campsite?>(null)
    }
composable( "UpdateCamp"){ CampsiteEditingScreen(navController,
            campsite = selectedCampsite)}

* */
import androidx.navigation.NavController
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Image
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import kotlin.String


@Composable
fun CampsiteEditingScreen(navController: NavController, campsite: Campsite?){
    /*ToDo: fIRST Copy this template for all other screens, the scaffold is the same for all screens*/
    /*ToDo: SECOND add tests as well for buttons and information on actual screen using the testtag, Can be done second week*/
    val oldLocation: String
    var qrCodeChecked: Boolean
    val oldName: String
    val oldDescription: String
    val oldMaxOcc: Int
    var oldTags = mutableListOf<String>()
    var oldImages = mutableListOf<Photo>()
    val locationDisplay: String
    val nameDisplay: String
    val descriptionDisplay: String
    val maxOccDisplay: String
    val scrollState = rememberScrollState()
    var error by remember {mutableStateOf("")}
    var stringRepOfTags = ""
    if (campsite == null){
        oldLocation = ""
        locationDisplay = "Enter campsite Location"
        oldName = ""
        qrCodeChecked = false
        nameDisplay = "Enter campsite Name"
        oldDescription = ""
        descriptionDisplay = "Enter campsite Description"
        oldMaxOcc = 0
        maxOccDisplay = "Enter campsite Maximum Occupancy"

    }
    else{
        oldLocation  = campsite.getLocation()
        locationDisplay = oldLocation
        oldName = campsite.getName()
        nameDisplay = oldName
        oldDescription = campsite.getDescription()
        descriptionDisplay = oldDescription
        oldMaxOcc = campsite.getmaxOcc()
        maxOccDisplay = oldMaxOcc.toString()
        qrCodeChecked = campsite.getQRCode()
        oldTags = campsite.getTags()
        oldImages = campsite.getPhotos()
    }
    var newCampName by remember { mutableStateOf("") }
    var newCampLocation by remember { mutableStateOf("") }
    //name: String, location: String, description: String, maxOcc: Int, tags: MutableList<String>
    var newCampDescription by remember { mutableStateOf("") }
    var newMaxOcc by remember {mutableStateOf("")}
    val newTags = remember {mutableStateListOf<String>()}
    var newTag  by remember { mutableStateOf("") }
    for (item in oldTags){
        newTags.add(item)
    }
    //var displayTag by remember{mutableStateOf(newTags.joinToString())}
    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Set the background color to white
        containerColor = Color.White,
        topBar = { // top bar with back button and notification icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFFE2AFAF)), // sets the background color to pink lkie the figma mockups
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                //icon button for back button, template for adding icons

                IconButton(

                    onClick = { /* TODO: Navigate to notifications */ },
                    modifier = Modifier.size(100.dp) // the size is defaulted to 44.dp which is pretty small so you can change this if needed for other icons

                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "back button",
                        tint = Color.Unspecified  //without it the icon would be black
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    IconButton(onClick = { /* TODO: Navigate to add */ },
                        modifier = Modifier.size(40 .dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.add),
                            contentDescription = "notifications",
                            tint = Color.Unspecified
                        )
                    }
                    IconButton(onClick = { /* TODO: Navigate to notifications */ },
                        modifier = Modifier.size(100.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.notification),
                            contentDescription = "notifications",
                            tint = Color.Unspecified
                        )
                    }
                }

            }
        },

        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFE2AFAF))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {


                IconButton(onClick = { navController.navigate("home") },
                    modifier = Modifier.size(80.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "Search",
                        tint = Color.Unspecified
                    )
                }

                IconButton(onClick = { navController.navigate("lottery") },
                    modifier = Modifier.size(80.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.lottery),
                        contentDescription = "Search",
                        tint = Color.Unspecified
                    )
                }

                IconButton(onClick = { navController.navigate("messenger") },
                    modifier = Modifier.size(80.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.messenger),
                        contentDescription = "Reservations",
                        tint = Color.Unspecified
                    )
                }

                IconButton(onClick = { navController.navigate("Profile") },
                    modifier = Modifier.size(80.dp)
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.account),
                        contentDescription = "Account",
                        tint = Color.Unspecified
                    )
                }

                IconButton(onClick = { navController.navigate("Settings") },
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = Color.Unspecified,
                    )
                }

            }
        }
    ) { innerPadding ->

        //inside the scaffold, this would be the content of the screen, start editing screen content from in here

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(16.dp),

            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //These first few rows handle the Name and location textboxes
            Row(
                modifier = Modifier.fillMaxWidth()

            ) {
                OutlinedTextField(
                    value = newCampName,
                    onValueChange = { newCampName = it },
                    label = { Text(nameDisplay) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = newCampLocation,
                    onValueChange = { newCampLocation = it },
                    label = { Text(locationDisplay) },
                    modifier = Modifier.weight(1f)
                )
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
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        //A filler Image for map until map is updated later down the road
                        Image(
                            painter = painterResource(id = R.drawable.map_filler_image),
                            contentDescription = "A description of the image for accessibility",
                            modifier = Modifier.size(150.dp)
                        )
                    }
                    //A check box for Qr code, not currently implemented as we are oding Qr Code at a later date
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Checkbox(
                            checked = qrCodeChecked,
                            onCheckedChange = { checked ->
                                qrCodeChecked = true // Update the state when clicked
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
                )
            }
            //THe following few rows handle tags, displaying current tags and letting users add and remove tags at their whim
            Row(
                modifier = Modifier.fillMaxWidth()

            ) {
                Text(
                    text = "Please enter a tag you wish to have added or removed, then press the corresponding button"
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth()

            ) {
                Text(
                    text = "current tags: "
                )
                Text(
                    text = newTags.joinToString(", ")
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth()

            ) {
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    label = { Text("Enter a tag") },
                    modifier = Modifier.weight(1f)

                )
            }
            Row(
                modifier = Modifier.fillMaxWidth()

            ) {
                Button(
                    modifier = Modifier.padding(vertical = 12.dp),
                    onClick = {
                        if (newTag.isNotBlank() and (newTag !in newTags)) {
                            newTags.add(newTag)
                            newTag = ""

                        }
                    }
                ) {
                    Text("Add Tag")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    modifier = Modifier.padding(vertical = 12.dp),
                    onClick = {
                        if (newTag.isNotBlank() and (newTag in newTags)) {
                            newTags.remove(newTag)
                            newTag = ""
                        }
                    }
                ) {
                    Text("remove Tag")
                }
            }
            //The following will Hand LE images
            //TODO
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Please select images you would like to upload by tapping below"
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.missing_image_filler),
                    contentDescription = "A description of the image for accessibility",
                    modifier = Modifier.size(300.dp)
                )


            }
            //The following is to handle updating/creating the cmapsite
            //Each button has a couple of if statemnets to make sure none of the required fields are blank/max occupancy entered is a number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if(campsite == null) {


                    Button(
                        modifier = Modifier.padding(vertical = 12.dp),
                        onClick = {
                            if (newTags.isNotEmpty() and (newCampName != "") and (newCampLocation != "") and (newCampDescription !="") and (newMaxOcc != "")) {
                                val intNewMaxOcc = newMaxOcc.toIntOrNull()
                                error = ""
                                if (intNewMaxOcc == null) {
                                    error = "Max Int must be a string"
                                } else {
                                    error = ""
                                    val newCampsite = Campsite(
                                        newCampName,
                                        newCampLocation,
                                        newCampDescription,
                                        intNewMaxOcc,
                                        newTags
                                    )
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
                        modifier = Modifier.padding(vertical = 12.dp),
                        onClick = {
                            if (newTags.isNotEmpty() and (newCampName != "") and (newCampLocation != "") and (newCampDescription !="") and (newMaxOcc != "")) {
                                val intNewMaxOcc = newMaxOcc.toIntOrNull()
                                error = ""
                                if (intNewMaxOcc == null) {
                                    error = "Max Int must be a string"
                                } else {
                                    error = ""
                                    campsite.updateName(newCampName)
                                    campsite.updateLocation(newCampLocation)
                                    campsite.updateDescription(newCampDescription)
                                    campsite.updateMaxOcc(intNewMaxOcc)


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
                if( (error != "") ){
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$error"
                        )
                    }
                }

            }
        }
    }
}
