package com.example.syntax_camping.dataClasses

import com.example.syntax_camping.Campsite

data class OwnerData(
    var location: String = "",
    var campsites: MutableList<Campsite>
) {}