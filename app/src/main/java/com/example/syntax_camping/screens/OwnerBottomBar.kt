
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

// The blueprint for user bottom bar
enum class OwnerBottomTab(//can replicate for campsite owner, just change the navigations as per campsite owner navhost names and routes
    val route: String,
    @DrawableRes val iconSelected: Int
) {
    Home("campsiteOwner", R.drawable.home),
    Lottery("campsiteLottery", R.drawable.lottery), // Add a tapped version here if you make one
    Messages("campsiteMessages", R.drawable.messenger),                     // Add a tapped version here if you make one
    Profile("campsiteProfile", R.drawable.account),
    Settings("campsiteSettings", R.drawable.ic_settings),
}


@Composable
fun CustomOwnerBottomBar(
    navController: NavController,
    currentRoute: String,
    backgroundColor: Color = Color(0xFF3E6B68)
) {
    NavigationBar(
        containerColor = backgroundColor
    ) {
        OwnerBottomTab.entries.forEach { tab -> //here it uses the bottom bar enum made atop
            val isSelected = currentRoute == tab.route

            NavigationBarItem(
                //this is becasue i made two states in the figma mockup, may be unnecessary if campsite owner icons don't change colour when tapped to go to the screens
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(tab.route)
                    }
                },
                icon = {
                    // swaps the icon if it is the currently selected page
                    val targetIcon = tab.iconSelected
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