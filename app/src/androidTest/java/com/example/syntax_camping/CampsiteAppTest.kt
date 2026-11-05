package com.example.syntax_camping//package com.example.syntax_camping

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.syntax_camping.screens.CampsiteListScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class CampsiteAppTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    val fakeCampsites = listOf(
        Campsite(
            "1",
            "Pine Ridge Campground",
            "Banff, AB",
            "Mountain view",
            4,
            mutableListOf(),
            4.8
        ),
        Campsite("2", "Lakeside Haven", "Jasper, AB", "Lake view", 6, mutableListOf(), 4.5),
        Campsite("5", "Haven", "Jasper, AB", "wonderful veiw", 7, mutableListOf(), 4.5)
    )


    @Test
    fun testSearchHomeScreenInput() {
        // 1. Type into search bar
        composeTestRule
            .onNodeWithTag("search_input_field")
            .performTextInput("Jasper")

        // 2. Click search icon
        composeTestRule
            .onNodeWithTag("search_icon")
            .performClick()
        composeTestRule
            .onNodeWithText("Lakeside Haven")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Pine Ridge Campground")
            .assertDoesNotExist()
    }



    }


