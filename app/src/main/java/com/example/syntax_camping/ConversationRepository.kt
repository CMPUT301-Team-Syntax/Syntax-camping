package com.example.syntax_camping

import com.example.syntax_camping.dataClasses.Conversation
import com.example.syntax_camping.dataClasses.Message
import com.google.firebase.firestore.FirebaseFirestore

object ConversationRepository {

    private var _conversations: MutableList<Conversation> = mutableListOf<Conversation>()
    private var refreshVersion = 0L // used so that if our refreshConversationCache fails, we preserve old conversations list

    fun getConversations(): MutableList<Conversation> {
        return _conversations
    }

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
                        val messages = snapshot.documents.map { document ->
                            Message(
                                document.id,
                                contents = document.getString("contents").orEmpty(),
                                sender = document.getString("sender").orEmpty()
                            )
                        }

                        onSuccess(
                            Conversation(
                                participants.toMutableList(),
                                messages.toMutableList()
                            )
                        )
                    }
                    .addOnFailureListener { error -> onError(error) }
            }
            .addOnFailureListener { error -> onError(error) }

    }

    fun refreshConversationCache(
        deviceId: String,
        onSuccess: (List<Conversation>) -> Unit,
        onError: (Exception) -> Unit
    ) {
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

}