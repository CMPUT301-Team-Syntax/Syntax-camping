package com.example.syntax_camping//package com.example.syntax_camping

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.syntax_camping.UserScreens.SearchHomeScreen
import com.example.syntax_camping.screens.CampsiteListScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteEditingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class CampsiteAppTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    @Test
    fun addnewCampsiteTest_and_FilterIt() {

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("tab_CampsitesHome")
            .performClick()




    }



}


//        composeTestRule.setContent {
//            val navController = androidx.navigation.compose.rememberNavController()
//            SearchHomeScreen(navController = navController)
//        }
//        // 1. Type into search bar
//        composeTestRule
//            .onNodeWithTag("search_input_field")
//            .performTextInput("Jasper")
//
//        // 2. Click search icon
//        composeTestRule
//            .onNodeWithTag("search_icon")
//            .performClick()
//        composeTestRule
//            .onNodeWithText("Lakeside Haven")
//            .assertIsDisplayed()
//        composeTestRule
//            .onNodeWithText("Pine Ridge Campground")
//            .assertDoesNotExist()


