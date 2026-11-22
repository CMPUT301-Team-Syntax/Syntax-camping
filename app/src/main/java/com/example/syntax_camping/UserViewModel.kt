package com.example.syntax_camping

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.syntax_camping.dataClasses.LotteryEntry
import com.example.syntax_camping.dataClasses.NotificationData
import com.example.syntax_camping.dataClasses.User
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration



class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val database = Database()

    val deviceId: String = database.getDeviceId(application)
    internal val notificationInbox = NotificationInbox(application, deviceId)
    private val joiningLotteries = mutableSetOf<String>()
    private val lotteries = lotteryRepository()
    private val announcedPendingIds = mutableSetOf<String>()
    private val announcedResults = mutableSetOf<String>()
    private var entrantListener: ListenerRegistration? = null
    var currentUser by mutableStateOf<User?>(null)
        private set  //can only change current user through the refreshCurrentUser function

    init {
        refreshCurrentUser()
        // ADDED: when our entrantData changes in Firestore (e.g. the draw picks us), reload the user so win notifications appear
        entrantListener = FirebaseFirestore.getInstance()
            .collection("entrantData")
            .document(deviceId)
            .addSnapshotListener { _, _ -> refreshCurrentUser() }
    }

    override fun onCleared() {
        entrantListener?.remove()
        super.onCleared()
    }

    /** Displays the database result and saves it in the notification inbox. */
    fun showNotification(notification: NotificationData) {
        viewModelScope.launch { notificationInbox.showNotification(notification) }
    }

    internal fun joinLottery(lotteryId: String, campsiteId: String? = null, onComplete: (Boolean) -> Unit) {
        if (!joiningLotteries.add(lotteryId)) {
            onComplete(false)
            return
        }
        val entry = LotteryEntry(deviceId, Timestamp.now())
        database.registerLotteryEntry(lotteryId, entry, campsiteId) { notification ->
            joiningLotteries.remove(lotteryId)
            notification?.let(::showNotification)
            onComplete(notification != null)
        }
    }

    // ADDED: shows a "you won" notification for every lottery the draw picked this user in
    private fun announceWins(user: User) {
        val pendingIds = user.entrantData?.pendingEntryIds ?: return
        for (pendingId in pendingIds) {
            if (!announcedPendingIds.add(pendingId)) continue
            lotteries.getPendingConfirmation(pendingId) { pending ->
                if (pending == null) {
                    announcedPendingIds.remove(pendingId)
                    return@getPendingConfirmation
                }
                lotteries.getLottery(pending.lotteryId) { lottery ->
                    val lotteryName = lottery?.name?.takeIf { it.isNotBlank() } ?: "a lottery"
                    showNotification(
                        NotificationData.Message(
                            id = "won:$pendingId",
                            title = "You won the lottery!",
                            message = "You were picked in $lotteryName. Open the lottery to choose a campsite and your dates."
                        )
                    )
                }
            }
        }
    }

    // ADDED: tells entrants who were not picked that they are on the waitlist (or that the lottery is over)
    private fun announceLostDraws(user: User) {
        val entered = user.entrantData?.lotteryEntryIds ?: return
        for (lotteryId in entered) {
            if (!announcedResults.add(lotteryId)) continue
            lotteries.getLottery(lotteryId) { lottery ->
                if (lottery == null || lottery.status == "initialAccepting") {
                    announcedResults.remove(lotteryId)   // not drawn yet, check again next time
                    return@getLottery
                }
                lotteries.getWinners(lotteryId) { winners ->
                    if (winners.any { it.userID == deviceId }) return@getWinners
                    lotteries.getWaitlist(lotteryId) { waitlist ->
                        val name = lottery.name.ifBlank { "a lottery" }
                        val onWaitlist = waitlist.any { it.userID == deviceId }
                        showNotification(
                            NotificationData.Message(
                                id = "lost:$lotteryId",
                                title = if (onWaitlist) "You're on the waitlist" else "Lottery ended",
                                message = if (onWaitlist)
                                    "You weren't picked in $name. You're on the waitlist and will be offered a spot if one opens up."
                                else
                                    "You weren't picked in $name. Sorry, it's over."
                            )
                        )
                    }
                }
            }
        }
    }

    fun refreshCurrentUser() {
        database.fetchUserDetails(deviceId) { result ->
            result.onSuccess { user ->
                user?.let {
                    announceWins(it)
                    announceLostDraws(it)
                }
                currentUser = user
                Log.d("UserDebug", "Successfully fetched current user")
            }.onFailure { error ->
                Log.e("UserDebug", "Failed to fetch user", error)
            }
        }
    }
}
