package com.example.cityflowbkk.features.place

import androidx.compose.runtime.Immutable

@Immutable
data class PlaceDetailUiState(
    val isLoading: Boolean = false,
    val place: PlaceDetailUiModel? = null,
    val errorMessage: String? = null,
)

@Immutable
data class PlaceDetailUiModel(
    val placeId: String,
    val name: String,
    val photoUrl: String?,
    val rating: Double?,
    val userRatingsTotal: Int?,
    val address: String?,
    val isOpenNow: Boolean?,
    val openingHours: List<String>,
    val website: String?,
    val phoneNumber: String?,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val localImageRes: Int? = null,
)
