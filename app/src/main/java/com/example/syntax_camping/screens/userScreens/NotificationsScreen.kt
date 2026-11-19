package com.example.syntax_camping.screens.userScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.AppNotification
import com.example.syntax_camping.NotificationInbox
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.NOTIFICATIONS_ROUTE
import com.example.syntax_camping.screens.tabs.TopBar
import com.example.syntax_camping.ui.theme.SyntaxcampingTheme

internal fun NavGraphBuilder.notificationsDestination(navController: NavController, inbox: NotificationInbox) {
    composable(NOTIFICATIONS_ROUTE) {
        LaunchedEffect(inbox.hasUnread) {
            if (inbox.hasUnread) inbox.markRead()
        }
        NotificationsScreen(navController, inbox.notifications)
    }
}

@Composable
private fun NotificationsScreen(navController: NavController, notifications: List<AppNotification>) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF4E8D5),
        contentColor = Color.Black,
        topBar = { TopBar(navController) },
        bottomBar = { CustomBottomBar(navController, currentRoute = NOTIFICATIONS_ROUTE) }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.notifications_empty), style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(25.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF2C9C9),
                            contentColor = Color.Black
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                notification.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(notification.message, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 890)
@Composable
private fun NotificationsPreview() {
    SyntaxcampingTheme {
        NotificationsScreen(
            rememberNavController(),
            listOf(
                AppNotification(
                    id = "preview",
                    title = stringResource(R.string.notification_lottery_joined_for, "Dolor Campsite"),
                    message = stringResource(R.string.notification_lottery_joined_body)
                )
            )
        )
    }
}
