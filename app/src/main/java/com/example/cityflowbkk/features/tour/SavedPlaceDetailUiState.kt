package com.example.cityflowbkk.features.tour

import androidx.compose.runtime.Immutable

@Immutable
data class SavedPlaceDetailUiState(
    val isLoading: Boolean = false,
    // Seeded instantly from AttractionUiModel cache
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val rating: Double? = null,
    val userRatingsTotal: Int? = null,
    val address: String? = null,
    val isOpenNow: Boolean? = null,
    val openingHours: List<String> = emptyList(),
    // Rich fields — available immediately if AttractionUiModel was loaded from Places API
    val placeId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val website: String? = null,
    val phoneNumber: String? = null,
    val errorMessage: String? = null
)
