package com.example.syntax_camping.screens.adminScreens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.tabs.AdminBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import coil3.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.CircularProgressIndicator
import com.example.syntax_camping.ImageDatabase
import com.example.syntax_camping.dataClasses.StoredImage
import kotlinx.coroutines.CancellationException
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Button
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun AdminBrowseImages(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()

    var searchImage by remember { mutableStateOf("") }
    var images by remember { mutableStateOf<List<StoredImage>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            images = ImageDatabase.getAllImages()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {   //in case this fails, throw an error.
            error = e.message ?: "Failed to load images"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF5F5F5),
        contentColor = Color.Black,
        topBar = { TopBar(navController) },
        bottomBar = { AdminBottomBar(navController, currentRoute = "adminBrowseImages") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            // Header Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Browse Images",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Search Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchImage,
                    onValueChange = { searchImage = it },
                    label = { Text("Type in the location or Campsite Name") },
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,   // Removes line when typing
                        unfocusedIndicatorColor = Color.Transparent, // Removes line when idle
                        disabledIndicatorColor = Color.Transparent   // Removes line if disabled
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_image_field"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        // keeps any near-me search that was already applied
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
            }

            // Main Content Area (Placeholder / Grid)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> { CircularProgressIndicator() }  //Loading animation
                    (error != null) -> {   //Error case
                        Text(
                            text = error ?: "Unknown error",
                            color = Color.Red
                        )
                    }
                    (images.isEmpty()) -> { Text("No images found.") }   //Empty image case
                    else -> {   //Normal case to display every image.
                        LazyColumn(modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp))
                        {
                            items(
                                items = images,
                                key = {it.storagePath}
                            ) { image ->

                                var ownerName by remember(image.category, image.parentId) {
                                    mutableStateOf("Loading...")
                                }
                                val date = SimpleDateFormat("yyyy-MM-dd", LocalLocale.current.platformLocale)
                                    .format(Date(image.uploadedAt))


                                //Try retrieving owner's name, otherwise fail.
                                LaunchedEffect(image.category, image.parentId) {
                                    try {
                                        ownerName = ImageDatabase.getImageOwnerName(image)
                                    } catch (error: CancellationException) {
                                        throw error
                                    } catch (e: Exception) {
                                        ownerName = "Unknown."
                                    }
                                }

                                //Displays each card.
                                CardToDisplay(
                                    uploadedBy = ownerName,
                                    postedOn = date,
                                    imageUrl = image.downloadUrl,
                                    imageName = image.name,
                                    imageCategory = image.category,
                                    onDeleteClick = {   //Delete implementation
                                        coroutineScope.launch {
                                            try {
                                                ImageDatabase.deleteImage(image)
                                                Log.d("ImageDatabase", "Deletion successful")
                                                images = images.filterNot {
                                                    it.storagePath == image.storagePath
                                                }

                                            } catch (e: CancellationException) {
                                                throw e
                                            } catch (e: Exception) {
                                                Log.e("ImageDatabase", "Deletion failed", e)
                                            }
                                        }
                                    },
                                    onViewProfileClick = {  //Navigate to different screens
                                        when (image.category) {
                                            "users" -> {
                                                navController.navigate("adminUserInfo/${image.parentId}")
                                            }
                                            "campsites" -> {
                                                navController.navigate("ownerCampsiteDetails/${image.parentId}")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardToDisplay(
    modifier: Modifier = Modifier,
    uploadedBy: String = "User",
    postedOn: String = "Today",
    imageUrl: String = "",
    imageName: String = "",
    imageCategory: String = "",
    onDeleteClick: () -> Unit = {},
    onViewProfileClick: () -> Unit = {}
) {

    var showImageDetails by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth()
            .clickable { showImageDetails = true },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Uploaded image",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(android.R.drawable.presence_invisible),
                    error = painterResource(android.R.drawable.presence_invisible),
                    modifier = Modifier.size(110.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Uploaded by: $uploadedBy", color = Color.Black, fontSize = 13.sp)
                    Text("Posted on: $postedOn", color = Color.Gray, fontSize = 13.sp)
                    Text("Category: $imageCategory", color = Color.Gray, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Tap to expand image",
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                //Three dot menu
                Box {
                    IconButton( onClick = { showMenu = true } ) {
                        Icon(
                            painter = painterResource(id=R.drawable.ic_three_dots),
                            contentDescription = "More options",
                            tint = Color.Black
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        //Delete picture button
                        DropdownMenuItem(
                            text = { Text(text = "Delete picture", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                showDeleteConfirmation = true
                            }
                        )
                        //Travel to user profile/campsite screen
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (imageCategory == "users") { "View user profile" }
                                    else { "View Campsite" }
                                )
                            },
                            onClick = {
                                showMenu = false
                                onViewProfileClick()
                            }
                        )
                    }
                }
            }
        }
    }

    //Enlarge image on click section.
    if (showImageDetails) {
        EnlargeImageDetails(
            imageUrl = imageUrl,
            uploadedBy = uploadedBy,
            postedOn = postedOn,
            imageName = imageName,
            imageCategory = imageCategory,
            onDismiss = { showImageDetails = false }
        )
    }

    //Delete image confirmation and results.
    if (showDeleteConfirmation) {
        AlertDialog( onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete picture?") },
            text =  {
                Text("Are you sure you want to permanently delete this image?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteClick()
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) { Text("Cancel")}
            }
        )
    }
}

//Section for enlarged images.
@Composable
fun EnlargeImageDetails(
    imageUrl: String,
    uploadedBy: String,
    postedOn: String,
    imageName: String,
    imageCategory: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Image details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(Modifier.height(16.dp))
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Enlarged image and details",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                )

                Spacer(Modifier.height(16.dp))
                Text( text = "Name: $uploadedBy", color = Color.Black )
                Text( text = "Posted on: $postedOn", color = Color.Black )
                Text( text = "Category: $imageCategory", color = Color.Black )
                Text( text = "File name: $imageName", color = Color.Black )

                Spacer(Modifier.height(16.dp))
                Button(onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Close") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardToDisplayPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        CardToDisplay(
            uploadedBy = "John Doe",
            postedOn = "2025-03-30"
        )
    }
}