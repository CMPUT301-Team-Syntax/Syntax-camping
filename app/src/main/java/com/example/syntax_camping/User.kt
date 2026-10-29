package com.example.syntax_camping

/**IN PROGRESS
 * Author: Sorachat Chaichana
 *
 * Superclass for all users of the app.
 *
 *
 * NOTE FOR SELF: Andrew's database class is the one creating the actual user,
 * we access the collectionPath and edit its data.
 *
 * Assuming that this is a singular user, all data will be handled as if it was.
 * Will be updated as user gets updated.
 */

open class User(var name: String, var userType: Int) {

    //Update the user's information
    open fun updateName(newName: String) {
        name = newName
    }

    open fun updateUserType(newType: Int) {
        userType = newType
    }

    open fun storeSettings(){
        //For the subclass to store their own settings
    }



}