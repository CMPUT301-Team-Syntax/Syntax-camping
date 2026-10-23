package com.example.syntax_camping
import coil3.compose.AsyncImage
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
/*
* This class takes as argument a URL for an image. since images are kind of wierd to handle in Kotlin I made a sepertae class for them, so for profile picture, etc use an image
 */
class Image(url: String) {
    private val _url = url
    @Composable
    fun Displayimage() {
        AsyncImage(
            model = "https://example.com/image.jpg",
            contentDescription = "Description of the image",
            modifier = Modifier.fillMaxWidth()
        )
    }
    fun getUrl(): String{
        return _url
    }
}