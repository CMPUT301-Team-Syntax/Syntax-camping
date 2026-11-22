package com.example.syntax_camping

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.syntax_camping.UserScreens.CampsiteRulesLink
import com.example.syntax_camping.UserScreens.CancellationPolicyLink
import com.example.syntax_camping.UserScreens.campsiteInformationDestinations
import com.example.syntax_camping.ui.theme.SyntaxcampingTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CampsiteInformationScreensTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun campsiteRulesLinkShowsRulesAndBackReturnsToDetails() {
        showCampsiteDetailsLinks()

        text(R.string.campsite_rules_link).performClick()

        text(R.string.campsite_rules_title).assertIsDisplayed()
        text(R.string.campsite_information_sample).assertIsDisplayed()
        text(R.string.campsite_rules_check_in_body).performScrollTo().assertIsDisplayed()
        text(R.string.campsite_rules_quiet_hours_body).performScrollTo().assertIsDisplayed()
        text(R.string.campsite_rules_campfires_body).performScrollTo().assertIsDisplayed()
        text(R.string.cancellation_policy_title).assertDoesNotExist()

        compose.onNodeWithContentDescription("back button").performClick()
        compose.onNodeWithText("River Camp").assertIsDisplayed()
        text(R.string.campsite_rules_link).assertIsDisplayed()
        text(R.string.campsite_rules_title).assertDoesNotExist()
    }

    @Test
    fun cancellationPolicyLinkShowsRefundTermsAndBackReturnsToDetails() {
        showCampsiteDetailsLinks()

        text(R.string.cancellation_policy_link).performClick()

        text(R.string.cancellation_policy_title).assertIsDisplayed()
        text(R.string.campsite_information_sample).assertIsDisplayed()
        text(R.string.cancellation_policy_early_body).performScrollTo().assertIsDisplayed()
        text(R.string.cancellation_policy_late_body).performScrollTo().assertIsDisplayed()
        text(R.string.cancellation_policy_no_show_body).performScrollTo().assertIsDisplayed()
        text(R.string.campsite_rules_title).assertDoesNotExist()

        compose.onNodeWithContentDescription("back button").performClick()
        compose.onNodeWithText("River Camp").assertIsDisplayed()
        text(R.string.cancellation_policy_link).assertIsDisplayed()
        text(R.string.cancellation_policy_title).assertDoesNotExist()
    }

    private fun text(@StringRes resource: Int) = compose.onNodeWithText(
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resource)
    )

    private fun showCampsiteDetailsLinks() {
        compose.setContent {
            SyntaxcampingTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "campsiteDetails") {
                    composable("campsiteDetails") {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("River Camp")
                            CampsiteRulesLink(navController)
                            CancellationPolicyLink(navController)
                        }
                    }
                    campsiteInformationDestinations(navController)
                }
            }
        }
    }
}
