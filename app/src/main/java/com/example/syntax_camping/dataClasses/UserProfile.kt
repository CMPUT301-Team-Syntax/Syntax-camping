package com.example.syntax_camping.dataClasses

class UserProfile(
    val user: User,
    val entrantData: EntrantData? = null,
    val ownerData: OwnerData? = null
) {}