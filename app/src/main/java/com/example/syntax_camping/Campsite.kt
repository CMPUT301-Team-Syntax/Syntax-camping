package com.example.syntax_camping

class Campsite(name: String, location: String, description: String, maxOcc: Int, tags: MutableList<String>, /*mapLocation<MapLocation> */) {
    //Please note whoever implements the ability to go in and out of campsite page, when ever they click details should reset campsite carousel index
    private var _currImageIndex = 0
    private var _name = name

    private var _location = location

    private var _description = description

    private var _maxOcc = maxOcc

    private val _tags =tags

    private var _images = mutableListOf<Image>()

    fun getName(): String{
        return _name
    }
    fun getLocation(): String{
        return _location
    }

    fun getDescription(): String{
        return _description
    }

    fun getmaxOcc(): Int{
        return _maxOcc
    }

    fun getTags(): List<String>{
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

    fun addTag(tag: String){
        _tags.add(tag)
    }
    fun removeTag(tag: String){
        _tags.remove(tag)
    }
    fun addImage(image:Image){
        _images.add(image)
    }
    fun removeImage(image:Image){
        _images.remove(image)
    }

    fun getCurrPhoto(): String{
        if(_images.size == 0)
            return "No image Found"
        else
            return _images[_currImageIndex].getUrl()
    }
    fun getNextPhoto():String{
        if(_images.size == 0)
            return "No image Found"
        else if(_currImageIndex == (_images.size-1)){
            _currImageIndex = 0
            return _images[_currImageIndex].getUrl()

        }
        else
            _currImageIndex += 1
            return _images[_currImageIndex].getUrl()

    }
    fun resetIndex(){
        _currImageIndex = 0
    }



}