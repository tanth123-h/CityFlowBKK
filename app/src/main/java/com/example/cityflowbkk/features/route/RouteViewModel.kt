package com.example.cityflowbkk.features.route

import android.app.Application
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
    private val btsTravelRepository = BtsTravelRepository()

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
                routePoints = emptyList(),
                navigationSteps = emptyList(),
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
                val suggestions = searchRepository.autocomplete(destination)
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
                    locationMessage = null,
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
                        locationMessage = null,
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
        val currentLatitude = state.currentLocationLatitude
        val currentLongitude = state.currentLocationLongitude
        val destination = state.selectedDestination

        if (destination == null) return

        if (currentLatitude == null || currentLongitude == null) {
            _uiState.update {
                it.copy(
                    route = null,
                    routePoints = emptyList(),
                    navigationSteps = emptyList(),
                    travelRecommendations = emptyList(),
                    routeGuidanceOptions = emptyList(),
                    selectedGuidanceMode = null,
                    isCalculatingRoute = false,
                    routeResult = destination.toDestinationResultText(),
                    travelRecommendation = "Recommended travel options will appear here.",
                    routeMessage = null,
                )
            }
            return
        }

        routeJob?.cancel()
        routeJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCalculatingRoute = true,
                    routeMessage = null,
                )
            }

            try {
                val result = directionsRepository.getRoute(
                    origin = MapLatLng(currentLatitude, currentLongitude),
                    destination = MapLatLng(destination.latitude, destination.longitude),
                )
                val recommendations = buildTravelRecommendations(
                    origin = MapLatLng(currentLatitude, currentLongitude),
                    destination = MapLatLng(destination.latitude, destination.longitude),
                    drivingDistanceMeters = result.route.distanceMeters,
                    drivingDurationSeconds = result.route.durationSeconds,
                )
                val guidanceOptions = buildGuidanceOptions(
                    recommendations = recommendations,
                    navigationSteps = result.steps,
                )
                _uiState.update {
                    it.copy(
                        route = result.route,
                        routePoints = result.points,
                        navigationSteps = result.steps,
                        travelRecommendations = recommendations,
                        routeGuidanceOptions = guidanceOptions,
                        selectedGuidanceMode = recommendations.firstOrNull { recommendation ->
                            recommendation.isFastest
                        }?.mode ?: TravelMode.Car,
                        isCalculatingRoute = false,
                        routeResult = "${result.route.distanceText} - ${result.route.durationText}",
                        travelRecommendation = recommendations.joinToString(separator = "\n") { recommendation ->
                            "${recommendation.mode.label}: ${recommendation.durationMinutes} min, ${recommendation.estimatedCostBaht} baht"
                        },
                        routeMessage = null,
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        route = null,
                        routePoints = emptyList(),
                        navigationSteps = emptyList(),
                        travelRecommendations = emptyList(),
                        routeGuidanceOptions = emptyList(),
                        selectedGuidanceMode = null,
                        isCalculatingRoute = false,
                        routeMessage = exception.message ?: "Could not calculate route.",
                    )
                }
            }
        }
    }

    fun onGuidanceModeSelected(mode: TravelMode) {
        _uiState.update { it.copy(selectedGuidanceMode = mode) }
    }

    private fun buildTravelRecommendations(
        origin: MapLatLng,
        destination: MapLatLng,
        drivingDistanceMeters: Int,
        drivingDurationSeconds: Int,
    ): List<TravelRecommendationUiModel> {
        val distanceKm = drivingDistanceMeters / 1000.0
        val walking = TravelRecommendationUiModel(
            mode = TravelMode.Walking,
            durationMinutes = (drivingDistanceMeters / WALKING_METERS_PER_MINUTE).toInt().coerceAtLeast(1),
            estimatedCostBaht = 0,
            routeSummary = "Walk ${formatDistance(drivingDistanceMeters)} to the destination.",
            instructions = listOf(
                "Follow pedestrian-friendly streets where available.",
                "Use crossings and station walkways where possible.",
            ),
        )
        val motorcycle = TravelRecommendationUiModel(
            mode = TravelMode.Motorcycle,
            durationMinutes = (distanceKm / MOTORCYCLE_KM_PER_HOUR * 60).toInt().coerceAtLeast(3),
            estimatedCostBaht = (20 + distanceKm * 9).toInt().coerceAtLeast(25),
            routeSummary = "Fast point-to-point ride for short urban trips.",
            instructions = listOf(
                "Book a motorcycle taxi or ride-hailing motorcycle.",
                "Confirm helmet availability and pickup point before departure.",
            ),
        )
        val car = TravelRecommendationUiModel(
            mode = TravelMode.Car,
            durationMinutes = (drivingDurationSeconds / 60.0).toInt().coerceAtLeast(1),
            estimatedCostBaht = (45 + distanceKm * 12).toInt().coerceAtLeast(45),
            routeSummary = "Drive ${formatDistance(drivingDistanceMeters)} using the calculated road route.",
            instructions = listOf(
                "Follow the map route from your current location.",
                "Allow extra time for parking and traffic near the destination.",
            ),
        )
        val bts = btsTravelRepository.buildRecommendation(origin, destination)
        val recommendations = listOf(walking, motorcycle, car, bts)
        val fastestMinutes = recommendations.minOf { it.durationMinutes }
        val cheapestCost = recommendations.minOf { it.estimatedCostBaht }
        return recommendations.map {
            it.copy(
                isFastest = it.durationMinutes == fastestMinutes,
                isCheapest = it.estimatedCostBaht == cheapestCost,
            )
        }
    }

    private fun buildGuidanceOptions(
        recommendations: List<TravelRecommendationUiModel>,
        navigationSteps: List<com.example.cityflowbkk.features.map.NavigationStepUiModel>,
    ): List<RouteGuidanceUiModel> {
        return recommendations.map { recommendation ->
            val steps = when (recommendation.mode) {
                TravelMode.Walking -> navigationSteps.mapIndexed { index, step ->
                    RouteGuidanceStepUiModel(
                        title = "Walk step ${index + 1}",
                        detail = step.instruction.toWalkingInstruction(),
                        durationText = step.durationText,
                        distanceText = step.distanceText,
                    )
                }.ifEmpty {
                    recommendation.instructions.mapIndexed { index, instruction ->
                        RouteGuidanceStepUiModel(
                            title = "Walk step ${index + 1}",
                            detail = instruction,
                        )
                    }
                }

                TravelMode.Car -> navigationSteps.mapIndexed { index, step ->
                    RouteGuidanceStepUiModel(
                        title = "Drive step ${index + 1}",
                        detail = step.instruction,
                        durationText = step.durationText,
                        distanceText = step.distanceText,
                    )
                }

                TravelMode.Bts -> recommendation.instructions.mapIndexed { index, instruction ->
                    RouteGuidanceStepUiModel(
                        title = when (index) {
                            0 -> "Walk to station"
                            1 -> "Board train"
                            2 -> "Ride"
                            3 -> "Exit station"
                            else -> "Walk to destination"
                        },
                        detail = instruction,
                    )
                }

                TravelMode.Motorcycle -> recommendation.instructions.mapIndexed { index, instruction ->
                    RouteGuidanceStepUiModel(
                        title = "Motorcycle step ${index + 1}",
                        detail = instruction,
                    )
                }
            }

            RouteGuidanceUiModel(
                mode = recommendation.mode,
                title = "${recommendation.mode.label} Guidance",
                summary = recommendation.routeSummary,
                durationMinutes = recommendation.durationMinutes,
                estimatedCostBaht = recommendation.estimatedCostBaht,
                steps = steps,
            )
        }
    }

    private fun String.toWalkingInstruction(): String {
        val lowered = lowercase()
        return if (lowered.startsWith("walk") || lowered.startsWith("turn") || lowered.startsWith("head")) {
            this
        } else {
            "Walk and $this"
        }
    }

    private fun formatDistance(distanceMeters: Int): String {
        return if (distanceMeters >= 1000) {
            "${"%.1f".format(distanceMeters / 1000.0)} km"
        } else {
            "$distanceMeters m"
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

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val WALKING_METERS_PER_MINUTE = 75.0
        private const val MOTORCYCLE_KM_PER_HOUR = 24.0
    }
}
