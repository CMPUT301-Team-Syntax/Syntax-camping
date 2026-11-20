package com.example.syntax_camping.dataClasses

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

//This class will handle interactions between the firebase and the dates
class DateRepository {
//For search will have a function to get all date repositories( so you can check if it has the dates needed in its available ates, tehn pull campsite id from that(daterepsotory has campsite id attached to it
    val db = FirebaseFirestore.getInstance()
    fun addDateManager(documentId:String, dateManager: DateManager){

            db.collection("campsites").document(documentId)
                .update("dateManager", dateManager)
                .addOnSuccessListener {

                }
                .addOnFailureListener { e ->

                }

    }


}