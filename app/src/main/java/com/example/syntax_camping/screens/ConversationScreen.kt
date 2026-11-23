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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.dataClasses.Conversation
import com.example.syntax_camping.dataClasses.Message

@Composable
fun MessageDisplay(msg: Message) {

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Color.White)
    ) {

        Text(msg.contents)
    }

}

@Composable
fun ConversationDisplay(convo: Conversation) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        for (message in convo.messages) {

            MessageDisplay(message)

        }

    }

}

@Preview
@Composable
fun ConversationDisplayPreview() {

    val msg1 = Message("msgId1", "John Smith", "Hello! I am message 1")
    val msg2 = Message("msgId2", "John Smith", "Greetings! I am message 2")
    val msg3 = Message("msgId3", "John Smith", "I am message 3. Goodbye!")

    val messages = mutableListOf<Message>(msg1, msg2, msg3)
    val participants = mutableListOf<String>("deviceId1", "deviceId2")

    val convo = Conversation(participants, messages)

    ConversationDisplay(convo)

}