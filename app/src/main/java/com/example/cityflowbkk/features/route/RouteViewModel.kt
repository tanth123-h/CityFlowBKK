package com.example.cityflowbkk.features.route

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cityflowbkk.BuildConfig
import com.example.cityflowbkk.features.map.BANGKOK_LATITUDE
import com.example.cityflowbkk.features.map.BANGKOK_LONGITUDE
import com.example.cityflowbkk.features.map.DirectionsRepository
import com.example.cityflowbkk.features.map.LocationRepository
import com.example.cityflowbkk.features.map.MapLatLng
import com.example.cityflowbkk.features.map.MapSearchRepository
import com.example.cityflowbkk.features.map.PlaceSuggestionUiModel
import com.example.cityflowbkk.features.map.RouteResult
import com.example.cityflowbkk.features.map.TravelMode as GoogleTravelMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RouteViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val locationRepository = LocationRepository(application.applicationContext)
    private val searchRepository = MapSearchRepository(BuildConfig.GOOGLE_MAPS_API_KEY)
    private val directionsRepository = DirectionsRepository(BuildConfig.GOOGLE_MAPS_API_KEY)

    private val _uiState = MutableStateFlow(RouteUiState())
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()

    private var destinationSearchJob: Job? = null
    private var routeJob: Job? = null

    init {
        refreshLocationPermissionState()
    }

    fun onDestinationChange(destination: String) {
        _uiState.update {
            it.copy(
                destination = destination,
                selectedDestination = null,
                route = null,
                routeSegments = emptyList(),
                overviewPolyline = emptyList(),
                navigationSteps = emptyList(),
                transitDetails = null,
                routeDetailsId = null,
                travelRecommendations = emptyList(),
                routeGuidanceOptions = emptyList(),
                selectedGuidanceMode = null,
                routeResult = "Route details will appear here after a destination is selected.",
                travelRecommendation = "Recommended travel options will appear here.",
                routeMessage = null,
                searchMessage = null,
            )
        }
        destinationSearchJob?.cancel()

        if (destination.isBlank()) {
            _uiState.update {
                it.copy(
                    destinationSuggestions = emptyList(),
                    isSearchingDestination = false,
                    searchMessage = null,
                )
            }
            return
        }

        destinationSearchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _uiState.update { it.copy(isSearchingDestination = true) }
            try {
                val suggestions = searchRepository.autocomplete(destination, _uiState.value.currentLocation())
                _uiState.update {
                    it.copy(
                        destinationSuggestions = suggestions,
                        isSearchingDestination = false,
                        searchMessage = null,
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        destinationSuggestions = emptyList(),
                        isSearchingDestination = false,
                        searchMessage = exception.message ?: "Destination search failed.",
                    )
                }
            }
        }
    }

    fun onDestinationSelected(suggestion: PlaceSuggestionUiModel) {
        destinationSearchJob?.cancel()
        _uiState.update {
            it.copy(
                destination = suggestion.primaryText,
                destinationSuggestions = emptyList(),
                isSearchingDestination = false,
                searchMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                val destination = searchRepository.fetchPlaceDetails(suggestion.placeId)
                _uiState.update {
                    it.copy(
                        selectedDestination = destination,
                        destination = destination.name,
                        cameraTargetLatitude = destination.latitude,
                        cameraTargetLongitude = destination.longitude,
                        cameraZoom = 15f,
                        routeResult = destination.toDestinationResultText(),
                        travelRecommendation = "Recommended travel options will appear here.",
                        searchMessage = null,
                        routeMessage = null,
                    )
                }
                calculateRouteIfPossible()
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        selectedDestination = null,
                        searchMessage = exception.message ?: "Could not load destination details.",
                    )
                }
            }
        }
    }

    fun onMapLoaded() {
        _uiState.update { it.copy(isMapReady = true) }
        if (_uiState.value.hasLocationPermission) {
            loadCurrentLocation(moveCamera = true)
        }
    }

    fun refreshLocationPermissionState() {
        val hasPermission = locationRepository.hasLocationPermission()
        _uiState.update {
            it.copy(
                hasLocationPermission = hasPermission,
                locationMessage = null,
            )
        }

        if (hasPermission && _uiState.value.isMapReady) {
            loadCurrentLocation(moveCamera = true)
        }
    }

    fun loadCurrentLocation(moveCamera: Boolean = true) {
        if (!locationRepository.hasLocationPermission()) {
            _uiState.update {
                it.copy(
                    hasLocationPermission = false,
                    isLocationLoading = false,
                    locationMessage = "Allow location access to plan a route from your current position.",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    hasLocationPermission = true,
                    isLocationLoading = true,
                    locationMessage = null,
                )
            }

            val location = locationRepository.getCurrentLocation()
            _uiState.update {
                if (location == null) {
                    it.copy(
                        isLocationLoading = false,
                        locationMessage = "Could not get your current GPS location. Try again when location is available.",
                    )
                } else {
                    it.copy(
                        currentLocationLatitude = location.latitude,
                        currentLocationLongitude = location.longitude,
                        cameraTargetLatitude = if (moveCamera) location.latitude else it.cameraTargetLatitude,
                        cameraTargetLongitude = if (moveCamera) location.longitude else it.cameraTargetLongitude,
                        cameraZoom = if (moveCamera) 15f else it.cameraZoom,
                        isLocationLoading = false,
                        locationMessage = null,
                    )
                }
            }
            calculateRouteIfPossible()
        }
    }

    fun onMyLocationClick() {
        loadCurrentLocation(moveCamera = true)
    }

    fun recenterCamera() {
        _uiState.update {
            it.copy(
                cameraTargetLatitude = it.currentLocationLatitude ?: BANGKOK_LATITUDE,
                cameraTargetLongitude = it.currentLocationLongitude ?: BANGKOK_LONGITUDE,
                cameraZoom = if (it.currentLocationLatitude != null && it.currentLocationLongitude != null) 15f else 12f,
            )
        }
    }

    fun dismissLocationMessage() {
        _uiState.update { it.copy(locationMessage = null) }
    }

    fun dismissSearchMessage() {
        _uiState.update { it.copy(searchMessage = null) }
    }

    fun dismissRouteMessage() {
        _uiState.update { it.copy(routeMessage = null) }
    }

    private fun calculateRouteIfPossible() {
        val state = _uiState.value
        val destination = state.selectedDestination ?: return
        val origin = state.currentLocation()

        if (origin == null) {
            if (state.hasLocationPermission && !state.isLocationLoading) {
                loadCurrentLocation(moveCamera = false)
            } else {
                _uiState.update {
                    it.copy(
                        route = null,
                        routeSegments = emptyList(),
                        overviewPolyline = emptyList(),
                        navigationSteps = emptyList(),
                        transitDetails = null,
                        routeDetailsId = null,
                        isCalculatingRoute = false,
                        routeMessage = if (state.hasLocationPermission) {
                            "Getting your current location before calculating the route."
                        } else {
                            "Allow location access to plan a route from your current position."
                        },
                    )
                }
            }
            return
        }

        val dest = MapLatLng(destination.latitude, destination.longitude)

        routeJob?.cancel()
        routeJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCalculatingRoute = true,
                    routeMessage = null,
                )
            }

            try {
                val transitRoute = directionsRepository.getRoute(origin, dest, mode = "transit")
                val uiSegments = transitRoute.segments.map { segment ->
                    val segmentType = when (segment.travelMode) {
                        GoogleTravelMode.WALKING -> RouteSegmentType.Walking
                        GoogleTravelMode.TRANSIT -> RouteSegmentType.Transit
                        GoogleTravelMode.DRIVING -> RouteSegmentType.Other
                    }
                    RouteSegmentUiModel(
                        points = segment.points,
                        color = when (segmentType) {
                            RouteSegmentType.Walking -> Color.Blue
                            RouteSegmentType.Transit -> Color.Green
                            RouteSegmentType.Other -> Color.Gray
                        },
                        segmentType = segmentType,
                    )
                }

                val transitSegments = transitRoute.segments.filter {
                    it.travelMode == GoogleTravelMode.TRANSIT && it.transitDetails != null
                }
                val firstTransit = transitSegments.firstOrNull()
                val lastTransit = transitSegments.lastOrNull()
                val transitDetails = if (firstTransit != null && lastTransit != null) {
                    TransitRouteDetailsUiModel(
                        lineName = transitSegments.joinToString(" / ") {
                            it.transitDetails?.lineShortName
                                ?: it.transitDetails?.lineName
                                ?: "Transit"
                        },
                        departureStation = firstTransit.transitDetails?.departureStop.orEmpty(),
                        arrivalStation = lastTransit.transitDetails?.arrivalStop.orEmpty(),
                        stationCount = transitSegments.sumOf { it.transitDetails?.numStops ?: 0 },
                        durationText = transitSegments.sumOf { it.durationSeconds }
                            .toDurationText()
                            .ifBlank { transitRoute.route.durationText },
                        distanceText = transitSegments.sumOf { it.distanceMeters }.toDistanceText(),
                        fareText = transitRoute.fareText ?: "Estimated fare unavailable",
                    )
                } else {
                    null
                }
                val routeDetailsId = RouteDetailsStore.put(
                    RouteDetailsPayload(
                        destinationName = destination.name,
                        destinationAddress = destination.address,
                        routeResult = transitRoute,
                    ),
                )

                _uiState.update {
                    it.copy(
                        route = transitRoute.route,
                        routeSegments = uiSegments,
                        overviewPolyline = transitRoute.points,
                        navigationSteps = transitRoute.steps,
                        transitDetails = transitDetails,
                        routeDetailsId = routeDetailsId,
                        isCalculatingRoute = false,
                        routeResult = transitRoute.toRouteResultText(transitDetails),
                        routeMessage = null,
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        route = null,
                        routeSegments = emptyList(),
                        overviewPolyline = emptyList(),
                        navigationSteps = emptyList(),
                        transitDetails = null,
                        routeDetailsId = null,
                        isCalculatingRoute = false,
                        routeMessage = exception.message ?: "Could not calculate route.",
                    )
                }
            }
        }
    }

    private fun RouteUiState.currentLocation(): MapLatLng? {
        val latitude = currentLocationLatitude ?: return null
        val longitude = currentLocationLongitude ?: return null
        return MapLatLng(latitude, longitude)
    }

    private fun RouteResult.toRouteResultText(
        details: TransitRouteDetailsUiModel?,
    ): String {
        return buildString {
            append("Total: ${route.distanceText} - ${route.durationText}")
            append("\n")
            append("Origin: Current GPS location")
            details?.let {
                append("\n")
                append("${it.departureStation} to ${it.arrivalStation}")
                append("\n")
                append("${it.stationCount} stations - Fare: ${it.fareText}")
            }
        }
    }

    private fun com.example.cityflowbkk.features.map.MapPlaceUiModel.toDestinationResultText(): String {
        return buildString {
            append(name)
            append("\n")
            append("Latitude: ${"%.6f".format(latitude)}")
            append("\n")
            append("Longitude: ${"%.6f".format(longitude)}")
        }
    }

    private fun Int.toDurationText(): String {
        if (this <= 0) return ""
        val minutes = (this + 59) / 60
        return if (minutes < 60) {
            "$minutes min"
        } else {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            if (remainingMinutes == 0) "$hours hr" else "$hours hr $remainingMinutes min"
        }
    }

    private fun Int.toDistanceText(): String {
        return if (this >= 1000) {
            String.format("%.1f km", this / 1000.0)
        } else {
            "$this m"
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}
