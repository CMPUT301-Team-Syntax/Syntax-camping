package com.example.syntax_camping.dataClasses

import com.google.firebase.Timestamp

// simple class to store messages

class Message(
    val messageId: String,
    val sender: String,
    val contents: String,
    val createdAt: Timestamp? = null
)
class Conversation(
    val conversationId: String,
    val participants: MutableList<String>,
    val messages: MutableList<Message>
)