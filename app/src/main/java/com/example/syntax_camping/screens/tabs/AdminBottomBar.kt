package com.example.syntax_camping.screens.tabs

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController

// Blueprint for the admin bottom bar. Each tab uses either a drawable or a vector icon.
enum class AdminTab(
    val route: String,
    @DrawableRes val iconUnselected: Int? = null,
    @DrawableRes val iconSelected: Int? = null,
    val vector: ImageVector? = null
) {
    Home("adminBrowseCampsites", com.example.syntax_camping.R.drawable.home, com.example.syntax_camping.R.drawable.home),
    Profiles("adminBrowseProfiles", com.example.syntax_camping.R.drawable.account, com.example.syntax_camping.R.drawable.account_tapped),
    Images("adminBrowseImages", vector = Icons.Outlined.Image),
    Complaints("adminComplaints", vector = Icons.Outlined.Report),
    Settings("adminSettings", com.example.syntax_camping.R.drawable.ic_settings, com.example.syntax_camping.R.drawable.settings_tapped)
}

@Composable
fun AdminBottomBar(
    navController: NavController,
    currentRoute: String,
    backgroundColor: Color = Color(0xFF3E6B68)
) {
    NavigationBar(containerColor = backgroundColor) {
        AdminTab.entries.forEach { tab ->
            val isSelected = currentRoute == tab.route

            NavigationBarItem(
                modifier = Modifier.testTag("adminTab_${tab.name.lowercase()}"),
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(tab.route) { launchSingleTop = true }
                    }
                },
                icon = {
                    if (tab.vector != null) {
                        Icon(
                            imageVector = tab.vector,
                            contentDescription = tab.route,
                            tint = Color.White
                        )
                    } else {
                        val res = if (isSelected) tab.iconSelected else tab.iconUnselected
                        Icon(
                            painter = painterResource(id = res!!),
                            contentDescription = tab.route,
                            tint = Color.Unspecified
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.White.copy(alpha = 0.2f)
                )
            )
        }
    }
}