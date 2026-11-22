package com.example.syntax_camping.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.syntax_camping.Database
import org.checkerframework.checker.units.qual.C

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.dataClasses.Conversation

@Composable
fun ConversationScreen(conversation: Conversation) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Participants", style = MaterialTheme.typography.titleLarge)
        Text(conversation.participants.joinToString().ifBlank { "No participants" })

        Text(
            "Messages (${conversation.messages.size})",
            style = MaterialTheme.typography.titleLarge
        )

        if (conversation.messages.isEmpty()) {
            Text("No messages yet")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(conversation.messages) { message ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = message.sender.ifBlank { "(empty sender)" },
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(message.contents.ifBlank { "(empty message)" })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationRoute(conversationId: String) {
    val db = remember { Database() }
    var conversation by remember(conversationId) { mutableStateOf<Conversation?>(null) }
    var error by remember(conversationId) { mutableStateOf<Exception?>(null) }

    LaunchedEffect(conversationId) {
        db.getConversation(
            conversationId,
            onSuccess = { loaded -> conversation = loaded },
            onError = { failure -> error = failure }
        )
    }

    val loaded = conversation
    when {
        loaded != null -> ConversationScreen(conversation = loaded)
        error != null -> Text("Could not load conversation")
        else -> Text("Loading conversation…")
    }
}

@Preview
@Composable
private fun LiveConversationPreview() {
    ConversationRoute(conversationId = "conservationExample")
}