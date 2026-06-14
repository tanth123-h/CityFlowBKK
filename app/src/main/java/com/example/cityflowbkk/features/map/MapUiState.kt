package com.example.cityflowbkk.features.map

import androidx.compose.runtime.Immutable

@Immutable
data class MapUiState(
    val searchQuery: String = "",
    val suggestions: List<PlaceSuggestionUiModel> = emptyList(),
    val isSearching: Boolean = false,
    val selectedPlace: MapPlaceUiModel? = null,
    val currentLocation: MapLatLng? = null,
    val route: RouteUiModel? = null,
    val routePoints: List<MapLatLng> = emptyList(),
    val isLoadingRoute: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val isLocationLoading: Boolean = false,
    val errorMessage: String? = null,
    val cameraTarget: MapLatLng = MapLatLng(BANGKOK_LATITUDE, BANGKOK_LONGITUDE),
    val cameraZoom: Float = 12f,
    val isMapReady: Boolean = false,
)

@Immutable
data class MapLatLng(
    val latitude: Double,
    val longitude: Double,
)

@Immutable
data class PlaceSuggestionUiModel(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String?,
)

@Immutable
data class MapPlaceUiModel(
    val placeId: String,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
)

@Immutable
data class RouteUiModel(
    val distanceText: String,
    val distanceMeters: Int,
    val durationText: String,
    val durationSeconds: Int,
    val arrivalTimeText: String,
)

const val BANGKOK_LATITUDE = 13.7563
const val BANGKOK_LONGITUDE = 100.5018
