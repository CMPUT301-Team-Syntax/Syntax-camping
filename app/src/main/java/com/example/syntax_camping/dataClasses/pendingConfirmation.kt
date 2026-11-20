package com.example.syntax_camping.dataClasses

class pendingConfirmation(val lotteryID: String, val userId: String) {
    val lrepo = lotteryRepository()
    //Note this function should only be called on a completed reservation, so once a reservation has actually been made, not just when user clicks make a reservation, in case they don't complete it right then and
        //need to come back
    fun confirmReservation(){

        lrepo.updateWinnerStatus(lotteryID,userId,"confirmed")
    }
    fun rejectReservation(){

        lrepo.updateWinnerStatus(lotteryID,userId,"rejected")
    }
}