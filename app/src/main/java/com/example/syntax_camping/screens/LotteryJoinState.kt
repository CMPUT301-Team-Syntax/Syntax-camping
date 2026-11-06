package com.example.syntax_camping.screens

// NEW FILE. What the campsite details screen offers a visitor, based on the lottery's status.
// Plain function, so it can be unit tested with plain JUnit.
enum class LotteryJoinState { CAN_ENTER_LOTTERY, CAN_JOIN_WAITLIST, ENDED, NONE }

fun lotteryJoinState(status: String?): LotteryJoinState = when (status) {
    "initialAccepting" -> LotteryJoinState.CAN_ENTER_LOTTERY
    "booking" -> LotteryJoinState.CAN_JOIN_WAITLIST
    "complete" -> LotteryJoinState.ENDED
    else -> LotteryJoinState.NONE
}