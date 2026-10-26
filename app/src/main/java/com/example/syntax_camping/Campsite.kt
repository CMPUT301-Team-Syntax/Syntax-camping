package com.example.syntax_camping

import android.R.attr.description
import android.R.attr.name

/*
* THis class is dedicated to the campsite object. It is how all of the data about how a campsite is stored,
* and contains functions to update it and to help display it(most of the dispalying of it has to do with the image list)
 */

class Campsite(documentId: String, name: String, location: String, description: String, maxOcc: Int, tags: MutableList<String>, /*mapLocation<MapLocation> */) {
    //Please note whoever implements the ability to go in and out of campsite page, when ever they click details should reset campsite carousel index
    private var _hasQRCode = false
    private var _currImageIndex = 0
    private var _name = name
    private var _location = location
    private var _description = description
    private var _maxOcc = maxOcc
    private val _tags =tags
    private var _images = mutableListOf<Photo>()
    fun getName(): String{
        return _name
    }
    fun getLocation(): String{
        return _location
    }

    fun getDescription(): String{
        return _description
    }
    /*
    * These functions for QR code are just filler until QR code is fully implemented
    * */
    fun generateQRcode(){
        _hasQRCode = true
    }
    fun getQRCode(): Boolean{
        return _hasQRCode
    }
    fun getmaxOcc(): Int{
        return _maxOcc
    }

    fun getTags(): MutableList<String>{
        return _tags
    }
    fun updateName(newName: String){
        _name = newName
    }
    fun updateLocation(newLocation: String){
         _location = newLocation
    }

    fun updateDescription(newDescription: String){
        _description = newDescription
    }

    fun updateMaxOcc(newMaxOcc: Int){
        _maxOcc = newMaxOcc
    }

    fun updateTag(newTags: MutableList<String>){
        _tags.clear()
        for (item in newTags){
            _tags.add(item)
        }
    }
    fun addImage(image:Photo){
        _images.add(image)
    }
    fun removeImage(image:Photo){
        _images.remove(image)
    }
    //returns the current photo, checking first to make sure na image exists
    fun getCurrPhoto(): String{
        if(_images.size == 0)
            return "No image Found"
        else
            return _images[_currImageIndex].getUrl()
    }
    fun getPhotos(): MutableList<Photo>{
        return _images
    }
    //returns the next phot in the list, loops back to the start if it can, and if there is no images returns no image found
    fun getNextPhoto():String{
        if(_images.size == 0) {
            return "No image Found"
        }
        else if(_currImageIndex == (_images.size-1)){
            _currImageIndex = 0
            return _images[_currImageIndex].getUrl()

        }
        else {
            _currImageIndex += 1
            return _images[_currImageIndex].getUrl()
        }
    }
    fun getPreviousPhoto():String{
        if(_images.size == 0) {
            return "No image Found"
        }
        else if(_currImageIndex == (0)){
            _currImageIndex = _images.size - 1
            return _images[_currImageIndex].getUrl()

        }
        else {
            _currImageIndex -= 1
        }

        return _images[_currImageIndex].getUrl()

    }
    //resets the photo index, should be called each time you leavve a page with an image caraousel
    fun resetIndex(){
        _currImageIndex = 0
    }


}