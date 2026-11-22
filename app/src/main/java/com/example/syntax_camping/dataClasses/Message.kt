package com.example.syntax_camping.dataClasses

// simple class to store messages

class Message(val messageId: String, val sender: String, val contents: String)
class Conversation(val participants: MutableList<String>, val messages: MutableList<Message>)