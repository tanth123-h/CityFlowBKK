package com.example.cityflowbkk.features.tour

import androidx.compose.runtime.Immutable

@Immutable
data class SavedPlaceDetailUiState(
    val isLoading: Boolean = false,
    // name / photo / rating seeded instantly from AttractionUiModel cache
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val rating: Double? = null,
    val userRatingsTotal: Int? = null,
    val address: String? = null,
    val isOpenNow: Boolean? = null,
    val openingHours: List<String> = emptyList(),
    // enriched by Place Details API
    val phoneNumber: String? = null,
    val website: String? = null,
    val placeId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val errorMessage: String? = null
)
