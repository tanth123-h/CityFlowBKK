package com.example.cityflowbkk.features.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cityflowbkk.BuildConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val searchRepository = MapSearchRepository(BuildConfig.GOOGLE_MAPS_API_KEY)
    private val directionsRepository = DirectionsRepository(BuildConfig.GOOGLE_MAPS_API_KEY)
    private val locationRepository = LocationRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var routeJob: Job? = null
    private var droppedPinJob: Job? = null

    init {
        refreshLocationPermissionState()
    }

    fun onMapReady() {
        _uiState.update { it.copy(isMapReady = true) }
        if (_uiState.value.hasLocationPermission) {
            loadCurrentLocation(moveCamera = true)
        }
    }

    fun refreshLocationPermissionState() {
        val hasPermission = locationRepository.hasLocationPermission()
        _uiState.update { it.copy(hasLocationPermission = hasPermission) }
        if (hasPermission && _uiState.value.isMapReady) {
            loadCurrentLocation(moveCamera = _uiState.value.selectedPlace == null)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, errorMessage = null) }
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update { it.copy(suggestions = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _uiState.update { it.copy(isSearching = true) }
            try {
                val suggestions = searchRepository.autocomplete(query)
                _uiState.update {
                    it.copy(suggestions = suggestions, isSearching = false, errorMessage = null)
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        suggestions = emptyList(),
                        isSearching = false,
                        errorMessage = exception.message ?: "Search failed.",
                    )
                }
            }
        }
    }

    fun onSuggestionSelected(suggestion: PlaceSuggestionUiModel) {
        _uiState.update {
            it.copy(
                searchQuery = suggestion.primaryText,
                suggestions = emptyList(),
                isSearching = false,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                val place = searchRepository.fetchPlaceDetails(suggestion.placeId)
                selectDestination(place)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(errorMessage = exception.message ?: "Could not load place details.")
                }
            }
        }
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
                selectedPlace = null,
                route = null,
                routePoints = emptyList(),
                suggestions = emptyList(),
                isSearching = false,
                errorMessage = null,
            )
        }

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
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        droppedPin = it.droppedPin?.copy(isLoadingDetails = false),
                        errorMessage = exception.message ?: "Could not load this location address.",
                    )
                }
            }
        }
    }

    fun setDroppedPinAsDestination() {
        val droppedPin = _uiState.value.droppedPin ?: return
        viewModelScope.launch {
            selectDestination(droppedPin.toPlace())
        }
    }

    fun navigateToDroppedPin() {
        setDroppedPinAsDestination()
    }

    fun calculateRouteToDroppedPin() {
        setDroppedPinAsDestination()
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                searchQuery = "",
                suggestions = emptyList(),
                isSearching = false,
            )
        }
    }

    fun loadCurrentLocation(moveCamera: Boolean = true) {
        if (!locationRepository.hasLocationPermission()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLocationLoading = true) }
            val location = locationRepository.getCurrentLocation()
            _uiState.update {
                it.copy(
                    currentLocation = location,
                    isLocationLoading = false,
                    cameraTarget = if (moveCamera && location != null) location else it.cameraTarget,
                    cameraZoom = if (moveCamera && location != null) 14f else it.cameraZoom,
                )
            }

            if (location != null && _uiState.value.selectedPlace != null) {
                calculateRoute(location, _uiState.value.selectedPlace!!)
            }
        }
    }

    fun onMyLocationClick() {
        if (!_uiState.value.hasLocationPermission) return
        loadCurrentLocation(moveCamera = true)
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun selectDestination(place: MapPlaceUiModel) {
        _uiState.update {
            it.copy(
                selectedPlace = place,
                cameraTarget = MapLatLng(place.latitude, place.longitude),
                cameraZoom = 15f,
                route = null,
                routePoints = emptyList(),
            )
        }

        val origin = _uiState.value.currentLocation ?: locationRepository.getCurrentLocation()?.also { location ->
            _uiState.update { state -> state.copy(currentLocation = location) }
        }

        if (origin != null) {
            calculateRoute(origin, place)
        } else {
            _uiState.update {
                it.copy(errorMessage = "Enable location to calculate a route from your current position.")
            }
        }
    }

    private fun DroppedPinUiModel.toPlace(): MapPlaceUiModel {
        return MapPlaceUiModel(
            placeId = "dropped:$latitude,$longitude",
            name = placeName,
            address = address,
            latitude = latitude,
            longitude = longitude,
        )
    }

    private fun calculateRoute(origin: MapLatLng, destination: MapPlaceUiModel) {
        routeJob?.cancel()
        routeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRoute = true, errorMessage = null) }
            try {
                val result = directionsRepository.getRoute(
                    origin = origin,
                    destination = MapLatLng(destination.latitude, destination.longitude),
                )
                _uiState.update {
                    it.copy(
                        route = result.route,
                        routePoints = result.points,
                        isLoadingRoute = false,
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        route = null,
                        routePoints = emptyList(),
                        isLoadingRoute = false,
                        errorMessage = exception.message ?: "Could not calculate route.",
                    )
                }
            }
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}
