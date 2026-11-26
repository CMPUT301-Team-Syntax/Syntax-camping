package com.example.syntax_camping.screens.userScreens.profileScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/** A user's picture as a rounded square, or a beige block if they have none. */
@Composable
fun ProfileAvatar(
    model: Any?,                       // a url String, a local Uri, or null/blank for none
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    val shown: Any? = if (model is String) model.takeIf { it.isNotBlank() } else model
    val shape = modifier
        .size(size)
        .clip(RoundedCornerShape(16.dp))

    if (shown != null) {
        AsyncImage(
            model = shown,
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = shape
        )
    } else {
        Box(modifier = shape.background(Color(0xFFF4E8D5)))
    }
}