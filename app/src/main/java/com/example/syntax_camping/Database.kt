package com.example.syntax_camping

import android.content.Context
import android.provider.Settings
import com.google.firebase.firestore.FirebaseFirestore

class Database {

    // Andrew
    // userExistsByDeviceId(String):
    // returns 1 if user exists with specified deviceId
    // returns 0 if not

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    public fun userExistsByDeviceId(devId: String): Boolean {

        val usersRef = db.collection("users")
        val query = usersRef.whereEqualTo("deviceId", devId)

        // return if !query is emprty

        return true

    }


    public fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"
    }



    //
    // todo: print current campsites


}