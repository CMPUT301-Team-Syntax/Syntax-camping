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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.CampsiteRowTab

// US 02.03.01: displays the owner's campsites.
// TODO: Connect the list and callbacks during team integration.
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "My Campsites",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (campsites.isEmpty()) {
                item {
                    Text("No campsites yet. Tap + to add one.")
                }
            }

            items(
                items = campsites,
                key = { it.documentId }
            ) { campsite ->
                CampsiteRowTab(campsite) {
                    onCampsiteClick(campsite)
                }
            }
        }
    }
}

// Shared only by Zelin's two pages.
@Composable
internal fun OwnerPagesLayout(
    navController: NavController,
    showAddButton: Boolean = false,
    onAddCampsite: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val pink = Color(0xFFE2AFAF)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        contentColor = Color.Black,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(pink),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OwnerTemplateIcon(
                    icon = R.drawable.back,
                    label = "Back",
                    size = 100
                ) {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showAddButton) {
                        OwnerTemplateIcon(
                            icon = R.drawable.add,
                            label = "Add campsite",
                            size = 40,
                            onClick = onAddCampsite
                        )
                    }

                    OwnerTemplateIcon(
                        icon = R.drawable.notification,
                        label = "Notifications",
                        size = 100,
                        onClick = onNotificationsClick
                    )
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(pink)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OwnerTemplateIcon(
                    icon = R.drawable.home,
                    label = "My Campsites",
                    size = 80
                ) {
                    navController.navigate("campsiteOwner") {
                        launchSingleTop = true
                    }
                }

                OwnerTemplateIcon(
                    icon = R.drawable.lottery,
                    label = "Lotteries",
                    size = 80
                ) {
                    navController.navigate("campsiteLottery") {
                        launchSingleTop = true
                    }
                }

                OwnerTemplateIcon(
                    icon = R.drawable.messenger,
                    label = "Messages",
                    size = 80
                ) {
                    navController.navigate("campsiteMessages") {
                        launchSingleTop = true
                    }
                }

                OwnerTemplateIcon(
                    icon = R.drawable.account,
                    label = "Profile",
                    size = 80
                ) {
                    navController.navigate("campsiteProfile") {
                        launchSingleTop = true
                    }
                }

                OwnerTemplateIcon(
                    icon = R.drawable.ic_settings,
                    label = "Settings",
                    size = 80
                ) {
                    navController.navigate("campsiteSettings") {
                        launchSingleTop = true
                    }
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
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(size.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = Color.Unspecified
        )
    }
}

// Sample data is only used by Android Studio Preview.
@Preview(
    name = "My Campsites - sample data",
    showBackground = true,
    widthDp = 411,
    heightDp = 800
)
@Composable
private fun MyCampsitesPreview() {
    val campsites = remember {
        listOf(
            Campsite(
                "sample-1",
                "Dolor Campsite",
                "Edmonton",
                "A quiet campsite near walking trails.",
                9,
                mutableListOf("Walking trails")
            ),
            Campsite(
                "sample-2",
                "Lorem Campsite",
                "Calgary",
                "A small campsite with picnic tables.",
                4,
                mutableListOf("Picnic tables")
            ),
            Campsite(
                "sample-3",
                "Ipsum Campsite",
                "Banff",
                "A campsite with mountain views.",
                12,
                mutableListOf("Mountain views")
            )
        )
    }

    MyCampsitesScreen(
        navController = rememberNavController(),
        campsites = campsites,
        onCampsiteClick = {},
        onAddCampsite = {}
    )
}

@Preview(
    name = "My Campsites - empty",
    showBackground = true,
    widthDp = 411,
    heightDp = 800
)
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