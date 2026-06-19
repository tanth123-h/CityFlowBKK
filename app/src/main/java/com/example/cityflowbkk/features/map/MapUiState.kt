package com.example.cityflowbkk.features.map

import androidx.compose.runtime.Immutable

@Immutable
data class MapUiState(
    val searchQuery: String = "",
    val suggestions: List<PlaceSuggestionUiModel> = emptyList(),
    val isSearching: Boolean = false,
    val selectedPlace: MapPlaceUiModel? = null,
    val droppedPin: DroppedPinUiModel? = null,
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
    
    // Station Coordinate Collection
    val tappedMarkers: List<TappedMarker> = emptyList(),
    val lastTapX: Float? = null,
    val lastTapY: Float? = null,
    
    // Map Zoom
    val mapZoom: Float = 1f,  // 1.0x = no zoom, 5.0x = max zoom
)

@Immutable
data class TappedMarker(
    val id: String,
    val x: Float,  // Normalized 0-1 relative to image
    val y: Float,  // Normalized 0-1 relative to image
)

@Immutable
data class DroppedPinUiModel(
    val latitude: Double,
    val longitude: Double,
    val placeName: String,
    val address: String?,
    val isLoadingDetails: Boolean = false,
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

@Immutable
data class NavigationStepUiModel(
    val index: Int = 0,
    val instruction: String,
    val distanceText: String,
    val distanceMeters: Int,
    val durationText: String,
    val durationSeconds: Int,
    val startLocation: MapLatLng,
    val endLocation: MapLatLng,
    val points: List<MapLatLng> = emptyList(),
    val travelMode: TravelMode = TravelMode.WALKING,
    val transportType: RouteTransportType = RouteTransportType.WALKING,
    val transitDetails: TransitDetails? = null,
)

const val BANGKOK_LATITUDE = 13.7563
const val BANGKOK_LONGITUDE = 100.5018
const val MIN_MAP_ZOOM = 1f
const val MAX_MAP_ZOOM = 5f
const val ZOOM_STEP = 0.2f  // 20% per click
