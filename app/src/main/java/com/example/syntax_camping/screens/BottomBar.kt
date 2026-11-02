package com.example.syntax_camping.screens

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.syntax_camping.R

// 1. The Blueprint for your specific app tabs
enum class BottomTab(
    val route: String,
    @DrawableRes val iconUnselected: Int,
    @DrawableRes val iconSelected: Int
) {
    Search("Search", R.drawable.ic_search, R.drawable.search_tapped),
    Reservations("Reservations", R.drawable.reservations, R.drawable.reservations), // Add a tapped version here if you make one
    Profile("Profile", R.drawable.account, R.drawable.account_tapped),                     // Add a tapped version here if you make one
    Settings("Settings", R.drawable.ic_settings, R.drawable.settings_tapped)
}


@Composable
fun CustomBottomBar(
    navController: NavController,
    currentRoute: String,
    backgroundColor: Color = Color(0xFF3E6B68)
) {
    NavigationBar(
        containerColor = backgroundColor
    ) {
        BottomTab.entries.forEach { tab ->
            val isSelected = currentRoute == tab.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(tab.route)
                    }
                },
                icon = {
                    // Swaps the icon if it is the currently selected page
                    val targetIcon = if (isSelected) tab.iconSelected else tab.iconUnselected
                    Icon(
                        painter = painterResource(id = targetIcon),
                        contentDescription = tab.route,
                        tint = Color.Unspecified
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    // Optional: changes the color of the bubble behind the active icon
                    indicatorColor = Color.White.copy(alpha = 0.2f)
                )
            )
        }
    }
}