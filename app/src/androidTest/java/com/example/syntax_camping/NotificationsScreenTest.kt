package com.example.syntax_camping
/*
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.example.syntax_camping.dataClasses.NotificationData
import com.example.syntax_camping.screens.LocalHasUnreadNotifications
import com.example.syntax_camping.screens.NotificationBannerHost
import com.example.syntax_camping.screens.TopBar
import com.example.syntax_camping.screens.userScreens.notificationsDestination
import com.example.syntax_camping.ui.theme.SyntaxcampingTheme
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NotificationsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun bellOpensSavedNotificationsAndBackReturnsToCampsite() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val userId = UUID.randomUUID().toString()
        context.getSharedPreferences("notifications_$userId", Context.MODE_PRIVATE).edit()
            .putString("updates", """[{"id":"dolor:10:0","subject":"Dolor Campsite","registration":"LOTTERY"}]""")
            .apply()
        val inbox = NotificationInbox(context, userId)
        inbox.showNotification(
            NotificationData.LotteryJoined("dolor:10:0", "Dolor Campsite", joinedWaitlist = false)
        )
        inbox.showNotification(NotificationData.LotteryJoined("pines:20:0", "Pines", joinedWaitlist = true))
        val restored = NotificationInbox(context, userId)
        assertEquals(
            listOf("Joined waitlist for Pines", "Joined lottery for Dolor Campsite"),
            restored.notifications.map { it.title }
        )
        assertTrue(restored.hasUnread)

        openTestApp(restored)
        compose.onNodeWithContentDescription(context.getString(R.string.notifications_bell)).performClick()
        compose.onNodeWithText("Joined lottery for Dolor Campsite").assertIsDisplayed()
        compose.onNodeWithText("Joined waitlist for Pines").assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.notifications_bell)).assertIsNotEnabled()
        compose.runOnIdle { assertFalse(NotificationInbox(context, userId).hasUnread) }

        compose.onNodeWithContentDescription("back button").performClick()
        compose.onNodeWithText("Joined lottery for Dolor Campsite").assertDoesNotExist()
        context.deleteSharedPreferences("notifications_$userId")
    }

    @Test
    fun notificationAppearsInInboxAfterClosingBannerAndClearsUnreadBadge() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val userId = UUID.randomUUID().toString()
        val inbox = NotificationInbox(context, userId)
        val databaseResult = NotificationData.Message(
            id = "booking-confirmed-17",
            title = "NoReply Syntax Camping",
            message = "Your booking at Dolor Campsite is confirmed."
        )
        val bell = compose.onNodeWithContentDescription(context.getString(R.string.notifications_bell))
        val back = compose.onNodeWithContentDescription("back button")
        val emptyMessage = compose.onNodeWithText(context.getString(R.string.notifications_empty))
        val unreadBadge = SemanticsMatcher.expectValue(
            SemanticsProperties.StateDescription,
            context.getString(R.string.notifications_unread)
        )

        openTestApp(inbox) {
            Button(onClick = { inbox.showNotification(databaseResult) }) {
                Text("Confirm booking")
            }
        }
        bell.performClick()
        emptyMessage.assertIsDisplayed()
        back.performClick()

        compose.onNodeWithText("Confirm booking").performClick()
        compose.onNodeWithText(databaseResult.title).assertIsDisplayed()
        compose.onNodeWithText(databaseResult.message).assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.notification_dismiss)).performClick()
        compose.onNodeWithText("Confirm booking").performClick()
        compose.onNodeWithText(databaseResult.title).assertDoesNotExist()

        bell.assert(unreadBadge).performClick()
        emptyMessage.assertDoesNotExist()
        compose.onAllNodesWithText(databaseResult.title).assertCountEquals(1)
        compose.onNodeWithText(databaseResult.message).assertIsDisplayed()
        back.performClick()
        compose.onNodeWithText("Confirm booking").assertIsDisplayed()
        bell.assert(!unreadBadge)

        compose.runOnIdle {
            val restored = NotificationInbox(context, userId)
            assertEquals(databaseResult.message, restored.notifications.single().message)
            assertFalse(restored.hasUnread)
        }
        context.deleteSharedPreferences("notifications_$userId")
    }

    private fun openTestApp(inbox: NotificationInbox, campsiteContent: @Composable () -> Unit = {}) {
        compose.setContent {
            SyntaxcampingTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(LocalHasUnreadNotifications provides inbox.hasUnread) {
                    NotificationBannerHost(inbox, Modifier.fillMaxSize()) {
                        NavHost(navController, startDestination = "campsiteDetails") {
                            composable("campsiteDetails") {
                                Scaffold(topBar = { TopBar(navController) }) { padding ->
                                    Box(
                                        modifier = Modifier.fillMaxSize().padding(padding),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        campsiteContent()
                                    }
                                }
                            }
                            notificationsDestination(navController, inbox)
                        }
                    }
                }
            }
        }
    }
}
*/