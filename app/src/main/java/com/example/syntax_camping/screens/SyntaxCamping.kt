package com.example.syntax_camping.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.Database
import com.example.syntax_camping.UserScreens.APP_GUIDELINES_ROUTE
import com.example.syntax_camping.UserScreens.AppGuidelinesScreen
import com.example.syntax_camping.UserScreens.CampsiteDetailsScreen
import com.example.syntax_camping.UserScreens.campsiteInformationDestinations
import com.example.syntax_camping.screens.userScreens.reservationScreens.PastReservationsScreen
import com.example.syntax_camping.screens.userScreens.profileScreen.ProfileScreen
import com.example.syntax_camping.UserScreens.ReservationsScreen
import com.example.syntax_camping.UserScreens.searchScreens.SearchHomeScreen
import com.example.syntax_camping.UserScreens.SettingsScreen
import com.example.syntax_camping.UserScreens.BookCampsiteScreen
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.AdminRepository
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.User
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.example.syntax_camping.screens.adminScreens.AdminBrowseCampsites
import com.example.syntax_camping.screens.adminScreens.AdminBrowseImages
import com.example.syntax_camping.screens.adminScreens.AdminBrowseProfiles
import com.example.syntax_camping.screens.adminScreens.AdminComplaints
import com.example.syntax_camping.screens.adminScreens.adminNotificationLogsDestination
import com.example.syntax_camping.screens.adminScreens.adminUserList.AdminUserItem
import com.example.syntax_camping.screens.adminScreens.adminUserList.userStatusOf
import com.example.syntax_camping.screens.adminScreens.adminUserView.AdminUserDetails
import com.example.syntax_camping.screens.adminScreens.adminUserView.AdminViewUserScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteEditingScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteMessagesScreen
import com.example.syntax_camping.screens.userScreens.profileScreen.UpdateProfileScreen
import com.example.syntax_camping.screens.ownerScreens.LotteryEditScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteOwnerHome
import com.example.syntax_camping.screens.userScreens.notificationsDestination
import com.example.syntax_camping.screens.ownerScreens.lotteryScreens.LotteryDetailsScreen
import com.example.syntax_camping.screens.userScreens.profileScreen.VeiwSearchHistory
import com.example.syntax_camping.screens.userScreens.reservationScreens.CancelledReservationsScreen
import com.example.syntax_camping.screens.userScreens.reservationScreens.ReservationCsvScreen
import com.example.syntax_camping.screens.userScreens.reservationScreens.ReservationInfoScreen

