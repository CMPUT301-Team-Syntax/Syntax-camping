package com.example.syntax_camping.dataClasses

class Date(date: String, maxOcc: Int) {
    var currentOcc = 0
    var dateString = date
    var maxOcc = maxOcc

    fun addOccupants(newOccupants: Int):Boolean{
        if((currentOcc + newOccupants)<=maxOcc) {
            currentOcc = currentOcc + newOccupants
            return true
        }
        return false
    }
    fun removeOccupants(numRemoved: Int):Boolean{
        if((currentOcc -numRemoved)>=0) {
            currentOcc = currentOcc - numRemoved
            return true
        }
        return false
    }
    fun checkFull():Boolean{
        if (currentOcc == maxOcc){
            return true
        }
        return false
    }

}