package com.example.syntax_camping
import com.google.firebase.firestore.FirebaseFirestore

/** USER REPOSITORY FOR DATABASE
 *  Author: Sorachat Chaichana
 *  Primarily interacts with the user database so the user function can call it.
 *  Temporary file, may delete if we figure out something different.
 */
class UserRepository {

    private var _users: MutableList<User> = mutableListOf<User>()

    fun getUsers(): MutableList<User> {
        return _users
    }

    fun getUser(documentID: String, callback: (User) -> Unit ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(documentID)
            .get()
            .addOnSuccessListener { document ->
                callback(
                    User(
                        name = document.getString("displayName") ?: "",
                        userType = document.getLong("userType")?.toInt() ?: 0,
                        location = document.getString("location") ?: ""
                    )
                )

            }

    }

    fun resetUsers() {
        _users.clear()
    }


}