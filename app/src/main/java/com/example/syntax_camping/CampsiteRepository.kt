package com.example.syntax_camping

import com.google.firebase.firestore.FirebaseFirestore

class CampsiteRepository {

    private var _campsites: MutableList<Campsite> = mutableListOf<Campsite>()

    fun getCampsites(): MutableList<Campsite> {
        return _campsites
    }

    fun updateRating(campsite: Campsite, num: Double){
        campsite.updateRating(num)
    }

    fun getCampsite(documentId: String, callback: (Campsite) -> Unit ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("campsites")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->



                callback(Campsite(
                    documentId = document.id,
                    name = document.getString("name") ?: "",
                    location = document.getString("location") ?: "",
                    description = document.getString("description") ?: "",
                    maxOcc = 0,
                    tags = mutableListOf<String>(),
                    rating = 0.0
                ))
            }

    }

    fun updateCampsites() {

        _campsites.clear()

        // get all campsite documentIds, call getCampsite() on each one, store each in _campsites

    }

}