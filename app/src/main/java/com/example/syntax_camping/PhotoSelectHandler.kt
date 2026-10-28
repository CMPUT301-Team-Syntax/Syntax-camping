package com.example.syntax_camping

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun photoPicker(){
    var  selectedPhotos by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }
    val launcher =  rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    )
    { uris ->
        selectedPhotos = uris
    }
    launcher.launch(
        PickVisualMediaRequest(
            ActivityResultContracts
                .PickVisualMedia.ImageOnly
        )
    )
}