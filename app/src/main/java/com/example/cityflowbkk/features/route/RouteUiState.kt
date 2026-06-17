package com.example.cityflowbkk.features.route

import com.example.cityflowbkk.features.map.BANGKOK_LATITUDE
import com.example.cityflowbkk.features.map.BANGKOK_LONGITUDE
import com.example.cityflowbkk.features.map.MapPlaceUiModel
import com.example.cityflowbkk.features.map.MapLatLng
import com.example.cityflowbkk.features.map.NavigationStepUiModel
import com.example.cityflowbkk.features.map.PlaceSuggestionUiModel
import com.example.cityflowbkk.features.map.RouteUiModel

data class RouteUiState(
    val destination: String = "",
    val destinationSuggestions: List<PlaceSuggestionUiModel> = emptyList(),
    val selectedDestination: MapPlaceUiModel? = null,
    val isSearchingDestination: Boolean = false,
    val route: RouteUiModel? = null,
    val routePoints: List<MapLatLng> = emptyList(),
    val navigationSteps: List<NavigationStepUiModel> = emptyList(),
    val isCalculatingRoute: Boolean = false,
    val travelRecommendations: List<TravelRecommendationUiModel> = emptyList(),
    val routeGuidanceOptions: List<RouteGuidanceUiModel> = emptyList(),
    val selectedGuidanceMode: TravelMode? = null,
    val routeResult: String = "Route details will appear here after a destination is selected.",
    val travelRecommendation: String = "Recommended travel options will appear here.",
    val currentLocationLatitude: Double? = null,
    val currentLocationLongitude: Double? = null,
    val cameraTargetLatitude: Double = BANGKOK_LATITUDE,
    val cameraTargetLongitude: Double = BANGKOK_LONGITUDE,
    val cameraZoom: Float = 12f,
    val markers: List<RouteMapMarkerUiModel> = listOf(
        RouteMapMarkerUiModel(
            id = "bangkok-center",
            title = "Bangkok",
            snippet = "Route planning starts here",
            latitude = BANGKOK_LATITUDE,
            longitude = BANGKOK_LONGITUDE,
        ),
    ),
    val isMapReady: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val isLocationLoading: Boolean = false,
    val locationMessage: String? = null,
    val searchMessage: String? = null,
    val routeMessage: String? = null,
)

data class RouteMapMarkerUiModel(
    val id: String,
    val title: String,
    val snippet: String?,
    val latitude: Double,
    val longitude: Double,
)

enum class TravelMode(
    val label: String,
    val iconLabel: String,
) {
    Walking("Walking", "🚶"),
    Motorcycle("Motorcycle", "🏍"),
    Car("Car", "🚗"),
    Bts("BTS", "🚆"),
}

data class TravelRecommendationUiModel(
    val mode: TravelMode,
    val durationMinutes: Int,
    val estimatedCostBaht: Int,
    val routeSummary: String,
    val instructions: List<String>,
    val isFastest: Boolean = false,
    val isCheapest: Boolean = false,
)

data class RouteGuidanceUiModel(
    val mode: TravelMode,
    val title: String,
    val summary: String,
    val durationMinutes: Int,
    val estimatedCostBaht: Int,
    val steps: List<RouteGuidanceStepUiModel>,
)

data class RouteGuidanceStepUiModel(
    val title: String,
    val detail: String,
    val durationText: String? = null,
    val distanceText: String? = null,
)
