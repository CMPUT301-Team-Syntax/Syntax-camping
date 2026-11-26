package com.example.syntax_camping.dataClasses

class PendingConfirmation(val pendingId: String, val lotteryId: String, val userId: String) {
    val lrepo = lotteryRepository()

    //Note this function should only be called on a completed reservation, so once a reservation has actually been made, not just when user clicks make a reservation, in case they don't complete it right then and
        //need to come back

    //added the callback (check lotteryrepo file, change as needed, needed notifs to work)
    fun confirmReservation(callback: (Boolean) -> Unit = {}) {
        lrepo.updateWinnerStatus(lotteryId, userId, "confirmed", pendingId, callback)
    }
    fun rejectReservation(callback: (Boolean) -> Unit = {}) {
        lrepo.updateWinnerStatus(lotteryId, userId,"rejected", pendingId, callback
        )
    }
}