package com.example.syntax_camping.dataClasses

class PendingConfirmation(val pendingId: String, val lotteryId: String, val userId: String) {
    val lrepo = lotteryRepository()

    //Note this function should only be called on a completed reservation, so once a reservation has actually been made, not just when user clicks make a reservation, in case they don't complete it right then and
        //need to come back
    fun confirmReservation(){

        lrepo.updateWinnerStatus(lotteryId,userId,"confirmed")
    }
    fun rejectReservation(){

        lrepo.updateWinnerStatus(lotteryId,userId,"rejected")
    }
}