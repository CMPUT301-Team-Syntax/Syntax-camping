package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.CampsiteCard

// US 02.03.01: displays the owner's campsites.
// TODO: Connect the list and callbacks during team integration.
// The header and footer follow the team's owner template.
@Composable
fun MyCampsitesScreen(
    navController: NavController,
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit,
    onAddCampsite: () -> Unit,
    onNotificationsClick: () -> Unit = {}
) {
    OwnerPagesLayout(
        navController = navController,
        showAddButton = true,
        onAddCampsite = onAddCampsite,
        onNotificationsClick = onNotificationsClick
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (campsites.isEmpty()) {
                item { Text("No campsites yet. Tap + to add one.") }
            }
            items(campsites, key = { it.documentId }) { campsite ->
                CampsiteCard(campsite) { onCampsiteClick(campsite) }
            }
        }
    }
}

// Used only by Zelin's list and details screens, not by other team pages.
@Composable
internal fun OwnerPagesLayout(
    navController: NavController,
    showAddButton: Boolean = false,
    onAddCampsite: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onEditCampsite: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val pink = Color(0xFFE2AFAF)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        contentColor = Color.Black,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().height(80.dp).background(pink),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Syntax\nCamping",
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 20.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onEditCampsite != null) {
                        TextButton(
                            onClick = onEditCampsite,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = Color(0xFFF3C7C7),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Update\nDetails", textAlign = TextAlign.Center, fontSize = 12.sp)
                        }
                    }
                    if (showAddButton) {
                        OwnerTemplateIcon(R.drawable.add, "Add campsite", 40, onAddCampsite)
                    }
                    OwnerTemplateIcon(
                        R.drawable.notification, "Notifications", 100, onNotificationsClick
                    )
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().height(100.dp)
                    .background(pink).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OwnerTemplateIcon(R.drawable.home, "My Campsites", 80) {
                    navController.navigate("campsiteOwner") { launchSingleTop = true }
                }
                OwnerTemplateIcon(R.drawable.lottery, "Lotteries", 80) {
                    navController.navigate("campsiteLottery") { launchSingleTop = true }
                }
                OwnerTemplateIcon(R.drawable.messenger, "Messages", 80) {
                    navController.navigate("campsiteMessages") { launchSingleTop = true }
                }
                OwnerTemplateIcon(R.drawable.account, "Profile", 80) {
                    navController.navigate("campsiteProfile") { launchSingleTop = true }
                }
                OwnerTemplateIcon(R.drawable.ic_settings, "Settings", 80) {
                    navController.navigate("campsiteSettings") { launchSingleTop = true }
                }
            }
        },
        content = content
    )
}

@Composable
private fun OwnerTemplateIcon(
    icon: Int,
    label: String,
    size: Int,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(size.dp)) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = Color.Unspecified
        )
    }
}

// Sample data is only used by Android Studio Preview.
@Preview(name = "My Campsites - sample data", showBackground = true, widthDp = 411, heightDp = 800)
@Composable
private fun MyCampsitesPreview() {
    val campsites = remember {
        listOf(
            Campsite("sample-1", "Dolor Campsite", "Edmonton",
                "A quiet campsite near walking trails.", 9, mutableListOf("Walking trails"), rating = 4.4),
            Campsite("sample-2", "Lorem Campsite", "Calgary",
                "A small campsite with picnic tables.", 4, mutableListOf("Picnic tables"), rating = 4.2),
            Campsite("sample-3", "Ipsum Campsite", "Banff",
                "A campsite with mountain views.", 12, mutableListOf("Mountain views"), rating = 4.6)
        )
    }
    MyCampsitesScreen(
        navController = rememberNavController(),
        campsites = campsites,
        onCampsiteClick = {},
        onAddCampsite = {}
    )
}

@Preview(name = "My Campsites - empty", showBackground = true, widthDp = 411, heightDp = 800)
@Composable
private fun EmptyMyCampsitesPreview() {
    MyCampsitesScreen(
        navController = rememberNavController(),
        campsites = emptyList(),
        onCampsiteClick = {},
        onAddCampsite = {}
    )
}

// End of MyCampsitesScreen.kt
