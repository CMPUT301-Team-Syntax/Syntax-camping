package com.example.syntax_camping.dataClasses

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



data class User(
    val userId: String,
    var name: String,
    var email: String,
    var phoneNumber: String,
    val entrantData: EntrantData? = null,
    val ownerData: OwnerData? = null
)