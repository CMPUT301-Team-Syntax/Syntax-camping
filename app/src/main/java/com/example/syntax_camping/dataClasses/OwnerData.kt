package com.example.syntax_camping.dataClasses

import com.example.syntax_camping.Campsite

data class OwnerData(
    var lotteryIds: MutableList<String> = mutableListOf(),
    var campsiteIds: MutableList<String>  = mutableListOf()
) {}