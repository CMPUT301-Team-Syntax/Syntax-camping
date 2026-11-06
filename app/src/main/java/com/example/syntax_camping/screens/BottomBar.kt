package com.example.syntax_camping.screens

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.syntax_camping.R

// The blueprint for user bottom bar
enum class BottomTab(//can replicate for campsite owner, just change the navigations as per campsite owner navhost names and routes
    val route: String,
    @DrawableRes val iconUnselected: Int,
    @DrawableRes val iconSelected: Int
) {
    Search("Search", R.drawable.ic_search, R.drawable.search_tapped),
    Reservations("Reservations", R.drawable.reservations, R.drawable.reservations),
    CampsitesHome("CampsiteOwnerHome", R.drawable.home, R.drawable.home ),
    Profile("Profile", R.drawable.account, R.drawable.account_tapped),                     // Add a tapped version here if you make one
    Settings("Settings", R.drawable.ic_settings, R.drawable.settings_tapped),
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

        BottomTab.entries.forEach { tab -> //here it uses the bottom bar enum made atop
            val isSelected = currentRoute == tab.route

            NavigationBarItem(
                modifier = Modifier.testTag("tab_$tab.name"),
                //this is becasue i made two states in the figma mockup, may be unnecessary if campsite owner icons don't change colour when tapped to go to the screens
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}"),
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(tab.route)
                    }
                },
                icon = {
                    // swaps the icon if it is the currently selected page
                    val targetIcon = if (isSelected) tab.iconSelected else tab.iconUnselected
                    Icon(
                        painter = painterResource(id = targetIcon),
                        contentDescription = tab.route,
                        tint = Color.Unspecified
                    )
                },

                colors = NavigationBarItemDefaults.colors(
                    // Optional: changes the color of the bubble behind the active icon
                    indicatorColor = Color.White.copy(alpha = 0.2f)//for some reason the tap on click kept having a white colour
                )
            )
        }
    }
}