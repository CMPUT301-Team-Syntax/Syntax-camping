package com.example.syntax_camping.dataClasses

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

//This class will handle interactions between the firebase and the dates
class DateRepository {
    val db = FirebaseFirestore.getInstance()

    fun addDateManager(
        documentId: String,
        dateManager: DateManager,
        callback: (Boolean) -> Unit = {}
    ) {
        if (documentId.isBlank() || dateManager.allDates.isEmpty()) {
            callback(false)
            return
        }

        val datesPayload = dateManager.allDates.map {
            mapOf(
                "date" to it.dateString,
                "maxOcc" to it.maxOcc,
                "currentOcc" to it.currentOcc
            )
        }

        db.collection("campsites").document(documentId)
            .update("dates", datesPayload)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}