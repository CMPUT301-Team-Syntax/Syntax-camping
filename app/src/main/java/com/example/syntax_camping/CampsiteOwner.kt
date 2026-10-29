package com.example.syntax_camping

/** Made on Oct 28th, 2026
 *  Author: Sorachat Chaichana
 *  A campsite owner subclass for the User superclass.
 *  Works in tandem with Luke's Campsite class.
*/
class CampsiteOwner(
    val userId: String,
    name:String,
    var location: String,
): User(name, 1) {
    //Waitlist should be stored in campsite, I'm gonna check up with Luke on this.
    var campsites = mutableListOf<Campsite>()   //Owner can have 0 to multiple campsites.

    fun addCampsite(campsite: Campsite){
        campsites.add(campsite)
    }

    fun removeCampsite(campsite: Campsite) {
        campsites.remove(campsite)
    }

    fun updateLocation(newLocation: String) {
        location = newLocation
    }

    fun getListWinners(/*campsite: Campsite(campsite will then call lottery to [preserve encapsulation*/){
        //Todo: This would belong in the WaitList class.
    }


}