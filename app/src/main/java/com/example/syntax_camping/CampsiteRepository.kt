package com.example.syntax_camping

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField

// object CampsiteRepository
// author: Andrew
/*

I opted to make the CampsiteRepository a static object, not a class
this is because many classes will be using the CampsiteRepository, and we should not
cache all campsites every time we create a new CampsiteRepository class.
Therefore, we should make it static and instantiate once at app startup

Usage:
provided that the MainActivity loads the campsites without errors, we can retrieve
a list of all current campsites with CampsiteRepository.getCamnpsites()

Example:
// print all campsite names
val campsites = CampsiteRepository()

for (campsite in campsite) {
    print(campsite.name())          // we changed getters and setters for Campsite
    // some other operation ...
}

*/

object CampsiteRepository {

    private var _campsites: MutableList<Campsite> = mutableListOf<Campsite>()

    // getCampsites
    // returns _campsites, a mutable list of campsites retrieved from the database

    fun getCampsites(): MutableList<Campsite> {
        return _campsites
    }

    // getCampsite
    // fetches campsite details by documentId from the database
    // constructs and returns Campsite object with these details

    // NOTE: uses callbacks. usage:
    // getCampsite("ExampleDocumentId") { campsite ->
    //      print("Campsite name: ${campsite.getName()}")
    // }

    // NOTE: private for now, for two reasons:  1. callbacks should not be relied on in composables (i think)
    //                                          2. no other class should be using this function

    private fun getCampsite(documentId: String, callback: (Campsite?) -> Unit ) {

        val db = FirebaseFirestore.getInstance()

        db.collection("campsites")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    callback(null)
                    return@addOnSuccessListener
                }

                val campsite = Campsite(
                    documentId = document.id,
                    name = document.getString("name") ?: "",
                    location = document.getString("location") ?: "",
                    description = document.getString("description") ?: "",
                    maxOcc = document.getLong("maxOccupancy")?.toInt() ?: 0,
                    tags = mutableListOf<String>(),
                    document.getDouble("rating") ?: 0.00
                )

                callback(campsite)
            }
            .addOnFailureListener {
                callback(null)
            }

    }

    // refreshCampsiteCache
    // clears _campsites list, fetches all documentIds in campsites collection
    // calls getCampsite on each campsite, adds them to _campsite list

    // usage: call this when app starts up, and whenever you want to refresh with live data from database

    fun refreshCampsiteCache(callback: (Boolean) -> Unit = {}) {

        val db = FirebaseFirestore.getInstance()

        db.collection("campsites")
            .get()
            .addOnSuccessListener { result ->

                val loadedCampsites = mutableListOf<Campsite>()

                for (document in result) {
                    val campsite = Campsite(
                        documentId = document.id,
                        name = document.getString("name") ?: "",
                        location = document.getString("location") ?: "",
                        description = document.getString("description") ?: "",
                        maxOcc = document.getLong("maxOccupancy")?.toInt() ?: 0,
                        tags = (
                                document.get("tags") as? List<*>
                                )?.filterIsInstance<String>()
                            ?.toMutableList()
                            ?: mutableListOf(),

                        document.getDouble("rating") ?: 0.00
                    )

                    loadedCampsites.add(campsite)
                }

                _campsites.clear()
                _campsites.addAll(loadedCampsites)

                callback(true)
            }
            .addOnFailureListener { exception ->
                callback(false)
            }




    }

}