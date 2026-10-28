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
 */

open class User(var name: String, var userType: Int, var location: String) {

    //Each class has specific details. I don't see the point to this so ill disable it for now
    //open fun addDetails(firstName: String, type: Int) {
    //

    //BUG1
    //private fun getName(): String {
    //    return name
    //}

    private fun getType(): Int {
        return userType
    }

    //BUG2
    //private fun getLocation(): String {
    //    return location
    //}

    //Update the user's information
    open fun updateDetails(newName: String, newType: Int){
        name = newName
        userType = newType
    }

    open fun storeSettings(){
        //For the subclass to store their own settings
    }



}