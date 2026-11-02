package com.example.syntax_camping
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.Arrangement
/*
 *SO this photo carousel class takes in a list of uri photos as an argument and then has several functions allowing for the movement through of that list.
 * It also has a composable function built into it for displaying the photocarousel, which takes as argument an object of the photo carousel class(It needs to know what photocarousel it is moving through)
 *
 */
class PhotoCarousel(images: List<Uri>) {
    //Variable initialization
    val images = images
    var _currImageIndex = 0
//Returns the current image as a Uri object, returns null if there are no images in images though
    fun getCurrPhoto(): Uri? {
        if (images.size == 0)
            return null
        else
            return images[_currImageIndex]
    }

//Updates the current image index so next time current image is called it will be uopated, does nothing if there is 0or one images, and loops if necessary
    fun nextPhoto(){
        if ((images.size != 0) and (images.size!=1)) {
            if (_currImageIndex == images.size - 1) {
                _currImageIndex = 0


            } else {
                _currImageIndex += 1
            }
        }
    }
    //updates the current image to display the previous image, loops if necessary, and does nothing if there is only one photo or no photos
    fun previousPhoto() {
        if ((images.size != 0) and (images.size!=1)) {
            if (_currImageIndex == (0)) {
                _currImageIndex = images.size - 1


            } else  {
                _currImageIndex -= 1
            }
        }



    }

    //resets the photo index, should be called each time you leavve a page with an image caraousel
    fun resetIndex() {
        _currImageIndex = 0
    }

    //This composable takes as argument an instance of the photocarousel class so it can call the other functions in the clas on itself, has it's variables tracked within it

    @Composable
    fun displayCarousel(modifier: Modifier = Modifier, photoCarousel: PhotoCarousel) {
        //Variable tracked by the display method(the photo it is showing
        var currPhoto  by remember { mutableStateOf(photoCarousel.getCurrPhoto()) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box( modifier = Modifier
                .fillMaxWidth()
                .height(220.dp) ){
                //if images is empty, displays an image showing to upload/there is no photos
            if (photoCarousel.getCurrPhoto() == null) {
                    Image(
                        painter = painterResource(id = R.drawable.missing_image_filler),
                        contentDescription = "A description of the image for accessibility",
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(220.dp),


                    )
                } else {
                    AsyncImage(
                        model = currPhoto,
                        contentDescription = "Selected photo preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }
                //The next arrow
                IconButton(
                    onClick = {
                        photoCarousel.nextPhoto()
                        currPhoto = photoCarousel.getCurrPhoto()

                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(8.dp)
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.back),//tkaes the already made back arrow and just mirrors it
                        contentDescription = "next button",
                        tint = Color.Unspecified,  //without it the icon would be black
                        modifier = Modifier.scale(scaleX = -1f, scaleY = 1f)
                    )
                }


                //the back arrow
                IconButton(
                    onClick = {
                        photoCarousel.previousPhoto()
                        currPhoto = photoCarousel.getCurrPhoto()

                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(8.dp)
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "back button",
                        tint = Color.Unspecified  //without it the icon would be black
                    )
                }


            }

        }

    }
}