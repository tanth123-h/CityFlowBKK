package com.example.cityflowbkk.features.route

import android.app.Application
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cityflowbkk.BuildConfig
import com.example.cityflowbkk.features.map.BANGKOK_LATITUDE
import com.example.cityflowbkk.features.map.BANGKOK_LONGITUDE
import com.example.cityflowbkk.features.map.DirectionsRepository
import com.example.cityflowbkk.features.map.DroppedPinUiModel
import com.example.cityflowbkk.features.map.LocationRepository
import com.example.cityflowbkk.features.map.MapLatLng
import com.example.cityflowbkk.features.map.MapSearchRepository
import com.example.cityflowbkk.features.map.PlaceSuggestionUiModel
import com.example.cityflowbkk.features.map.RouteResult
import com.example.cityflowbkk.features.map.RouteTransportType
import com.example.cityflowbkk.features.map.TravelMode as GoogleTravelMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RouteViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val locationRepository = LocationRepository(application.applicationContext)
    private val searchRepository = MapSearchRepository(BuildConfig.GOOGLE_MAPS_API_KEY)
    private val directionsRepository = DirectionsRepository(BuildConfig.GOOGLE_MAPS_API_KEY)
    private val arrivalAlertSettingsRepository = ArrivalAlertSettingsRepository(appContext)
    private val transitArrivalAlertTracker = TransitArrivalAlertTracker()
    private val transitArrivalNotifier = TransitArrivalNotifier(appContext)

    private val _uiState = MutableStateFlow(
        RouteUiState(
            arrivalAlertsEnabled = arrivalAlertSettingsRepository.isEnabled(),
            alertDistanceThresholdMeters = arrivalAlertSettingsRepository.thresholdMeters(),
        ),
    )
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()

    private var destinationSearchJob: Job? = null
    private var routeJob: Job? = null
    private var droppedPinJob: Job? = null
    private var locationTrackingJob: Job? = null
    private var lastDeviationRecalculationMillis: Long = 0L

    init {
        refreshLocationPermissionState()
    }

    fun onDestinationChange(destination: String) {
        _uiState.update {
            it.copy(
                destination = destination,
                selectedDestination = null,
                droppedPin = null,
                route = null,
                routeSegments = emptyList(),
                overviewPolyline = emptyList(),
                navigationSteps = emptyList(),
                activeNavigationStepIndex = null,
                currentNavigationInstruction = null,
                isNavigating = false,
                isOffRoute = false,
                arrivalAlertStationName = null,
                transitDetails = null,
                routeDetailsId = null,
                routeBounds = null,
                travelRecommendations = emptyList(),
                routeGuidanceOptions = emptyList(),
                selectedGuidanceMode = null,
                routeResult = "Route details will appear here after a destination is selected.",
                travelRecommendation = "Recommended travel options will appear here.",
                routeMessage = null,
                searchMessage = null,
            )
        }
        transitArrivalAlertTracker.reset()
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
                        droppedPin = null,
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

    fun onMapClick(location: MapLatLng) {
        droppedPinJob?.cancel()
        routeJob?.cancel()
        _uiState.update {
            it.copy(
                droppedPin = DroppedPinUiModel(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    placeName = "Dropped pin",
                    address = null,
                    isLoadingDetails = true,
                ),
                selectedDestination = null,
                destination = "Dropped pin",
                destinationSuggestions = emptyList(),
                route = null,
                routeSegments = emptyList(),
                overviewPolyline = emptyList(),
                navigationSteps = emptyList(),
                activeNavigationStepIndex = null,
                currentNavigationInstruction = null,
                isNavigating = false,
                isOffRoute = false,
                arrivalAlertStationName = null,
                transitDetails = null,
                routeDetailsId = null,
                routeResult = "Tap Set as Destination to route to this pin.",
                routeMessage = null,
                searchMessage = null,
            )
        }
        transitArrivalAlertTracker.reset()

        droppedPinJob = viewModelScope.launch {
            try {
                val place = searchRepository.reverseGeocode(location)
                _uiState.update {
                    it.copy(
                        droppedPin = DroppedPinUiModel(
                            latitude = place.latitude,
                            longitude = place.longitude,
                            placeName = place.name,
                            address = place.address,
                            isLoadingDetails = false,
                        ),
                        destination = place.name,
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        droppedPin = it.droppedPin?.copy(isLoadingDetails = false),
                        routeMessage = exception.message ?: "Could not load this location address.",
                    )
                }
            }
        }
    }

    fun setDroppedPinAsDestination() {
        val droppedPin = _uiState.value.droppedPin ?: return
        _uiState.update {
            it.copy(
                selectedDestination = droppedPin.toPlace(),
                destination = droppedPin.placeName,
                routeResult = droppedPin.toPlace().toDestinationResultText(),
                routeMessage = null,
                searchMessage = null,
            )
        }
        calculateRouteIfPossible()
    }

    fun navigateToDroppedPin() {
        setDroppedPinAsDestination()
    }

    fun calculateRouteToDroppedPin() {
        setDroppedPinAsDestination()
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

    fun dismissArrivalAlert() {
        _uiState.update { it.copy(arrivalAlertStationName = null) }
    }

    fun onArrivalAlertsEnabledChange(enabled: Boolean) {
        arrivalAlertSettingsRepository.setEnabled(enabled)
        if (!enabled) {
            transitArrivalAlertTracker.reset()
        }
        _uiState.update { it.copy(arrivalAlertsEnabled = enabled) }
    }

    fun onAlertDistanceThresholdChange(thresholdMeters: Int) {
        val coercedThreshold = thresholdMeters.coerceIn(
            ArrivalAlertSettingsRepository.MIN_THRESHOLD_METERS,
            ArrivalAlertSettingsRepository.MAX_THRESHOLD_METERS,
        )
        arrivalAlertSettingsRepository.setThresholdMeters(coercedThreshold)
        _uiState.update { it.copy(alertDistanceThresholdMeters = coercedThreshold) }
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
                        activeNavigationStepIndex = null,
                        currentNavigationInstruction = null,
                        isNavigating = false,
                        isOffRoute = false,
                        arrivalAlertStationName = null,
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
            val wasNavigating = _uiState.value.isNavigating
            _uiState.update {
                it.copy(
                    isCalculatingRoute = true,
                    arrivalAlertStationName = null,
                    routeMessage = null,
                )
            }

            try {
                // Look up nearest stations for display/fare only — NOT used for routing.
                // Google Directions API handles all BTS/MRT/transfer logic internally.
                val stationPair = StationRepository.findNearestPairWithFallback(origin, dest)
                if (stationPair != null) {
                    Log.d(DIAG, "Nearest stations (display only):" +
                        " origin=${stationPair.originStation.name} (${stationPair.originStation.lineName})" +
                        " %.0fm".format(stationPair.originDistanceMeters) +
                        " | dest=${stationPair.destinationStation.name} (${stationPair.destinationStation.lineName})" +
                        " %.0fm".format(stationPair.destinationDistanceMeters))
                } else {
                    Log.d(DIAG, "No nearby stations within search radius.")
                }

                // Single Google Directions transit request using real origin/destination.
                // Google resolves BTS, MRT, Airport Rail Link, transfers, and walking.
                Log.d(DIAG, "Requesting transit route:" +
                    " origin=(%.6f, %.6f)".format(origin.latitude, origin.longitude) +
                    " dest=(%.6f, %.6f)".format(dest.latitude, dest.longitude))

                val routes = directionsRepository.getRoutes(origin, dest, mode = "transit")
                val transitRoute = routes.selectBestRoute()

                // Log every transit segment so the response is fully visible in logcat
                val foundTransitSegments = transitRoute.segments.filter {
                    it.travelMode == GoogleTravelMode.TRANSIT
                }
                if (foundTransitSegments.isEmpty()) {
                    Log.w(DIAG, "NO transit segments in selected route — route is walking-only.")
                    Log.w(DIAG, "  Google found no BTS/MRT/ARL service for this origin/destination pair.")
                    Log.w(DIAG, "  Total segments: ${transitRoute.segments.size}")
                    transitRoute.segments.forEach { seg ->
                        Log.w(DIAG, "    segment travelMode=${seg.travelMode}" +
                            " transportType=${seg.transportType}" +
                            " dist=${seg.distanceText} dur=${seg.durationText}")
                    }
                } else {
                    Log.d(DIAG, "Transit segments found: ${foundTransitSegments.size}")
                    foundTransitSegments.forEachIndexed { i, seg ->
                        val td = seg.transitDetails
                        if (td != null) {
                            Log.d(DIAG, "  TRANSIT[$i] FOUND:")
                            Log.d(DIAG, "    line.name      : ${td.lineName}")
                            Log.d(DIAG, "    line.short_name: ${td.lineShortName ?: "n/a"}")
                            Log.d(DIAG, "    vehicle.type   : ${td.vehicleType ?: "n/a"}")
                            Log.d(DIAG, "    vehicle.name   : ${td.vehicleName ?: "n/a"}")
                            Log.d(DIAG, "    departure_stop : ${td.departureStop}")
                            Log.d(DIAG, "    arrival_stop   : ${td.arrivalStop}")
                            Log.d(DIAG, "    num_stops      : ${td.numStops}")
                            Log.d(DIAG, "    transportType  : ${seg.transportType.diagColorName()}")
                            Log.d(DIAG, "    agencies       : ${td.agencies}")
                            
                            // IMMEDIATE classification test
                            val detectedType = FareRepository.detectTransitType(td)
                            Log.d(DIAG, "    *** FareRepository classified this as: $detectedType ***")
                        } else {
                            Log.w(DIAG, "  TRANSIT[$i] segment has null transitDetails")
                        }
                    }
                }
                val uiSegments = transitRoute.segments.map { segment ->
                    val segmentType = when (segment.travelMode) {
                        GoogleTravelMode.WALKING -> RouteSegmentType.Walking
                        GoogleTravelMode.TRANSIT -> RouteSegmentType.Transit
                        GoogleTravelMode.DRIVING -> RouteSegmentType.Other
                    }
                    RouteSegmentUiModel(
                        index = segment.index,
                        points = segment.points,
                        color = segment.transportType.routeColor(),
                        segmentType = segmentType,
                        transportType = segment.transportType,
                        instruction = segment.instruction,
                        width = if (segmentType == RouteSegmentType.Transit) 14f else 6f,
                    )
                }

                val transitSegments = transitRoute.segments.filter {
                    it.travelMode == GoogleTravelMode.TRANSIT && it.transitDetails != null
                }
                val firstTransit = transitSegments.firstOrNull()
                val lastTransit = transitSegments.lastOrNull()
                val fareSummary = FareRepository.calculateFareSummary(transitRoute.segments)

                Log.d(DIAG, "=== FARE SUMMARY DEBUG ===")
                Log.d(DIAG, "BTS fare: ${fareSummary.btsFareBaht}")
                Log.d(DIAG, "MRT fare: ${fareSummary.mrtFareBaht}")
                Log.d(DIAG, "BTS origin: ${fareSummary.btsOriginStation}")
                Log.d(DIAG, "BTS destination: ${fareSummary.btsDestinationStation}")
                Log.d(DIAG, "MRT origin: ${fareSummary.mrtOriginStation}")
                Log.d(DIAG, "MRT destination: ${fareSummary.mrtDestinationStation}")

                val transitDetails = if (firstTransit != null && lastTransit != null) {
                    val routeTypeLabel = when {
                        fareSummary.hasBts && fareSummary.hasMrt -> "BTS + MRT"
                        fareSummary.hasBts -> "BTS"
                        fareSummary.hasMrt -> "MRT"
                        else -> "Transit"
                    }
                    val departureStation = firstTransit.transitDetails?.departureStop.orEmpty()
                    val arrivalStation = lastTransit.transitDetails?.arrivalStop.orEmpty()
                    
                    Log.d(DIAG, "=== TRANSIT DETAILS UI MODEL ===")
                    Log.d(DIAG, "departureStation: '$departureStation'")
                    Log.d(DIAG, "arrivalStation: '$arrivalStation'")
                    Log.d(DIAG, "routeTypeLabel: '$routeTypeLabel'")
                    Log.d(DIAG, "hasBts: ${fareSummary.hasBts}, hasMrt: ${fareSummary.hasMrt}")
                    Log.d(DIAG, "btsOriginStation: '${fareSummary.btsOriginStation}'")
                    Log.d(DIAG, "btsDestinationStation: '${fareSummary.btsDestinationStation}'")
                    Log.d(DIAG, "mrtOriginStation: '${fareSummary.mrtOriginStation}'")
                    Log.d(DIAG, "mrtDestinationStation: '${fareSummary.mrtDestinationStation}'")
                    
                    TransitRouteDetailsUiModel(
                        lineName = transitSegments.joinToString(" / ") {
                            it.transitDetails?.lineShortName
                                ?: it.transitDetails?.lineName
                                ?: "Transit"
                        },
                        routeType = routeTypeLabel,
                        departureStation = departureStation,
                        arrivalStation = arrivalStation,
                        stationCount = transitSegments.sumOf { it.transitDetails?.numStops ?: 0 },
                        durationText = transitSegments.sumOf { it.durationSeconds }
                            .toDurationText()
                            .ifBlank { transitRoute.route.durationText },
                        distanceText = transitSegments.sumOf { it.distanceMeters }.toDistanceText(),
                        btsFareText = fareSummary.btsFareBaht.toFareAmountText(),
                        mrtFareText = fareSummary.mrtFareBaht.toFareAmountText(),
                        totalTransitFareText = fareSummary.totalFareBaht.toFareAmountText(),
                        hasBts = fareSummary.hasBts,
                        hasMrt = fareSummary.hasMrt,
                        btsOriginStation = fareSummary.btsOriginStation,
                        btsDestinationStation = fareSummary.btsDestinationStation,
                        mrtOriginStation = fareSummary.mrtOriginStation,
                        mrtDestinationStation = fareSummary.mrtDestinationStation,
                    )
                } else {
                    null
                }
                val routeDetailsId = RouteDetailsStore.put(
                    RouteDetailsPayload(
                        destinationName = destination.name,
                        destinationAddress = destination.address,
                        routeResult = transitRoute,
                        nearestOriginStationName = stationPair?.originStation?.name,
                        nearestDestinationStationName = stationPair?.destinationStation?.name,
                        btsFareText = transitDetails?.btsFareText,
                        mrtFareText = transitDetails?.mrtFareText,
                        totalFareText = transitDetails?.totalTransitFareText,
                    ),
                )

                _uiState.update {
                    it.copy(
                        route = transitRoute.route,
                        routeSegments = uiSegments,
                        overviewPolyline = transitRoute.points,
                        navigationSteps = transitRoute.steps,
                        activeNavigationStepIndex = transitRoute.steps.firstOrNull()?.index,
                        currentNavigationInstruction = transitRoute.steps.firstOrNull()?.instruction,
                        isNavigating = wasNavigating,
                        isOffRoute = false,
                        transitDetails = transitDetails,
                        routeDetailsId = routeDetailsId,
                        isCalculatingRoute = false,
                        routeBounds = computeRouteBounds(transitRoute.points),
                        routeResult = transitRoute.toRouteResultText(transitDetails),
                        routeMessage = null,
                    )
                }
                if (wasNavigating) {
                    val firstStep = transitRoute.steps.firstOrNull()
                    val nextStep = transitRoute.steps.getOrNull(1)
                    _uiState.update {
                        it.copy(
                            activeNavigationStepIndex = firstStep?.index,
                            currentNavigationInstruction = firstStep?.instruction,
                            nextNavigationInstruction = nextStep?.instruction,
                            remainingDistanceText = transitRoute.route.distanceText,
                            remainingTimeText = transitRoute.route.durationText,
                            estimatedArrivalTimeText = transitRoute.route.arrivalTimeText,
                            hasArrivedAtDestination = false,
                        )
                    }
                    transitArrivalAlertTracker.reset()
                    startLocationTracking()
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        route = null,
                        routeSegments = emptyList(),
                        overviewPolyline = emptyList(),
                        navigationSteps = emptyList(),
                        activeNavigationStepIndex = null,
                        currentNavigationInstruction = null,
                        isNavigating = false,
                        isOffRoute = false,
                        arrivalAlertStationName = null,
                        transitDetails = null,
                        routeDetailsId = null,
                        isCalculatingRoute = false,
                        routeBounds = null,
                        routeMessage = exception.message ?: "Could not calculate route.",
                    )
                }
            }
        }
    }

    fun startNavigation() {
        val steps = _uiState.value.navigationSteps
        if (steps.isEmpty()) return

        val firstStep = steps.firstOrNull()
        val nextStep = steps.getOrNull(1)

        _uiState.update {
            it.copy(
                isNavigating = true,
                hasArrivedAtDestination = false,
                activeNavigationStepIndex = firstStep?.index,
                currentNavigationInstruction = firstStep?.instruction,
                nextNavigationInstruction = nextStep?.instruction,
                remainingDistanceText = it.route?.distanceText,
                remainingTimeText = it.route?.durationText,
                estimatedArrivalTimeText = it.route?.arrivalTimeText,
            )
        }
        transitArrivalAlertTracker.reset()
        startLocationTracking()
    }

    fun endNavigation() {
        locationTrackingJob?.cancel()
        locationTrackingJob = null
        _uiState.update {
            it.copy(
                isNavigating = false,
                hasArrivedAtDestination = false,
                activeNavigationStepIndex = null,
                currentNavigationInstruction = null,
                nextNavigationInstruction = null,
                remainingDistanceText = null,
                remainingTimeText = null,
                estimatedArrivalTimeText = null,
                isOffRoute = false,
                routeMessage = null,
            )
        }
        transitArrivalAlertTracker.reset()
    }

    private fun startLocationTracking() {
        if (locationTrackingJob?.isActive == true || !locationRepository.hasLocationPermission()) return

        locationTrackingJob = viewModelScope.launch {
            locationRepository.locationUpdates()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            locationMessage = exception.message ?: "Live location updates stopped.",
                            isNavigating = false,
                        )
                    }
                }
                .collect { location ->
                    onNavigationLocation(location)
                }
        }
    }

    private fun onNavigationLocation(location: MapLatLng) {
        val state = _uiState.value
        val steps = state.navigationSteps
        if (steps.isEmpty()) {
            _uiState.update {
                it.copy(
                    currentLocationLatitude = location.latitude,
                    currentLocationLongitude = location.longitude,
                )
            }
            return
        }

        if (!state.isNavigating) {
            _uiState.update {
                it.copy(
                    currentLocationLatitude = location.latitude,
                    currentLocationLongitude = location.longitude,
                )
            }
            return
        }

        val activeStepIndex = NavigationTracker.activeStepIndex(
            location = location,
            steps = steps,
            previousIndex = state.activeNavigationStepIndex,
        )
        val isOffRoute = NavigationTracker.isOffRoute(location, steps)
        val activeStep = activeStepIndex?.let { steps.getOrNull(it) }
        val nextStep = activeStepIndex?.let { steps.getOrNull(it + 1) }

        var remainingDistanceMeters = 0
        var remainingDurationSeconds = 0

        if (activeStepIndex != null && activeStepIndex in steps.indices) {
            val activeStepEnd = steps[activeStepIndex].endLocation
            val distanceToActiveStepEnd = NavigationTracker.distanceMeters(location, activeStepEnd).toInt()
            
            val activeStepTotalDistance = steps[activeStepIndex].distanceMeters.coerceAtLeast(1)
            val activeStepTotalDuration = steps[activeStepIndex].durationSeconds
            val activeStepRemainingDuration = ((distanceToActiveStepEnd.toDouble() / activeStepTotalDistance) * activeStepTotalDuration).toInt()
            
            remainingDistanceMeters += distanceToActiveStepEnd
            remainingDurationSeconds += activeStepRemainingDuration

            for (i in (activeStepIndex + 1) until steps.size) {
                remainingDistanceMeters += steps[i].distanceMeters
                remainingDurationSeconds += steps[i].durationSeconds
            }
        } else {
            remainingDistanceMeters = state.route?.distanceMeters ?: 0
            remainingDurationSeconds = state.route?.durationSeconds ?: 0
        }

        val remainingDistanceText = remainingDistanceMeters.toDistanceText()
        val remainingTimeText = remainingDurationSeconds.toDurationText()
        val estimatedArrivalTimeText = formatArrivalTime(remainingDurationSeconds)

        val lastStep = steps.lastOrNull()
        var hasArrived = state.hasArrivedAtDestination
        var currentInstruction = activeStep?.instruction

        if (lastStep != null && !hasArrived) {
            val distanceToDest = NavigationTracker.distanceMeters(location, lastStep.endLocation)
            if (distanceToDest <= 20.0) {
                hasArrived = true
                currentInstruction = "Arrived at Destination"
                viewModelScope.launch {
                    transitArrivalNotifier.showDestinationArrival()
                }
            }
        }

        _uiState.update {
            it.copy(
                currentLocationLatitude = location.latitude,
                currentLocationLongitude = location.longitude,
                activeNavigationStepIndex = activeStepIndex,
                currentNavigationInstruction = currentInstruction,
                nextNavigationInstruction = nextStep?.instruction,
                remainingDistanceText = remainingDistanceText,
                remainingTimeText = remainingTimeText,
                estimatedArrivalTimeText = estimatedArrivalTimeText,
                hasArrivedAtDestination = hasArrived,
                isNavigating = true,
                isOffRoute = isOffRoute,
                routeMessage = if (isOffRoute) {
                    "You appear to be off route. Recalculating from your current location..."
                } else if (it.routeMessage?.startsWith("You appear") == true) {
                    null
                } else {
                    it.routeMessage
                },
            )
        }

        if (isOffRoute) {
            recalculateAfterDeviation(location)
        } else if (!hasArrived) {
            maybeShowTransitArrivalAlert(location, activeStepIndex)
        }
    }

    private fun maybeShowTransitArrivalAlert(
        location: MapLatLng,
        activeStepIndex: Int?,
    ) {
        val state = _uiState.value
        if (!state.arrivalAlertsEnabled) return

        val alert = transitArrivalAlertTracker.arrivalAlert(
            location = location,
            steps = state.navigationSteps,
            activeStepIndex = activeStepIndex,
            thresholdMeters = state.alertDistanceThresholdMeters,
        ) ?: return

        _uiState.update { it.copy(arrivalAlertStationName = alert.stationName) }
        transitArrivalNotifier.showArrivalAlert(alert.stationName)
    }

    private fun recalculateAfterDeviation(location: MapLatLng) {
        val now = System.currentTimeMillis()
        if (now - lastDeviationRecalculationMillis < DEVIATION_RECALCULATION_THROTTLE_MS) return
        lastDeviationRecalculationMillis = now

        _uiState.update {
            it.copy(
                currentLocationLatitude = location.latitude,
                currentLocationLongitude = location.longitude,
            )
        }
        calculateRouteIfPossible()
    }

    private fun RouteUiState.currentLocation(): MapLatLng? {
        val latitude = currentLocationLatitude ?: return null
        val longitude = currentLocationLongitude ?: return null
        return MapLatLng(latitude, longitude)
    }

    private fun DroppedPinUiModel.toPlace(): com.example.cityflowbkk.features.map.MapPlaceUiModel {
        return com.example.cityflowbkk.features.map.MapPlaceUiModel(
            placeId = "dropped:$latitude,$longitude",
            name = placeName,
            address = address,
            latitude = latitude,
            longitude = longitude,
        )
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
                append("${it.stationCount} stations - Total fare: ${it.totalTransitFareText}")
            }
        }
    }

    private fun Int?.toFareText(): String {
        return this?.let { "฿$it" } ?: "Unavailable"
    }

    private fun Int?.toFareAmountText(): String {
        return this?.let { "฿$it" } ?: "Unavailable"
    }

    private fun RouteTransportType.routeColor(): Color {
        return when (this) {
            RouteTransportType.WALKING -> Color(0xFF1A73E8)
            RouteTransportType.BTS_SUKHUMVIT -> Color(0xFF8BC34A)
            RouteTransportType.BTS_SILOM -> Color(0xFF0B7D3B)
            RouteTransportType.MRT_BLUE -> Color(0xFF1565C0)
            RouteTransportType.MRT_PURPLE -> Color(0xFF7E57C2)
            RouteTransportType.AIRPORT_RAIL_LINK -> Color(0xFFD32F2F)
            RouteTransportType.BUS -> Color(0xFFFF9800)
            RouteTransportType.DRIVING -> Color(0xFF757575)
            RouteTransportType.UNKNOWN_TRANSIT -> Color(0xFF34A853)
        }
    }

    /** Diagnostic-only: returns a human-readable colour name for logcat output. */
    private fun RouteTransportType.diagColorName(): String = when (this) {
        RouteTransportType.WALKING           -> "BLUE(walking)"
        RouteTransportType.BTS_SUKHUMVIT     -> "LIGHT_GREEN(bts-sukhumvit)"
        RouteTransportType.BTS_SILOM         -> "DARK_GREEN(bts-silom)"
        RouteTransportType.MRT_BLUE          -> "BLUE(mrt-blue)"
        RouteTransportType.MRT_PURPLE        -> "PURPLE(mrt-purple)"
        RouteTransportType.AIRPORT_RAIL_LINK -> "RED(arl)"
        RouteTransportType.BUS               -> "ORANGE(bus)"
        RouteTransportType.DRIVING           -> "GREY(driving)"
        RouteTransportType.UNKNOWN_TRANSIT   -> "GREEN(unknown-transit)"
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
        if (this <= 0) return "0 min"
        val minutes = (this + 59) / 60
        return if (minutes < 60) {
            "$minutes min"
        } else {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            if (remainingMinutes == 0) "$hours hr" else "$hours hr $remainingMinutes min"
        }
    }

    private fun formatArrivalTime(durationSeconds: Int): String {
        val arrivalMillis = System.currentTimeMillis() + durationSeconds * 1000L
        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return formatter.format(java.util.Date(arrivalMillis))
    }

    private fun Int.toDistanceText(): String {
        return if (this >= 1000) {
            String.format("%.1f km", this / 1000.0)
        } else {
            "$this m"
        }
    }

    /**
     * Builds a 3-leg route: walk → transit (station-to-station) → walk.
     *
     * Leg A: walking  origin → originStation
     * Leg B: transit  originStation → destStation  (plain transit; Google picks the rail line)
     * Leg C: walking  destStation → destination
     *
     * Returns null if leg B returns no transit segments (Google couldn't find a rail path
     * between those two station coordinates), so the caller falls back to Google default.
     */
    private suspend fun buildThreeLegRoute(
        origin: MapLatLng,
        destination: MapLatLng,
        originStation: MapLatLng,
        destStation: MapLatLng,
    ): RouteResult? {
        // Run all three legs — leg A and C in parallel, leg B independently
        val legA = directionsRepository.getWalkingRoute(origin, originStation)
        val legB = directionsRepository.getStationTransitRoute(originStation, destStation)
        val legC = directionsRepository.getWalkingRoute(destStation, destination)

        Log.d(DIAG, "  Leg A (walk): ${legA.route.durationText} ${legA.route.distanceText}" +
            " — ${legA.segments.size} segment(s)")
        Log.d(DIAG, "  Leg B (transit): ${legB?.route?.durationText ?: "NULL"}")
        Log.d(DIAG, "  Leg C (walk): ${legC.route.durationText} ${legC.route.distanceText}" +
            " — ${legC.segments.size} segment(s)")

        if (legB == null) {
            Log.w(DIAG, "  Leg B returned null — no transit route between stations")
            return null
        }

        val legBTransitSegments = legB.segments.filter { it.travelMode == GoogleTravelMode.TRANSIT }
        if (legBTransitSegments.isEmpty()) {
            Log.w(DIAG, "  Leg B has no TRANSIT segments — Google returned walking between stations")
            return null
        }

        legBTransitSegments.forEach { seg ->
            val td = seg.transitDetails
            if (td != null) {
                Log.d(DIAG, "  Leg B rail: ${td.lineName} | ${td.vehicleType ?: "?"}" +
                    " | ${td.departureStop}→${td.arrivalStop} | ${td.numStops} stops")
            }
        }

        // Re-index all segments sequentially across the three legs
        var idx = 0
        val allSegments = (legA.segments + legB.segments + legC.segments)
            .map { it.copy(index = idx++) }
        val allSteps = (legA.steps + legB.steps + legC.steps)
            .mapIndexed { i, step -> step.copy(index = i) }
        val allPoints = legA.points + legB.points + legC.points

        if (allSegments.isEmpty()) return null

        val totalMeters  = legA.route.distanceMeters + legB.route.distanceMeters + legC.route.distanceMeters
        val totalSeconds = legA.route.durationSeconds + legB.route.durationSeconds + legC.route.durationSeconds

        return com.example.cityflowbkk.features.map.RouteResult(
            route = com.example.cityflowbkk.features.map.RouteUiModel(
                distanceText    = totalMeters.toDistanceText(),
                distanceMeters  = totalMeters,
                durationText    = totalSeconds.toDurationText(),
                durationSeconds = totalSeconds,
                arrivalTimeText = formatArrivalTime(totalSeconds),
            ),
            points   = allPoints,
            segments = allSegments,
            steps    = allSteps,
            fareText     = legB.fareText,
            fareCurrency = legB.fareCurrency,
            fareValue    = legB.fareValue,
        )
    }

    /**
     * Computes an axis-aligned bounding box from all polyline points with 10% padding.
     * Returns null if the point list is empty or degenerate (single point).
     */
    private fun computeRouteBounds(points: List<com.example.cityflowbkk.features.map.MapLatLng>): RouteBounds? {
        if (points.size < 2) return null
        val minLat = points.minOf { it.latitude }
        val maxLat = points.maxOf { it.latitude }
        val minLng = points.minOf { it.longitude }
        val maxLng = points.maxOf { it.longitude }
        // Add 10% padding on each axis so markers are not clipped by sheet or search bar
        val latPad = (maxLat - minLat) * 0.10
        val lngPad = (maxLng - minLng) * 0.10
        return RouteBounds(
            swLat = minLat - latPad,
            swLng = minLng - lngPad,
            neLat = maxLat + latPad,
            neLng = maxLng + lngPad,
        )
    }

    /**
     * Selects the best route from a list of Google Directions alternatives.
     *
     * Only walk + BTS/MRT/ARL routes are considered. Bus and driving are excluded.
     *
     * Priority:
     *  1. Routes containing at least one rail segment (BTS, MRT, Airport Rail Link).
     *     Among these, prefer fewest transit legs (fewer transfers), then shortest duration.
     *  2. Walk-only routes — used when the destination has no nearby rail station.
     *  3. First result as last resort (should not normally be reached after stripping).
     */
    private fun List<RouteResult>.selectBestRoute(): RouteResult {
        val railTypes = setOf(
            RouteTransportType.BTS_SUKHUMVIT,
            RouteTransportType.BTS_SILOM,
            RouteTransportType.MRT_BLUE,
            RouteTransportType.MRT_PURPLE,
            RouteTransportType.AIRPORT_RAIL_LINK,
        )

        // Exclude any route that still contains bus or driving segments
        val cleanRoutes = filter { route ->
            route.segments.none {
                it.transportType == RouteTransportType.BUS ||
                it.transportType == RouteTransportType.DRIVING
            }
        }

        val railRoutes = cleanRoutes.filter { route ->
            route.segments.any { it.transportType in railTypes }
        }

        return if (railRoutes.isNotEmpty()) {
            railRoutes.minWith(
                compareBy(
                    { route -> route.segments.count { it.travelMode == GoogleTravelMode.TRANSIT } },
                    { route -> route.route.durationSeconds },
                ),
            )
        } else {
            // No rail available — return the shortest clean (walk-only) route
            cleanRoutes.minByOrNull { it.route.durationSeconds } ?: first()
        }
    }

    companion object {
        private const val DIAG = "CityFlowDiag"
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val DEVIATION_RECALCULATION_THROTTLE_MS = 30_000L
    }
}
