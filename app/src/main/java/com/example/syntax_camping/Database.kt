package com.example.syntax_camping

import android.content.Context
import android.provider.Settings
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.joinToString

// class Database
// author: Andrew
// a class used to handle all database data requests

// NOTE: we must use callbacks as firebase calls are asynchronous
// usage:
// userExistsByDeviceId(someDeviceId) { -> exists
//      if (exists)
//          code to run if user exists
//      else
//          code to run if not
// }

class Database {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // userExistsByDeviceId
    // calls callback function if there is a document with matching document id to device id

    fun userExistsByDeviceId(devId: String, callback: (Boolean) -> Unit) {
        db.collection("users")
            .document(devId)
            .get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // getDeviceId
    // returns a string representation of user's device id

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"

    }

    // updateUser
    // takes in a device id (this will be the document id)
    // and sets the field according to userDetails

    fun updateUser(devId: String, userDetails: User) {
        db.collection("users")
            .document(devId)
            .update(
                mapOf(
                    "displayName" to userDetails.name,
                    "userType" to userDetails.userType,
                )
            )
    }

    // createNewUser
    // takes a device id, creates new document with document id equal to device id
    // sets fields equal to userDetails

    fun createNewUser(devId: String, userDetails: User) {
        db.collection("users")
            .document(devId)
            .set(
                mapOf(
                    "displayName" to userDetails.name,
                    "userType" to userDetails.userType,
                )
            )
    }

    // fetchUserDetailsByDeviceId
    // calls callback function when a matching document is found.

    fun fetchUserDetailsByDeviceId(devId: String, callback: (User) -> Unit) {
        db.collection("users")
            .document(devId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("displayName") ?: "New User"
                    val userType = document.getLong("userType")?.toInt() ?: 0
                    callback(User(name, userType))
                } else {
                    callback(User("New User", 0))
                }
            }
            .addOnFailureListener { e ->
                println("Error fetching user: $e")
                callback(User("New User", 0))
            }
    }

    fun createNewCampsite(campsite: Campsite) {
        db.collection("campsites")
            .document(campsite.documentId)
            .set(
                mapOf(
                    "name" to campsite.name,
                    "location" to campsite.location,
                    "description" to campsite.description,
                    "maxOccupancy" to campsite.maxOcc,
                    "tags" to campsite.tags.joinToString(separator = ",")   // Campsite() stores tags as a MutableList, firestore stores them as a string.
                )
            )
    }

    fun updateCampsite(campsite: Campsite) {
        db.collection("campsites")
            .document(campsite.documentId)
            .update(
                mapOf(
                    "name" to campsite.name,
                    "location" to campsite.location,
                    "description" to campsite.description,
                    "maxOccupancy" to campsite.maxOcc,
                    "tags" to campsite.tags.joinToString(separator = ",")
                )
            )
    }


}