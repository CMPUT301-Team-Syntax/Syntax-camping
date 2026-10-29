package com.example.syntax_camping.dataClasses

import com.example.syntax_camping.Campsite
import com.google.firebase.Timestamp

//WORK IN PROGRESS
//Stores the lottery entries data (campsite info & time joined) itself!
//Works with Entrant class.
data class LotteryEntry(
    val userId: String,
    val campsite: Campsite,
    val joined: Timestamp
) {}