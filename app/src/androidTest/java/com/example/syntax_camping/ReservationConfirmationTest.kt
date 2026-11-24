package com.example.syntax_camping

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.UserScreens.ReservationConfirmationControls
import com.example.syntax_camping.UserScreens.ReservationConfirmedContent
import com.example.syntax_camping.UserScreens.campsiteInformationDestinations
import com.example.syntax_camping.ui.theme.SyntaxcampingTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ReservationConfirmationTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun acknowledgementControlsConfirmationAndShowsSavedReservation() {
        var confirmationCalls = 0
        showBooking { confirmationCalls++ }

        val checkbox = compose.onNodeWithTag("agreeRulesCheckbox")
        val confirm = compose.onNodeWithTag("confirmReservationButton")
        checkbox.assertIsOff()
        confirm.assertIsNotEnabled()

        checkbox.performClick().assertIsOn()
        confirm.assertIsEnabled()
        checkbox.performClick().assertIsOff()
        confirm.assertIsNotEnabled()
        compose.runOnIdle { assertEquals(0, confirmationCalls) }

        checkbox.performClick()
        confirm.performClick()
        compose.onNodeWithText("Reservation Confirmed").assertIsDisplayed()
        compose.onNodeWithText("River Camp").assertIsDisplayed()
        compose.onNodeWithText("Check-in: 05/12/26").assertIsDisplayed()
        compose.onNodeWithText("Check-out: 06/12/26").assertIsDisplayed()
        compose.onNodeWithText("Status: Confirmed").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, confirmationCalls) }

        compose.onNodeWithText("My Reservations").performClick()
        compose.onNodeWithText("Your Reservations").assertIsDisplayed()
    }

    @Test
    fun rulesAndCancellationPolicyCanBeReadBeforeAcknowledging() {
        showBooking {}

        compose.onNodeWithText("🏕️ Campsite Rules").performClick()
        compose.onNodeWithText("Campsite Rules").assertIsDisplayed()
        compose.onNodeWithContentDescription("back button").performClick()

        compose.onNodeWithText("📜 Cancellation policy").performClick()
        compose.onNodeWithText("Cancellation Policy").assertIsDisplayed()
        compose.onNodeWithContentDescription("back button").performClick()

        compose.onNodeWithTag("agreeRulesCheckbox").assertIsOff()
        compose.onNodeWithTag("confirmReservationButton").assertIsNotEnabled()
    }

    private fun showBooking(onConfirm: () -> Unit) {
        compose.setContent {
            SyntaxcampingTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "booking") {
                    composable("booking") {
                        var acknowledged by rememberSaveable { mutableStateOf(false) }
                        var confirmed by rememberSaveable { mutableStateOf(false) }
                        if (confirmed) {
                            ReservationConfirmedContent("River Camp", "05/12/26", "06/12/26") {
                                navController.navigate("reservations")
                            }
                        } else {
                            ReservationConfirmationControls(
                                navController = navController,
                                acknowledged = acknowledged,
                                onAcknowledgementChange = { acknowledged = it },
                                bookingNameEntered = true,
                                submitting = false,
                                onConfirm = {
                                    onConfirm()
                                    confirmed = true
                                }
                            )
                        }
                    }
                    campsiteInformationDestinations(navController)
                    composable("reservations") { Text("Your Reservations") }
                }
            }
        }
    }
}
