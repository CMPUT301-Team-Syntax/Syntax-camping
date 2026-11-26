package com.example.syntax_camping.screens.userScreens.profileScreen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.Database
import com.example.syntax_camping.R
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar


@Composable
fun UpdateProfileScreen(navController: NavController, userViewModel: UserViewModel, modifier: Modifier = Modifier) {
    var nameValue by remember { mutableStateOf("") }
    var emailValue by remember { mutableStateOf("") }
    var phoneValue by remember { mutableStateOf("") }

    // newly picked picture (not uploaded yet) and whether the saved one should be removed
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var removeSavedPicture by remember { mutableStateOf(false) }

    //Makes name, email, and phone display their previous setting
    val currentUser = userViewModel.currentUser
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            nameValue = user.name
            emailValue = user.email
            phoneValue = user.phoneNumber
        }
    }

    val savedUrl = currentUser?.profileImageUrl ?: ""
    // what the avatar shows right now: the new pick, else the saved picture (unless being removed)
    val shownPicture: Any? = pickedUri ?: savedUrl.takeIf { it.isNotBlank() && !removeSavedPicture }

    //Saving logic initialization
    val database = remember { Database() }
    var isSaving by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                pickedUri = uri
                removeSavedPicture = false
            }
        }
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        containerColor = Color.White,
        topBar = {
            TopBar(navController)
        },
        bottomBar = {
            CustomBottomBar(
                navController = navController,
                currentRoute = "Profile",
                backgroundColor = Color(0xFF3E6B68)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(33.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(96.dp)) {
                        // Avatar: tap it to pick a picture
                        ProfileAvatar(
                            model = shownPicture,
                            modifier = Modifier
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("updateProfileAvatar")
                        )

                        // Remove badge when there is a picture
                        if (shownPicture != null) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .clickable {
                                        pickedUri = null
                                        removeSavedPicture = true
                                    }
                                    .testTag("updateProfileRemovePhoto"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.delete_2_svgrepo_com),
                                    contentDescription = "Remove photo",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = " update Profile ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))

            CustomUnderlineField(
                value = nameValue,
                onValueChange = { nameValue = it },
                placeholder = "Full Name"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomUnderlineField(
                value = emailValue,
                onValueChange = { emailValue = it },
                placeholder = "Email Address"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomUnderlineField(
                value = phoneValue,
                onValueChange = { phoneValue = it },
                placeholder = "Phone Number"
            )

            Spacer(modifier = Modifier.height(150.dp))

            Button(
                //Saves the updated fields.
                onClick = {
                    currentUser?.let { user ->
                        isSaving = true   //Prevents you from spamming the save button.

                        // saves everything once we know which picture url to keep
                        fun saveWithPicture(pictureUrl: String) {
                            val updatedUser = user.copy(    //Make a copy of the user (we'll use this to update)
                                name = nameValue,
                                email = emailValue,
                                phoneNumber = phoneValue,
                                profileImageUrl = pictureUrl
                            )

                            //Saving logic block.
                            database.updateUser(userViewModel.deviceId, updatedUser) { result ->
                                isSaving = false

                                result.onSuccess {
                                    Log.d("UserDebug", "Profile updated successfully!")
                                    userViewModel.refreshCurrentUser()  //Refresh current user's data.
                                    navController.popBackStack() //Go back a page.
                                }.onFailure { error ->
                                    Log.e("UserDebug", "Failed to update profile", error)
                                }
                            }
                        }

                        val newPicture = pickedUri
                        when {
                            // a new picture was picked: upload it first, then save its url
                            newPicture != null -> {
                                database.uploadProfileImage(userViewModel.deviceId, newPicture) { uploadResult ->
                                    uploadResult.onSuccess { url ->
                                        saveWithPicture(url)
                                    }.onFailure { error ->
                                        isSaving = false
                                        Log.e("UserDebug", "Failed to upload profile picture", error)
                                    }
                                }
                            }
                            // the saved picture was removed
                            removeSavedPicture -> saveWithPicture("")
                            // nothing changed about the picture
                            else -> saveWithPicture(user.profileImageUrl)
                        }
                    }
                },

                enabled = currentUser != null && !isSaving,  //Button is disabled when its saving.
                shape = RoundedCornerShape(33.dp),
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 7.dp)
            ) {
                Text(
                    text = if (isSaving) "Saving..." else "Save",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun CustomUnderlineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = modifier.onFocusChanged { isFocused = it.isFocused },
        decorationBox = { innerTextField ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, color = Color.Gray)
                    }
                    innerTextField()
                }
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(
                    color = if (isFocused) Color(0xFF3E6B68) else Color.LightGray,
                    thickness = if (isFocused) 2.dp else 1.dp
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun UpdateProfilePreview() {
    UpdateProfileScreen(
        navController = TODO(),
        userViewModel = TODO(),
        modifier = TODO()
    )
}