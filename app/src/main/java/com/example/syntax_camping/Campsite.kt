package com.example.syntax_camping

import android.R.attr.description
import android.R.attr.name
import android.media.Rating
import android.net.Uri
import androidx.core.net.toUri
import com.example.syntax_camping.dataClasses.LotteryManager
/*
* THis class is dedicated to the campsite object. It is how all of the data about how a campsite is stored,
* and contains functions to update it and to help display it(most of the dispalying of it has to do with the image list)
 */

class Campsite(documentId: String, name: String, location: String, description: String, maxOcc: Int, tags: MutableList<String>, rating: Double, images: MutableList<String> = mutableListOf<String>(), /*mapLocation<MapLocation> */) {
    //Please note whoever implements the ability to go in and out of campsite page, when ever they click details should reset campsite carousel index
    private var _hasQRCode = false
    private lateinit var _lotteryId: String
    private var _currImageIndex = 0
    private var _name = name
    private var _location = location
    private var _description = description
    private var _maxOcc = maxOcc
    private val _tags =tags
    private var _images = mutableListOf<Uri>()
    private var _documentId= documentId
    private var _rating =  rating
    private var _raters = 0 //number of people that have rated

    val name: String
        get() = _name
    val location: String
        get() = _location
    val description: String
        get() = _description
    val maxOcc: Int
        get() = _maxOcc
    val tags: MutableList<String>
        get() = _tags
    val images: MutableList<Uri>
        get() = _images
    val documentId: String
        get() = _documentId
    val hasQRCode: Boolean
        get() = _hasQRCode
    val rating: Double
        get() = _rating
    val raters: Int
        get() = _raters
    val lotteryId: String
        get() = _lotteryId
    /*
    * These functions for QR code are just filler until QR code is fully implemented
    * */
    //YES I KNOW THAT CAMPSITE LIKeLY WILL NOT HANDLE QR CODE GENERATION AND THIS FUNCTION WILL BE DELETED LATER, I AM JUST USING IT FOR NOW
    fun updateRating(number: Double){

        _rating = (_rating*_raters) + number
        _raters += 1
        _rating /= _raters
    }

    fun generateQRcode(){
        _hasQRCode = true
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
    fun addImage(image:Uri){
        _images.add(image)
    }
    fun removeImage(image:Uri){
        _images.remove(image)
    }

    fun getImagesAsStrs(): MutableList<String> {

        val imagesAsStrs = mutableListOf<String>()
        for (image in _images) {
            imagesAsStrs.add(image.toString())
        }

        return imagesAsStrs

    }

    // Database stores images as a list of string.
    // On init, we must parse these strings as Uris
    fun updateLotteryId(newLotteryId: String){
        _lotteryId= newLotteryId
    }
    init {

        for (image in images) {

            addImage(image.toUri())

        }

    }



}