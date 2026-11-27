package com.example.syntax_camping.dataClasses

//Author: Sorachat Chaichana
//Date: 2026-11-26
//Data class to store images to display in the admin image browsing screen.
data class StoredImage(
    val name: String,
    val downloadUrl: String,
    val storagePath: String,
    val parentId: String,
    val category: String,   //Either user profile's picture or campsite picture
    val uploadedAt: Long
)
