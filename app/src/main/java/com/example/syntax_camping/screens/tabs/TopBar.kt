package com.example.syntax_camping.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.syntax_camping.R
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner

internal const val NOTIFICATIONS_ROUTE = "Notifications"
internal val LocalHasUnreadNotifications = compositionLocalOf { false }

@Composable
fun TopBar(navController: NavController){
    val currentEntry by navController.currentBackStackEntryAsState()
    val onNotificationsPage = currentEntry?.destination?.route == NOTIFICATIONS_ROUTE
    val showUnreadBadge = LocalHasUnreadNotifications.current && !onNotificationsPage
    val unreadDescription = stringResource(R.string.notifications_unread)
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFF3E6B68)), // sets the background color to whatever colour really lkie the figma mockups
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        //icon button for back button, template for adding icons
        IconButton(

            onClick = { navController.popBackStack() },
            modifier = Modifier.size(100.dp) // the size is defaulted to 44.dp which is pretty small so you can change this if needed for other icons

        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "back button",
                tint = Color.Unspecified  //without it the icon would be black
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            IconButton(
                onClick = {
                    val scanner = GmsBarcodeScanning.getClient(context)

                    scanner.startScan()
                        .addOnSuccessListener { barcode ->

                            val content = barcode.rawValue.orEmpty()
                            val prefix = "syntaxcamping:campsite:"

                            if (content.startsWith(prefix)) {
                                val campsiteId = content.removePrefix(prefix)

                                if (campsiteId.isNotBlank() &&
                                    !campsiteId.contains("/")
                                ) {
                                    navController.navigate("campsiteDetails/$campsiteId")
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("QRScanner", "Failed to scan QR code", exception)
                        }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = "Scan QR Code",
                    tint = Color.White
                )
            }
            IconButton(onClick = {navController.navigate( "campsiteMessages")},
                modifier = Modifier.size(40 .dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.messenger),
                    contentDescription = "messages",
                    tint = Color.Unspecified
                )
            }
            IconButton(
                onClick = { navController.navigate(NOTIFICATIONS_ROUTE) { launchSingleTop = true } },
                enabled = !onNotificationsPage,
                modifier = Modifier.size(50.dp).semantics {
                    if (showUnreadBadge) stateDescription = unreadDescription
                }
            ) {
                BadgedBox(badge = { if (showUnreadBadge) Badge() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.notification),
                        contentDescription = stringResource(R.string.notifications_bell),
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}
