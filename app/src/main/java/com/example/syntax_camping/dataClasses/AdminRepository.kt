package com.example.syntax_camping.dataClasses

import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.Database
/*
* works with any admin calls, is how the admin screens get information tied to a user
 */
class AdminRepository {
    private val database = Database()
    private val reservationRepo = ReservationRepository()
    private val lotteryRepo = lotteryRepository()

    //Ok so this one will get you a list of users of type user, which you can then get info from(will not have proifle pictures atm since that is not implementd
    fun getAllUsers(callback: (List<User>) -> Unit) {
        database.refreshUserCache { success ->
            if (success) {
                callback(database.users)
            } else {
                callback(emptyList())
            }
        }
    }

    // This gets you a list of campsites owned by a user, the user being passed as an argument
    fun getUserCampsites(user: User, callback: (List<Campsite>) -> Unit) {
        val targetIds = user.ownerData?.campsiteIds
            ?.filter { it.isNotBlank() }
            ?.toSet()
            .orEmpty()

        if (targetIds.isEmpty()) {
            callback(emptyList())
            return
        }

        val cachedMatches = CampsiteRepository.getCampsites()
            .filter { it.documentId in targetIds }

        if (cachedMatches.size == targetIds.size) {
            callback(cachedMatches)
            return
        }

        CampsiteRepository.refreshCampsiteCache {
            val refreshedMatches = CampsiteRepository.getCampsites()
                .filter { it.documentId in targetIds }
            callback(refreshedMatches)
        }
    }

    // same as cmapsite above, but gets the reservations associated with that user
    fun getUserReservations(user: User, callback: (List<Reservation>) -> Unit) {
        val reservationIds = user.entrantData?.reservationIds
            ?.filter { it.isNotBlank() }
            ?.distinct()
            .orEmpty()

        if (reservationIds.isEmpty()) {
            callback(emptyList())
            return
        }

        val results = mutableListOf<Reservation>()
        var completed = 0

        for (resId in reservationIds) {
            reservationRepo.getReservation(resId) { reservation ->
                synchronized(results) {
                    if (reservation != null) {
                        results.add(reservation)
                    }
                    completed++
                    if (completed == reservationIds.size) {
                        callback(results.toList())
                    }
                }
            }
        }
    }

    // gets all the lotteries associated with a user
    fun getUserLotteries(
        user: User,
        callback: (ownedLotteries: List<Lottery>, enteredLotteries: List<Lottery>) -> Unit
    ) {
        val ownedIds = user.ownerData?.lotteryIds
            ?.filter { it.isNotBlank() }
            ?.toSet()
            .orEmpty()

        val enteredIds = (
                user.entrantData?.lotteryEntryIds.orEmpty() +
                        user.entrantData?.pendingEntryIds.orEmpty()
                ).filter { it.isNotBlank() }.toSet()

        lotteryRepo.getLotteries { allLotteries ->
            val owned = allLotteries.filter {
                it.lotteryId in ownedIds || it.ownerId == user.userId
            }
            val entered = allLotteries.filter {
                it.lotteryId in enteredIds
            }
            callback(owned, entered)
        }
    }
}