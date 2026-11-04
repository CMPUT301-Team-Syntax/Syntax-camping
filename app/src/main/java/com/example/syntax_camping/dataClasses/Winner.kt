package com.example.syntax_camping.dataClasses

class Winner(lotteryEndTime: Long, bookingEndTime: Long, status: String?, userId: String) {
    val bookingEndTime = bookingEndTime
    val lotteryEndTime = lotteryEndTime

    val status = status
    val userID = userId

}