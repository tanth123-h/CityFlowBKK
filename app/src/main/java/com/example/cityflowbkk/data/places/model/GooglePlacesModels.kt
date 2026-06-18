package com.example.cityflowbkk.data.places.model

data class PlaceSearchRequest(
    val textQuery: String,
    val maxResultCount: Int = 1
)

data class PlaceSearchResponse(
    val places: List<GooglePlace>?
)

data class GooglePlace(
    val id: String?,
    val displayName: DisplayName?,
    val formattedAddress: String?,
    val rating: Double?,
    val userRatingCount: Int?,
    val regularOpeningHours: RegularOpeningHours?,
    val photos: List<PlacePhoto>?
)

data class DisplayName(
    val text: String?,
    val languageCode: String?
)

data class RegularOpeningHours(
    val openNow: Boolean?,
    val weekdayDescriptions: List<String>?
)

data class PlacePhoto(
    val name: String?,
    val widthPx: Int?,
    val heightPx: Int?,
    val authorAttributions: List<AuthorAttribution>?
)

data class AuthorAttribution(
    val displayName: String?,
    val uri: String?,
    val photoUri: String?
)
