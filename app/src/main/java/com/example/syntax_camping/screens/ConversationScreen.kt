package com.example.syntax_camping.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.ConversationRepository
import com.example.syntax_camping.dataClasses.Conversation
import com.example.syntax_camping.dataClasses.Message

import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.fromColorLong
import com.example.syntax_camping.globalDeviceId
import java.util.UUID

// ConversationScreen.kt
// Author: Andrew
// Screen for displaying conversations with other users

@Composable
fun MessageDisplay(msg: Message, isMine: Boolean) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier.widthIn(max = maxWidth * 0.8f),
            shape = RoundedCornerShape(16.dp),
            color = if (isMine) Color(0xFF487572) else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isMine) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Text(
                text = msg.contents,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun ConversationDisplay(
    convoId: String,
    convo: Conversation,
    currentDeviceId: String
) {

    // some LLM usage here, mostly to make it look nice, I don't have tons of experience with making UI look good

    var draft by remember(convoId, currentDeviceId) { mutableStateOf("") }
    var sending by remember(convoId, currentDeviceId) { mutableStateOf(false) }
    var sendError by remember(convoId, currentDeviceId) {
        mutableStateOf<String?>(null)
    }
    var pendingMessage by remember(convoId, currentDeviceId) {
        mutableStateOf<Message?>(null)
    }
    val sentMessages = remember(convoId, currentDeviceId) {
        mutableStateListOf<Message>()
    }
    val messages = (convo.messages + sentMessages).distinctBy { it.messageId }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) { // display each message one by one
            items(messages, key = { it.messageId }) { message ->
                MessageDisplay(message, globalDeviceId == message.sender)
            }
        }

        sendError?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        // input text bar as well as "send" button

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = {
                    draft = it
                    pendingMessage = null
                    sendError = null
                },
                modifier = Modifier.weight(1f),
                label = { Text("Message") },
                enabled = !sending,
                maxLines = 4
            )

            Button(
                enabled = draft.isNotBlank() && !sending,
                onClick = {
                    val message = pendingMessage ?: Message(
                        messageId = UUID.randomUUID().toString(),
                        sender = currentDeviceId,
                        contents = draft.trim()
                    )
                    pendingMessage = message
                    sending = true
                    sendError = null

                    ConversationRepository.updateConversation(
                        convoId = convoId,
                        message = message,
                        onSuccess = {
                            sentMessages.add(message)
                            draft = ""
                            pendingMessage = null
                            sending = false
                        },
                        onError = { error ->
                            sending = false
                            sendError = "Couldn't send message. Try again."
                            Log.e("ConversationScreen", "Message send failed", error)
                        }
                    )
                }
            ) {
                Text(if (sending) "Sending…" else "Send")
            }
        }
    }
}

// old preview, logic shouldn't be used any longer

@Preview
@Composable
fun ConversationDisplayPreview() {

    val msg1 = Message("msgId1", "John Smith", "Hello! I am message 1")
    val msg2 = Message("msgId2", "John Smith", "Greetings! I am message 2")
    val msg3 = Message("msgId3", "John Smith", "I am message 3. Goodbye!")

    val messages = mutableListOf<Message>(msg1, msg2, msg3)
    val participants = mutableListOf<String>("deviceId1", "deviceId2")

    val convo = Conversation("preview", participants, messages)

    ConversationDisplay(convo.conversationId, convo, "deviceId1")

}