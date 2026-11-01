package com.example.syntax_camping
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.toMutableList
import kotlin.text.split

/** USER REPOSITORY FOR DATABASE
 *  Author: Sorachat Chaichana
 *  Primarily interacts with the user database so the user function can call it.
 *  Temporary file, may delete if we figure out something different.
 */
class UserRepository {

    private var _users: MutableList<User> = mutableListOf<User>()

    //Returns _users, a mutable list of all users retrieved from the database.
    fun getUsers(): MutableList<User> {
        return _users
    }

    //Fetch user details by documentId from database
    //Constructs & return user object with associated details.

    //Since I'm following Andrew's CampsiteRepository file, I'll private this for now too for the same reasons.
    //Also, this may mess with the database if handled in a composable. Maybe we could use this through a User superclass funciton instead?
    private fun getUser(documentId: String, callback: (User?) -> Unit ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    callback(null)
                    return@addOnSuccessListener
                }

                callback(
                    User(
                        name = document.getString("displayName") ?: "",
                        userType = document.getLong("userType")?.toInt() ?: 0,
                    )
                )

            }
            .addOnFailureListener {
                callback(null)
            }
    }


    // Clears all users in _users list and fetch all documentIds in users collection
    // calls getUser on each campsite to add them back into the _user mutable list.
    // As the name suggests, it acts as a literal refresh so that data gets constantly updated between screens.
    // At least I think that's what Andrew means :D
    //(callback: Boolean -> Unit = {}) is basically an empty function that returns true if it passed.
    fun refreshUserCache(callback: (Boolean) -> Unit = {}) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val loadedUsers = mutableListOf<User>()

                for (document in result) {
                    val user = User(
                        name = document.getString("displayName") ?: "",
                        userType = document.getLong("userType")?.toInt() ?: 0,
                    )

                    loadedUsers.add(user)
                }

                _users.clear()
                _users.addAll(loadedUsers)

                callback(true)
            }
            //Fail case
            .addOnFailureListener { exception ->
                callback(false)
            }
    }
}