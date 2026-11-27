package com.example.syntax_camping

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.dataClasses.NotificationLogEntry
import com.example.syntax_camping.dataClasses.NotificationLogPage
import com.example.syntax_camping.dataClasses.NotificationLogScope
import com.example.syntax_camping.screens.adminScreens.AdminNotificationLogsScreen
import com.example.syntax_camping.screens.adminScreens.adminCampsiteList.CampsiteListScreenAdmin
import com.example.syntax_camping.screens.adminScreens.adminUserList.UserStatus
import com.example.syntax_camping.screens.adminScreens.adminUserView.AdminUserDetails
import com.example.syntax_camping.screens.adminScreens.adminUserView.AdminViewUserScreen
import com.example.syntax_camping.screens.adminScreens.openAdminNotificationLogs
import com.example.syntax_camping.ui.theme.SyntaxcampingTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AdminNotificationLogsTest {
    @get:Rule val compose = createComposeRule()

    private val river = NotificationLogEntry(
        "river-entry", "river", "River Camp", "Joined lottery", "Your entry was received.", 1_795_600_000_000L
    )
    private val pines = NotificationLogEntry(
        "pines-entry", "pines", "Pines Camp", "Joined waitlist", "You were added to the waitlist.", 1_795_500_000_000L
    )

    @Test
    fun campsiteMenuOpensOnlyTheSelectedCampsiteLogs() {
        val campsite = Campsite("river", "River Camp", "Edmonton", "", 4, mutableListOf(), 0.0)
        setContent(
            "adminBrowseCampsites", NotificationLogScope.CAMPSITE, "river",
            NotificationLogPage("River Camp", listOf(river)), campsite
        )

        compose.onNodeWithTag("adminCampsiteMenu_river").performClick()
        compose.onNodeWithTag("adminViewNotificationsItem").performClick()
        compose.onNodeWithText("Notification logs for River Camp").assertIsDisplayed()
        compose.onNodeWithText(river.message).assertIsDisplayed()
        compose.onNodeWithText(pines.message).assertDoesNotExist()

        compose.onNodeWithContentDescription("back button").performClick()
        compose.onNodeWithTag("adminCampsiteMenu_river").assertIsDisplayed()
    }

    @Test
    fun ownerProfileOpensLogsGroupedByCampsite() {
        setContent("adminUserInfo", NotificationLogScope.OWNER, "owner-1", NotificationLogPage("Ada", listOf(river, pines)))

        compose.onNodeWithText("View notification logs").performClick()
        compose.onNodeWithText("Notification logs for owner Ada").assertIsDisplayed()
        compose.onNodeWithText("River Camp").assertIsDisplayed()
        compose.onNodeWithText(river.message).assertIsDisplayed()
        compose.onNodeWithText("Pines Camp").assertIsDisplayed()
        compose.onNodeWithText(pines.message).assertIsDisplayed()

        compose.onNodeWithContentDescription("back button").performClick()
        compose.onNodeWithText("View notification logs").assertIsDisplayed()
    }

    private fun setContent(
        start: String,
        expectedScope: NotificationLogScope,
        expectedId: String,
        page: NotificationLogPage,
        campsite: Campsite? = null
    ) {
        compose.setContent {
            SyntaxcampingTheme {
                val nav = rememberNavController()
                NavHost(navController = nav, startDestination = start) {
                    composable("adminBrowseCampsites") {
                        CampsiteListScreenAdmin(
                            campsiteList = listOfNotNull(campsite),
                            onItemClick = {},
                            onViewNotificationsClick = { nav.openAdminNotificationLogs(NotificationLogScope.CAMPSITE, it) }
                        )
                    }
                    composable("adminUserInfo") {
                        AdminViewUserScreen(nav, "owner-1", AdminUserDetails("Ada", "", "", UserStatus.OWNER))
                    }
                    composable("adminNotificationLogs/{scope}/{id}") { entry ->
                        val scope = NotificationLogScope.entries.first { it.route == entry.arguments?.getString("scope") }
                        val id = requireNotNull(entry.arguments?.getString("id"))
                        AdminNotificationLogsScreen(nav, scope, id) { requestedScope, requestedId, cursor ->
                            assertEquals(expectedScope, requestedScope)
                            assertEquals(expectedId, requestedId)
                            assertEquals(null, cursor)
                            Result.success(page)
                        }
                    }
                }
            }
        }
    }
}
