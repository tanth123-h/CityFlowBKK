package com.example.cityflowbkk.features.home

data class BangkokPlace(
    val name: String,
    val nearestStation: String,
    val primaryCategory: Category,
    val imageRes: Int,
    val travelNotice: String,
)
