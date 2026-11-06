package com.example.syntax_camping.dataClasses
import com.google.firebase.Timestamp
//handles entries in the database lotteries oclletion of those users on the waitlist
class WaitlistEntry( joinedTimestamp: Timestamp, status: String?, userId: String) {

    val status = status
    val userID = userId
}