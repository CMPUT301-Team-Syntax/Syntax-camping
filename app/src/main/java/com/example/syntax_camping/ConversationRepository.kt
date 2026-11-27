package com.example.syntax_camping

import com.example.syntax_camping.dataClasses.Conversation
import com.example.syntax_camping.dataClasses.Message
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// ConversationRepository
// Author: Andrew
// documentation won't be provided as none of these methods should be used outside of ConversationScreen.kt
// let me know personally if you need documentation

object ConversationRepository {

    private var _conversations: MutableList<Conversation> = mutableListOf<Conversation>()
    private var refreshVersion = 0L // used so that if our refreshConversationCache fails, we preserve old conversations list

    fun getConversations(): MutableList<Conversation> {
        return _conversations
    }

    // returns a Conversation object based on matching conversationId

    fun getConversation(
        conversationId: String,
        onSuccess: (Conversation) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val conversationRef = FirebaseFirestore.getInstance()
            .collection("conversations")
            .document(conversationId)

        conversationRef.get()
            .addOnSuccessListener { conversation ->
                if (!conversation.exists()) {
                    onError(NoSuchElementException("Conversation '$conversationId' was not found"))
                    return@addOnSuccessListener
                }

                val participants = (conversation.get("participants") as? List<*>)
                    .orEmpty()
                    .filterIsInstance<String>()

                conversationRef.collection("messages").get()
                    .addOnSuccessListener { snapshot ->
                        val messages = snapshot.documents
                            .map { document ->
                                Message(
                                    messageId = document.id,
                                    sender = document.getString("sender").orEmpty(),
                                    contents = document.getString("contents").orEmpty(),
                                    createdAt = document.getTimestamp("createdAt")
                                )
                            }
                            .sortedWith(compareBy<Message> { it.createdAt }.thenBy { it.messageId })
                            .toMutableList()

                        // convert back to mutableList (online resources suggested using List with firebase)

                        onSuccess(
                            Conversation(
                                conversationId = conversation.id,
                                participants = participants.toMutableList(),
                                messages = messages.toMutableList()
                            )
                        )
                    }
                    .addOnFailureListener { error -> onError(error) }
            }
            .addOnFailureListener { error -> onError(error) }

    }

    // similar to CampsiteRepository, we need to refresh the cache, basically "update" our stored list of conversations

    fun refreshConversationCache(
        deviceId: String,
        onSuccess: (List<Conversation>) -> Unit,
        onError: (Exception) -> Unit
    ) {

        // make sure that we don't overwrite our cache with an empty list if error getting data
        val version = ++refreshVersion

        FirebaseFirestore.getInstance()
            .collection("conversations")
            .whereArrayContains("participants", deviceId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (version != refreshVersion) return@addOnSuccessListener

                if (snapshot.isEmpty) {
                    _conversations = mutableListOf<Conversation>()
                    onSuccess(_conversations)
                    return@addOnSuccessListener
                }

                val loaded = mutableListOf<Conversation>()
                var failed = false

                snapshot.documents.forEach { document ->
                    getConversation(
                        conversationId = document.id,
                        onSuccess = { conversation ->
                            if (version == refreshVersion && !failed) {
                                loaded.add(conversation)

                                if (loaded.size == snapshot.size()) {
                                    _conversations = loaded.toMutableList()
                                    onSuccess(_conversations)
                                }
                            }
                        },
                        onError = { error ->
                            if (version == refreshVersion && !failed) {
                                failed = true
                                onError(error)
                            }
                        }
                    )
                }
            }
            .addOnFailureListener { error ->
                if (version == refreshVersion) onError(error)
            }
    }

    // maybe should be in Database.kt ??
    // update a conversation by conversationId with a new message

    fun updateConversation(
        convoId: String,
        message: Message,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("conversations")
            .document(convoId)
            .collection("messages")
            .document(message.messageId)
            .set(
                mapOf(
                    "sender" to message.sender,
                    "contents" to message.contents,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onError(error) }
    }

}