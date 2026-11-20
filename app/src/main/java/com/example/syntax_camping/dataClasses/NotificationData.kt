package com.example.syntax_camping.dataClasses

/** Notification data returned by a database operation after it succeeds. */
sealed interface NotificationData {
    /** Stable identifier for the event, shared by retries or repeated reads. */
    val id: String

    data class LotteryJoined(
        override val id: String,
        val subject: String,
        val joinedWaitlist: Boolean
    ) : NotificationData

    data class Message(
        override val id: String,
        val title: String,
        val message: String
    ) : NotificationData
}
