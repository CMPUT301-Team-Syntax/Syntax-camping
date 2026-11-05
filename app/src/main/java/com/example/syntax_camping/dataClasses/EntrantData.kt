package com.example.syntax_camping.dataClasses

data class EntrantData(
   val reservationIds: MutableList<String> = mutableListOf(),
   val lotteryEntryIds: MutableList<String> = mutableListOf()
) {}