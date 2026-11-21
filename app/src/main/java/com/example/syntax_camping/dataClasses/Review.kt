package com.example.syntax_camping.dataClasses

class Review(rating:Double, userId: String, reviewText: String, reviewerName: String = "", timestamp: Long = 0L) {
    val rating = rating
    val userId = userId
    val reviewText = reviewText
    //added reviewer name and timestamp
    val reviewerName = reviewerName
    val timestamp = timestamp
}