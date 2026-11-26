package com.example.syntax_camping.screens.adminScreens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.syntax_camping.R
import com.example.syntax_camping.dataClasses.AdminNotificationLogsRepository
import com.example.syntax_camping.dataClasses.NotificationLogCursor
import com.example.syntax_camping.dataClasses.NotificationLogEntry
import com.example.syntax_camping.dataClasses.NotificationLogPage
import com.example.syntax_camping.dataClasses.NotificationLogScope
import com.example.syntax_camping.screens.tabs.AdminBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import java.text.DateFormat
import java.util.Date

internal fun NavController.openAdminNotificationLogs(scope: NotificationLogScope, id: String) {
    if (id.isBlank()) return
    navigate("adminNotificationLogs/${scope.route}/${Uri.encode(id)}") { launchSingleTop = true }
}

internal fun NavGraphBuilder.adminNotificationLogsDestination(navController: NavController) {
    composable("adminNotificationLogs/{scope}/{id}") { entry ->
        val id = entry.arguments?.getString("id").orEmpty()
        val scope = NotificationLogScope.entries.find { it.route == entry.arguments?.getString("scope") }
        if (scope != null && id.isNotBlank()) {
            AdminAccess(navController) {
                val repository = remember { AdminNotificationLogsRepository() }
                AdminNotificationLogsScreen(navController, scope, id, repository::load)
            }
        }
    }
}

@Composable
internal fun AdminNotificationLogsScreen(
    navController: NavController,
    scope: NotificationLogScope,
    id: String,
    load: suspend (NotificationLogScope, String, NotificationLogCursor?) -> Result<NotificationLogPage>
) {
    var page by remember(scope, id) { mutableStateOf<NotificationLogPage?>(null) }
    var loading by remember(scope, id) { mutableStateOf(true) }
    var failed by remember(scope, id) { mutableStateOf(false) }
    var request by remember(scope, id) { mutableIntStateOf(0) }
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) }

    LaunchedEffect(scope, id, request) {
        loading = true
        failed = false
        load(scope, id, page?.next).onSuccess { incoming ->
            val combined = (page?.rows.orEmpty() + incoming.rows).distinctBy { it.id }
            page = incoming.copy(rows = combined)
        }.onFailure {
            failed = true
        }
        loading = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF5F5F5),
        topBar = { TopBar(navController) },
        bottomBar = {
            AdminBottomBar(navController, currentRoute =
                if (scope == NotificationLogScope.CAMPSITE) "adminBrowseCampsites" else "adminBrowseProfiles")
        }
    ) { padding ->
        val result = page
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = stringResource(
                        when {
                            result == null -> R.string.admin_notification_logs
                            scope == NotificationLogScope.CAMPSITE -> R.string.admin_campsite_notification_logs
                            else -> R.string.admin_owner_notification_logs
                        },
                        result?.name.orEmpty()
                    ),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            if (scope == NotificationLogScope.OWNER) {
                result?.rows.orEmpty().groupBy { it.campsiteId }.forEach { (campsiteId, entries) ->
                    item(key = "campsite_$campsiteId") {
                        Text(
                            text = entries.first().campsiteName,
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    items(entries, key = { it.id }) { NotificationLogCard(it, dateFormat) }
                }
            } else {
                items(result?.rows.orEmpty(), key = { it.id }) { NotificationLogCard(it, dateFormat) }
            }

            when {
                loading -> item { CircularProgressIndicator() }
                failed -> item {
                    Text(stringResource(R.string.admin_notification_logs_error))
                    TextButton(onClick = { request++ }) { Text(stringResource(R.string.admin_notification_logs_retry)) }
                }
                result?.rows?.isEmpty() == true -> item { Text(stringResource(R.string.admin_notification_logs_empty)) }
                result?.next != null -> item {
                    TextButton(onClick = { request++ }) { Text(stringResource(R.string.admin_notification_logs_more)) }
                }
            }
        }
    }
}

@Composable
private fun NotificationLogCard(entry: NotificationLogEntry, dateFormat: DateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(entry.title, style = MaterialTheme.typography.titleSmall)
            Text(entry.message, style = MaterialTheme.typography.bodyMedium)
            Text(dateFormat.format(Date(entry.occurredAt)), style = MaterialTheme.typography.bodySmall)
        }
    }
}
