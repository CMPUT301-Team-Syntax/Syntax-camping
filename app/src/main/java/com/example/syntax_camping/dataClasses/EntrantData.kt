package com.example.syntax_camping.dataClasses

data class EntrantData(
   val reservations: MutableList<Reservation> = mutableListOf(),
   val lotteryEntries: MutableList<LotteryEntry> = mutableListOf()
) {}