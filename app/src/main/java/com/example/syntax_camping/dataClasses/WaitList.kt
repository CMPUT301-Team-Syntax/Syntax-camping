package com.example.syntax_camping.dataClasses

import com.example.syntax_camping.Campsite
import com.google.firebase.Timestamp

/**
 * WORK IN PROGRESS.
 * Waitlist data class that is stored in a campsite.
 */
data class WaitList(
    val campsite: Campsite,
    val joined: Timestamp
) {
    private var waitlist = mutableListOf<String>()

    fun addUser(userId: String) {
        waitlist.add(userId)
    }

    fun removeUser(userId: String) {
        waitlist.remove(userId)
    }

}