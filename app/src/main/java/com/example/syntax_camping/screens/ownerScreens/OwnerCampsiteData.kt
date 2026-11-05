package com.example.syntax_camping.screens.ownerScreens

import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

/** Reads the existing campsite repository without changing user/account handling. */
internal suspend fun loadOwnerCampsitesFromRepository(): List<Campsite> = withTimeout(15_000L) {
    suspendCancellableCoroutine<List<Campsite>> { continuation ->
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
