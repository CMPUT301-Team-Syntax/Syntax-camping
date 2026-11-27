package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.lazy.items
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.syntax_camping.ConversationRepository
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.Conversation
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar


import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.example.syntax_camping.screens.ConversationDisplay
import com.google.firebase.firestore.FirebaseFirestore

@Composable

fun CampsiteMessagesScreen (navController: NavController, userViewModel: UserViewModel){
    val context = LocalContext.current
    val deviceId = remember(context) {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            .orEmpty()
    }

    var conversations by remember(deviceId) {
        mutableStateOf<List<Conversation>>(emptyList())
    }
    var selectedConversation by remember(deviceId) {
        mutableStateOf<Conversation?>(null)
    }
    var loading by remember(deviceId) { mutableStateOf(true) }
    var loadError by remember(deviceId) { mutableStateOf<String?>(null) }
    var refreshAttempt by remember(deviceId) { mutableStateOf(0) }

    LaunchedEffect(deviceId, refreshAttempt) {
        loading = true
        loadError = null

        if (deviceId.isBlank()) {
            loading = false
            loadError = "Couldn't identify this device."
            return@LaunchedEffect
        }

        ConversationRepository.refreshConversationCache(
            deviceId = deviceId,
            onSuccess = { loaded ->
                conversations = loaded.toList()
                loading = false
            },
            onError = { error ->
                Log.e("CampsiteMessages", "Couldn't load conversations", error)
                loadError = "Couldn't load conversations."
                loading = false
            }
        )
    }

    BackHandler(enabled = selectedConversation != null) {
        selectedConversation = null
        refreshAttempt++
    }

    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Set the background color to white
        containerColor = Color.White,
        topBar = { // top bar with back button and notification icon
            TopBar(
                navController
            )
        },

        bottomBar = {
            CustomBottomBar(navController,
                "campsiteMessages")
        }
    ){innerPadding ->

        val openConversation = selectedConversation

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when {
                openConversation != null -> {
                    TextButton(onClick = {
                        selectedConversation = null
                        refreshAttempt++
                    }) {
                        Text("Back to messages")
                    }

                    ConversationDisplay(
                        convoId = openConversation.conversationId,
                        convo = openConversation,
                        currentDeviceId = deviceId
                    )
                }

                loading -> CircularProgressIndicator()

                loadError != null -> {
                    Text(loadError.orEmpty())
                    Button(onClick = { refreshAttempt++ }) {
                        Text("Retry")
                    }
                }

                conversations.isEmpty() -> Text("No conversations yet.")

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = conversations, key = { it.conversationId }) { conversation ->
                        ConversationInboxRow(
                            conversation = conversation,
                            currentDeviceId = deviceId,
                            onClick = { selectedConversation = conversation }
                        )
                    }
                }
            }
        }
    }

}

private val InboxGreen = Color(0xFF487572)

@Composable
private fun ConversationInboxRow(
    conversation: Conversation,
    currentDeviceId: String,
    onClick: () -> Unit
) {
    val otherDeviceId = conversation.participants.firstOrNull { it != currentDeviceId }
    var displayName by remember(otherDeviceId) { mutableStateOf<String?>(null) }

    LaunchedEffect(otherDeviceId) {
        if (otherDeviceId == null) return@LaunchedEffect

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(otherDeviceId)
            .get()
            .addOnSuccessListener { profile ->
                displayName = profile.getString("displayName")
            }
            .addOnFailureListener { error ->
                Log.w("CampsiteMessages", "Couldn't load participant profile", error)
            }
    }

    val title = displayName?.takeIf { it.isNotBlank() }
        ?: otherDeviceId
        ?: "Conversation"

    val latestMessage = conversation.messages.lastOrNull()
    val preview = when {
        latestMessage == null -> "No messages yet"
        latestMessage.sender == currentDeviceId -> "You: ${latestMessage.contents}"
        else -> latestMessage.contents
    }

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(InboxGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title.trim().take(1).uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}