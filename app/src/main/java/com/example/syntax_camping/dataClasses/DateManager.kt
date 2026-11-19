package com.example.syntax_camping.dataClasses

class DateManager(dates: MutableList<Date>) {

    private var _ownerSelectedDates = dates
    private var _filledDates = mutableListOf<Date>()

    private var _availableDates = mutableListOf<Date>()

    val filledDates: MutableList<Date>
        get(){
            updateFilledDates()
            return _filledDates
        }
    val availableDates: MutableList<Date>
        get(){
            updateAvailableDates()
            return _availableDates
        }

    fun updateFilledDates(){
        _filledDates.clear()
        for(date in _ownerSelectedDates){
            if (date.checkFull()){
                _filledDates.add(date)
            }
        }
    }
    fun updateAvailableDates(){
        _availableDates.clear()
        for(date in _ownerSelectedDates){
            if (!date.checkFull()){
                _availableDates.add(date)
            }
        }
    }
    fun addReservation(dateToAddString: String, peopleWith: Int):Boolean{
        for(date in _ownerSelectedDates){
            if(date.dateString == dateToAddString){
                if(date.addOccupants(peopleWith)){
                    return true
                }



            }
        }
        return false
    }
    fun removeReservation(dateToAddString: String, peopleLeaving: Int):Boolean{
        for(date in _ownerSelectedDates){
            if(date.dateString == dateToAddString){
                if(date.removeOccupants(peopleLeaving)){
                    return true
                }



            }
        }
        return false
    }
}