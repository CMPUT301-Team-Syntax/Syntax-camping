package com.example.syntax_camping.dataClasses
import com.google.firebase.Timestamp
class WaitlistEntry( joinedTimestamp: Timestamp, status: String?, userId: String) {

    val status = status
    val userID = userId
}