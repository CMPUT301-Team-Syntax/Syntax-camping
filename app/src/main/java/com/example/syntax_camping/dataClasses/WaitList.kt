package com.example.syntax_camping.dataClasses

import com.example.syntax_camping.Campsite
import com.google.firebase.Timestamp

/**
 * WORK IN PROGRESS.
 * Waitlist data class that is stored in a campsite.
 * The waitlist itself
 */
class WaitList(
    val campsite: Campsite,
    val joined: Timestamp
) {
    //Contains a list of Entrant class because we need to access their name.
    private var waitlist = mutableListOf<String>()

    fun addUser(entrantId: String) {
        waitlist.add(entrantId)
    }

    fun removeUser(entrantId: String) {
        waitlist.remove(entrantId)
    }

    fun getListWinners(){
        //Todo
    }

}