@Composable
fun SyntaxCamping(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel
) {
    val navController = rememberNavController()
    var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }
    //i changed the name from of sel to selected for readability

    var isLoading by remember { mutableStateOf(true) }
    // var rp = CampsiteRepository()
    var documentId = "Test12"
    var selCampsite by remember { mutableStateOf<Campsite?>(null) }
    var selectedLottery by remember { mutableStateOf<Lottery?>(null) }

    NavHost(navController = navController, startDestination = "Search", modifier = modifier) {

        Log.d("UserDebug", "Current User: ${userViewModel.deviceId}")

        composable("campsiteDetails/{campsiteId}") { backStackEntry ->
            // Extract the ID from the navigation route
            val campsiteId = backStackEntry.arguments?.getString("campsiteId")
            CampsiteDetailsScreen(
                navController = navController,
                campsiteID = campsiteId,
                userViewModel = userViewModel,
                update = false,
                onEditCampsite = { campsiteToEdit ->
                    selectedCampsite = campsiteToEdit
                    navController.navigate("UpdateCamp")
                },
                onEditLottery = { campsiteForLottery ->
                    selCampsite = campsiteForLottery
                    selectedLottery = null
                    navController.navigate("CreateLottery")
                },
                onEditExistingLottery = { lotteryToEdit ->
                    selCampsite = null
                    selectedLottery = lotteryToEdit
                    navController.navigate("CreateLottery")
                }
            )
        }

        campsiteInformationDestinations(navController)

        composable("ownerCampsiteDetails/{campsiteId}") { backStackEntry ->
            val campsiteId = backStackEntry.arguments?.getString("campsiteId")
            CampsiteDetailsScreen(
                navController = navController,
                campsiteID = campsiteId,
                userViewModel = userViewModel,
                update = true,
                onEditCampsite = { campsiteToEdit ->
                    selectedCampsite = campsiteToEdit
                    navController.navigate("UpdateCamp")
                },
                onEditLottery = { campsiteForLottery ->
                    selCampsite = campsiteForLottery
                    selectedLottery = null
                    navController.navigate("CreateLottery")
                },
                onEditExistingLottery = { lotteryToEdit ->
                    selCampsite = null
                    selectedLottery = lotteryToEdit
                    navController.navigate("CreateLottery")
                }
            )
        }

        composable("bookCampsite/{campsiteId}/{lotteryId}") { backStackEntry ->
            BookCampsiteScreen(
                navController = navController,
                campsiteId = backStackEntry.arguments?.getString("campsiteId"),
                lotteryId = backStackEntry.arguments?.getString("lotteryId"),
                userViewModel = userViewModel
            )
        }

        composable("Settings") {
            SettingsScreen(navController, userViewModel)
        }
        composable(APP_GUIDELINES_ROUTE) {
            AppGuidelinesScreen(navController)
        }
        notificationsDestination(navController,
            userViewModel.notificationInbox)
        composable("Search") {
            SearchHomeScreen(navController, userViewModel)
        }
        composable("Reservations") {
            ReservationsScreen(navController, userViewModel)
        }
        composable("PastReservations") {
            PastReservationsScreen(navController, userViewModel)
        }
        composable("Profile") {
            ProfileScreen(navController, userViewModel)
        }
        composable("CampsiteOwnerHome") {
            CampsiteOwnerHome(
                navController,
                userViewModel,
                onEditCampsite = { campsiteToEdit ->
                    selectedCampsite = campsiteToEdit
                    navController.navigate("UpdateCamp")
                },
                onEditLottery = { lotteryToEdit ->
                    selCampsite = null
                    selectedLottery = lotteryToEdit
                    navController.navigate("CreateLottery")
                }
            )
        }

        composable("campsiteMessages") {
            CampsiteMessagesScreen(navController, userViewModel)
        }
        composable("CreateLottery") {
            LotteryEditScreen(
                navController,
                userViewModel,
                selectedLottery,
                preselectedCampsite = selCampsite
            )
        }

        composable("LotteryDetailsScreen/{lotteryId}") { backStackEntry ->
            val lotteryId = backStackEntry.arguments?.getString("lotteryId")
            LotteryDetailsScreen(
                navController = navController,
                lotteryId = lotteryId,
                userViewModel = userViewModel,
                onEditLottery = { lotteryToEdit: Lottery ->
                    selCampsite = null
                    selectedLottery = lotteryToEdit
                    navController.navigate("CreateLottery")
                }
            )
        }


        composable("UpdateCamp") {
            CampsiteEditingScreen(
                navController,
                userViewModel,
                selectedCampsite
            )
        }
        composable("UpdateProfile") {
            UpdateProfileScreen(navController, userViewModel, modifier = Modifier)
        }

        adminNotificationLogsDestination(navController)

        composable("adminUserInfo/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            var details by remember(userId) { mutableStateOf<AdminUserDetails?>(null) }
            LaunchedEffect(userId) {
                if (userId.isNotBlank()) {
                    Database().fetchUserDetails(userId) { result ->
                        val user = result.getOrNull()
                        if (user != null) {
                            // show the basics straight away, fill in the rest as it arrives
                            details = AdminUserDetails(
                                name = user.name,
                                email = user.email,
                                phone = user.phoneNumber,
                                status = user.adminStatus(),
                                profileImageUrl = user.profileImageUrl
                            )
                            val admin = AdminRepository()

                            admin.getUserReservations(user) { reservations ->
                                details = details?.copy(reservations = reservations)
                            }

                            admin.getUserCampsites(user) { campsites ->
                                details = details?.copy(campsites = campsites)

                                // reviews this user wrote
                                CampsiteRepository.refreshCampsiteCache {
                                    val reviews = CampsiteRepository.getCampsites().flatMap { campsite ->
                                        campsite.ratings
                                            .filter { it.userId == userId }
                                            .map { campsite to it }
                                    }
                                    details = details?.copy(reviews = reviews)
                                }

                                // lotteries: what the user record says, plus any lottery
                                // their campsites point to that it missed
                                admin.getUserLotteries(user) { owned, _ ->
                                    val all = owned.toMutableList()
                                    val knownIds = all.map { it.lotteryId }.toSet()
                                    val missing = campsites
                                        .mapNotNull { it.lotteryId?.takeIf { id -> id.isNotBlank() } }
                                        .distinct()
                                        .filter { it !in knownIds }

                                    fun showLotteries() {
                                        details = details?.copy(
                                            ownedLotteries = all.filter { !it.isJoinable },
                                            sharedLotteries = all.filter { it.isJoinable }
                                        )
                                    }

                                    if (missing.isEmpty()) {
                                        showLotteries()
                                    } else {
                                        var remaining = missing.size
                                        for (id in missing) {
                                            lotteryRepository().getLottery(id) { lottery ->
                                                if (lottery != null) all.add(lottery)
                                                remaining--
                                                if (remaining == 0) showLotteries()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AdminViewUserScreen(navController, userId, details)
        }

        composable("adminBrowseProfiles") {
            var users by remember { mutableStateOf<List<AdminUserItem>?>(null) }
            LaunchedEffect(Unit) {
                AdminRepository().getAllUsers { loaded ->
                    users = loaded.sortedBy { it.name.lowercase() }
                        .map { AdminUserItem(it.userId, it.name, it.adminStatus()) }
                }
            }
            AdminBrowseProfiles(navController, users.orEmpty(), loading = users == null)
        }

        composable("adminBrowseCampsites") {
            AdminBrowseCampsites(navController)
        }

        composable("adminBrowseImages") {
            AdminBrowseImages(navController)
        }

        composable("adminComplaints") {
            AdminComplaints(navController)
        }

        composable("reservationInfo/{reservationId}") { backStackEntry ->
            val reservationId = backStackEntry.arguments?.getString("reservationId") ?: ""
            ReservationInfoScreen(navController, reservationId)
        }

        composable("History"){
            VeiwSearchHistory(history = userViewModel.searchHistory, { query -> /* navigate or handle click */ }, navController)
        }

        composable("reservationCsv/{reservationId}") { backStackEntry ->
            val reservationId = backStackEntry.arguments?.getString("reservationId") ?: ""
            ReservationCsvScreen(navController, reservationId)
        }
        composable("cancelledReservations") {
            CancelledReservationsScreen(navController, userViewModel)
        }

    }
}

private fun User.adminStatus() = userStatusOf(
    hasCampsites = ownerData?.campsiteIds.orEmpty().any { it.isNotBlank() },
    inLotteryActivity = entrantData?.let {
        it.lotteryEntryIds.isNotEmpty() || it.pendingEntryIds.isNotEmpty() || it.reservationIds.isNotEmpty()
    } == true
)
