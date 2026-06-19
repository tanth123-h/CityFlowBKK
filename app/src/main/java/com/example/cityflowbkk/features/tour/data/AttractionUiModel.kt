package com.example.cityflowbkk.features.tour.data

import androidx.compose.runtime.Immutable

@Immutable
data class AttractionUiModel(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val photoUrl: String?,
    val rating: Double?,
    val userRatingsTotal: Int?,
    val address: String?,
    val openingHours: List<String>,
    val isOpenNow: Boolean?,
    // Rich fields populated from Places API — used in detail screen without a second call
    val placeId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val website: String? = null,
    val phoneNumber: String? = null
)
