package com.example.syntax_camping.dataClasses
/*
*Works with database to store a winner entry from the lotteries colelction of a specific lottery
* Used in certain lottery display screens to show a list of winners, and will be used later to determine and inform users who won the lotter
 */
class Winner(lotteryEndTime: Long, bookingEndTime: Long, status: String?, userId: String) {
    val bookingEndTime = bookingEndTime
    val lotteryEndTime = lotteryEndTime

    val status = status
    val userID = userId

}