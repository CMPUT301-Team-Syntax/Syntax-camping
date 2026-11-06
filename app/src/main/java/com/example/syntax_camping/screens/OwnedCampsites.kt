package com.example.syntax_camping.screens

import com.example.syntax_camping.Campsite
import com.example.syntax_camping.dataClasses.OwnerData

// NEW FILE. Returns only the campsites whose documentId is in the owner's campsiteIds.
// Plain function, so it can be called from any screen and unit tested with plain JUnit.
// Returns an empty list if the user or their ownerData hasn't loaded yet.
fun filterOwnedCampsites(allCampsites: List<Campsite>, ownerData: OwnerData?): List<Campsite> {
    val ownedIds = ownerData?.campsiteIds ?: return emptyList()
    return allCampsites.filter { it.documentId in ownedIds }
}