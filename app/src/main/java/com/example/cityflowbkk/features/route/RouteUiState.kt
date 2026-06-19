package com.example.cityflowbkk.features.route

import com.example.cityflowbkk.features.map.BANGKOK_LATITUDE
import com.example.cityflowbkk.features.map.BANGKOK_LONGITUDE
import com.example.cityflowbkk.features.map.DroppedPinUiModel
import com.example.cityflowbkk.features.map.MapPlaceUiModel
import com.example.cityflowbkk.features.map.MapLatLng
import com.example.cityflowbkk.features.map.NavigationStepUiModel
import com.example.cityflowbkk.features.map.PlaceSuggestionUiModel
import com.example.cityflowbkk.features.map.RouteTransportType
import com.example.cityflowbkk.features.map.RouteUiModel
import androidx.compose.ui.graphics.Color

data class RouteUiState(
    val destination: String = "",
    val destinationSuggestions: List<PlaceSuggestionUiModel> = emptyList(),
    val selectedDestination: MapPlaceUiModel? = null,
    val droppedPin: DroppedPinUiModel? = null,
    val isSearchingDestination: Boolean = false,
    val route: RouteUiModel? = null,
    val routeSegments: List<RouteSegmentUiModel> = emptyList(),
    val overviewPolyline: List<MapLatLng> = emptyList(),
    val navigationSteps: List<NavigationStepUiModel> = emptyList(),
    val activeNavigationStepIndex: Int? = null,
    val currentNavigationInstruction: String? = null,
    val nextNavigationInstruction: String? = null,
    val remainingDistanceText: String? = null,
    val remainingTimeText: String? = null,
    val estimatedArrivalTimeText: String? = null,
    val hasArrivedAtDestination: Boolean = false,
    val isNavigating: Boolean = false,
    val isOffRoute: Boolean = false,
    val arrivalAlertsEnabled: Boolean = ArrivalAlertSettingsRepository.DEFAULT_ENABLED,
    val alertDistanceThresholdMeters: Int = ArrivalAlertSettingsRepository.DEFAULT_THRESHOLD_METERS,
    val arrivalAlertStationName: String? = null,
    val transitDetails: TransitRouteDetailsUiModel? = null,
    val routeDetailsId: String? = null,
    val isCalculatingRoute: Boolean = false,
    val routeBounds: RouteBounds? = null,
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

data class RouteSegmentUiModel(
    val index: Int,
    val points: List<MapLatLng>,
    val color: Color,
    val segmentType: RouteSegmentType,
    val transportType: RouteTransportType,
    val instruction: String,
    val width: Float,
)

enum class RouteSegmentType {
    Walking,
    Transit,
    Other,
}

data class TransitRouteDetailsUiModel(
    val lineName: String,
    val departureStation: String,
    val arrivalStation: String,
    val stationCount: Int,
    val durationText: String,
    val distanceText: String,
    val btsFareText: String,
    val mrtFareText: String,
    val totalTransitFareText: String,
    val btsOriginStation: String? = null,
    val btsDestinationStation: String? = null,
    val mrtOriginStation: String? = null,
    val mrtDestinationStation: String? = null,
)

data class RouteMapMarkerUiModel(
    val id: String,
    val title: String,
    val snippet: String?,
    val latitude: Double,
    val longitude: Double,
)

/**
 * Axis-aligned bounding box covering the full route polyline.
 * Used to fit the map camera so the entire route is visible.
 */
data class RouteBounds(
    val swLat: Double,
    val swLng: Double,
    val neLat: Double,
    val neLng: Double,
)

enum class TravelMode(
    val label: String,
    val iconLabel: String,
) {
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
