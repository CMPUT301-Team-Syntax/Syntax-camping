package com.example.syntax_camping.dataClasses

import com.example.syntax_camping.Campsite

/**
 * WORK IN PROGRESS
 * Lottery entries that should be stored with the campsite itself.
 */
data class LotteryEntry (
    val campsite: Campsite,
    val number: Int,
) {}