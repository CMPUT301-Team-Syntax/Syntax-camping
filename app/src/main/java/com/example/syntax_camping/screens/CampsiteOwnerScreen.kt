package com.example.syntax_camping.screens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.screens.ownerScreens.CampsiteDetailsLotteryScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteEditingScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteMessagesScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteProfileScreen
import com.example.syntax_camping.screens.ownerScreens.CampsiteSettingsScreen
import com.example.syntax_camping.screens.ownerScreens.MyCampsitesScreen
import com.example.syntax_camping.screens.ownerScreens.OwnerCampsiteDetailsScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

/** Renders the campsites returned by the team's existing database repository. */
@Composable
fun CampsiteOwnerScreen(modifier: Modifier = Modifier) {
    val loadCampsites: suspend () -> List<Campsite> = remember {
        {
            withTimeout(15_000L) {
                suspendCancellableCoroutine<List<Campsite>> { continuation ->
                    // Wait for the asynchronous refresh before reading the cache.
                    // Account integration remains in the team's shared data layer.
                    CampsiteRepository.refreshCampsiteCache { success ->
                        if (continuation.isActive) {
                            continuation.resumeWith(
                                if (success) Result.success(CampsiteRepository.getCampsites().toList())
                                else Result.failure(IllegalStateException(
                                    "Could not load campsites. Please try again."
                                ))
                            )
                        }
                    }
                }
            }
        }
    }
    OwnerCampsitesNavigation(modifier = modifier, loadCampsites = loadCampsites)
}

/** The loader is injectable so navigation can be checked without a live database. */
@Composable
internal fun OwnerCampsitesNavigation(
    modifier: Modifier = Modifier,
    loadCampsites: suspend () -> List<Campsite>
) {
    val navController = rememberNavController()
    var campsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshRequest by remember { mutableIntStateOf(0) }
    val entry by navController.currentBackStackEntryAsState()

    // Refresh on entry and on return from editing. A copied list triggers Compose
    // updates even though the existing data layer exposes mutable model objects.
    LaunchedEffect(entry, refreshRequest, loadCampsites) {
        val route = entry?.destination?.route
        if (route != "campsiteOwner" && route != "ownerCampsiteDetails/{campsiteId}") {
            return@LaunchedEffect
        }
        isLoading = true
        errorMessage = null
        try {
            campsites = loadCampsites().toList()
        } catch (_: TimeoutCancellationException) {
            errorMessage = "Loading campsites took too long. Please try again."
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            errorMessage = error.message ?: "Could not load campsites. Please try again."
        } finally {
            isLoading = false
        }
    }

    NavHost(
        navController = navController,
        startDestination = "campsiteOwner",
        modifier = modifier
    ) {
        composable("campsiteOwner") {
            MyCampsitesScreen(
                navController = navController,
                campsites = campsites,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onRetry = { refreshRequest++ },
                onCampsiteClick = { campsite ->
                    navController.navigate("ownerCampsiteDetails/${Uri.encode(campsite.documentId)}")
                }
            )
        }
        composable(
            route = "ownerCampsiteDetails/{campsiteId}",
            arguments = listOf(navArgument("campsiteId") { type = NavType.StringType })
        ) { detailsEntry ->
            val campsiteId = detailsEntry.arguments?.getString("campsiteId")
            OwnerCampsiteDetailsScreen(
                navController = navController,
                campsite = campsites.find { it.documentId == campsiteId },
                isLoading = isLoading,
                errorMessage = errorMessage,
                onRetry = { refreshRequest++ },
                onEditCampsite = { campsite ->
                    navController.navigate("UpdateCamp/${Uri.encode(campsite.documentId)}")
                }
            )
        }
        composable("campsiteLottery") { CampsiteDetailsLotteryScreen(navController) }
        composable("campsiteMessages") { CampsiteMessagesScreen(navController) }
        composable("campsiteProfile") { CampsiteProfileScreen(navController) }
        composable("campsiteSettings") { CampsiteSettingsScreen(navController) }
        // The shared owner bottom bar currently uses this route spelling.
        composable("CAMPSITESettings") { CampsiteSettingsScreen(navController) }
        composable("UpdateCamp") {
            CampsiteEditingScreen(navController, null)
        }
        composable(
            route = "UpdateCamp/{campsiteId}",
            arguments = listOf(navArgument("campsiteId") { type = NavType.StringType })
        ) { editEntry ->
            val campsiteId = editEntry.arguments?.getString("campsiteId")
            val campsite = campsites.find { it.documentId == campsiteId }
            if (campsite == null) {
                OwnerCampsiteDetailsScreen(
                    navController = navController,
                    campsite = null,
                    onEditCampsite = {}
                )
            } else {
                CampsiteEditingScreen(navController, campsite)
            }
        }
    }
}
