package com.example.syntax_camping
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class PhotoCarouselTest {

    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.v2.createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testCarouselWithRealImages() {
        val images = listOf(
            "IMG_6503.jpeg",
            "IMG_6934.jpeg",
            "IMG_7175.jpeg",
            "IMG_7625.jpeg",
            "IMG_7655.jpeg",
            "IMG_7659.jpeg",
            "IMG_7672.jpeg"
        ).map {filename ->
            Uri.parse("file:///android_asset/Campsitetesterphotos/$filename")
        }

        val carousel = PhotoCarousel(images)
        composeTestRule.setContent {
            carousel.displayCarousel(photoCarousel = carousel)
        }

        //Initially shows the first image.
        composeTestRule.runOnIdle {
            assertEquals(images[0], carousel.getCurrPhoto())
        }

        //User presses next, (expected images[1])
        composeTestRule
            .onNodeWithContentDescription("next button")
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(images[1], carousel.getCurrPhoto())
        }

        //User presses previous (expected images[0])
        composeTestRule
            .onNodeWithContentDescription("back button")
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(images[0], carousel.getCurrPhoto())
        }

        //User presses previous to loop back at the last image (expected images[6])
        composeTestRule
            .onNodeWithContentDescription("back button")
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(images[6], carousel.getCurrPhoto())
        }

        //User presses next to loop back to the first image (expected images[0])
        composeTestRule
            .onNodeWithContentDescription("next button")
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(images[0], carousel.getCurrPhoto())
        }
    }

    //Test the case where there are no images.
    @Test
    fun emptyCarouselDisplayFallback() {
        val carousel = PhotoCarousel(emptyList())

        //Try displaying the carousel.
        composeTestRule.setContent {
            carousel.displayCarousel(photoCarousel = carousel)
        }

        //Verify that no current photo exists.
        composeTestRule.runOnIdle {
            assertNull(carousel.getCurrPhoto())
        }

        //Verify that it uses the fallback image.
        composeTestRule
            .onNodeWithContentDescription("A description of the image for accessibility")
            .assertExists()
    }

}