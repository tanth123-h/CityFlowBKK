package com.example.cityflowbkk.features.btsmap

import android.app.Application
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cityflowbkk.R
import com.example.cityflowbkk.features.btsmap.data.BTSStationJsonRepository
import com.example.cityflowbkk.features.btsmap.data.FareRepository
import com.example.cityflowbkk.features.btsmap.data.RouteGraphRepository
import com.example.cityflowbkk.features.btsmap.data.RouteRepository
import com.example.cityflowbkk.features.btsmap.model.RouteResult
import com.example.cityflowbkk.features.stationmapping.model.StationCoordinate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BTSMapUiState(
    val stations: List<StationCoordinate> = emptyList(),
    val originStation: StationCoordinate? = null,
    val destinationStation: StationCoordinate? = null,
    val selectedStation: StationCoordinate? = null,
    val isLoading: Boolean = false,
    val isGraphReady: Boolean = false,
    // Computed route between origin and destination
    val routeResult: RouteResult? = null,
    // Actual pixel dimensions of the drawable (read at runtime via BitmapFactory)
    val imgWidth: Int = 0,
    val imgHeight: Int = 0,
    // Original image dimensions used when station coordinates were generated
    // These are set at runtime from actual drawable size
    val originalImgWidth: Float = 0f,
    val originalImgHeight: Float = 0f,
    // Debug overlay toggle
    val debugMode: Boolean = false,
)

class BTSMapViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BTSMapUiState())
    val uiState: StateFlow<BTSMapUiState> = _uiState.asStateFlow()

    private val routeRepository = RouteRepository()

    init {
        // Read actual drawable dimensions at runtime — these are the source of truth
        // for coordinate conversion. stations.json coordinates were generated from
        // this exact image, so we MUST use its real pixel dimensions for alignment.
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(application.resources, R.drawable.btsmap, options)
        
        val actualWidth = options.outWidth
        val actualHeight = options.outHeight
        
        // Debug log to verify image dimensions
        android.util.Log.d("BTSMapViewModel", "=== BTS Map Image Dimensions ===")
        android.util.Log.d("BTSMapViewModel", "Drawable width: $actualWidth px")
        android.util.Log.d("BTSMapViewModel", "Drawable height: $actualHeight px")
        
        if (actualWidth <= 0 || actualHeight <= 0) {
            android.util.Log.e("BTSMapViewModel", "ERROR: Invalid image dimensions! width=$actualWidth, height=$actualHeight")
        }
        
        _uiState.update {
            it.copy(
                imgWidth = actualWidth,
                imgHeight = actualHeight,
                // USE ACTUAL IMAGE DIMENSIONS for coordinate conversion
                originalImgWidth = actualWidth.toFloat(),
                originalImgHeight = actualHeight.toFloat()
            )
        }
        loadStationsAndGraph()
    }

    private fun loadStationsAndGraph() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Wait for image dimensions to be available
            val imgWidth = _uiState.value.originalImgWidth
            val imgHeight = _uiState.value.originalImgHeight
            
            if (imgWidth <= 0f || imgHeight <= 0f) {
                android.util.Log.e("BTSMapViewModel", "Cannot load stations: invalid image dimensions")
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            // Load marker coordinates — pass actual image dimensions for normalization
            val stations = BTSStationJsonRepository.loadStations(
                getApplication(),
                imgWidth,
                imgHeight
            )
            
            android.util.Log.d("BTSMapViewModel", "Loaded ${stations.size} stations")

            // Build the route graph and load fare table in parallel coroutines
            RouteGraphRepository.init(getApplication())
            FareRepository.init(getApplication())

            _uiState.update {
                it.copy(
                    stations    = stations,
                    isLoading   = false,
                    isGraphReady = true
                )
            }
        }
    }

    /**
     * First tap  → Origin (green)
     * Second tap → Destination (red)
     * Tap same as origin → ignored
     * Tap when both set → replace Destination
     * After every change: recompute route if both stations are set.
     */
    fun onStationClicked(station: StationCoordinate) {
        _uiState.update { state ->
            val newState = when {
                state.originStation == null ->
                    state.copy(originStation = station, selectedStation = station)
                state.originStation.stationId == station.stationId ->
                    state
                else ->
                    state.copy(destinationStation = station, selectedStation = station)
            }
            // Recompute route immediately if both ends are defined
            val route = computeRoute(newState.originStation, newState.destinationStation)
            newState.copy(routeResult = route)
        }
    }

    fun clearOrigin() {
        _uiState.update { it.copy(originStation = null, routeResult = null) }
    }

    fun clearDestination() {
        _uiState.update { it.copy(destinationStation = null, routeResult = null) }
    }

    fun swapStations() {
        _uiState.update { state ->
            val newState = state.copy(
                originStation      = state.destinationStation,
                destinationStation = state.originStation,
            )
            val route = computeRoute(newState.originStation, newState.destinationStation)
            newState.copy(routeResult = route)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedStation = null) }
    }

    fun toggleDebugMode() {
        _uiState.update { it.copy(debugMode = !it.debugMode) }
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private fun computeRoute(
        origin: StationCoordinate?,
        destination: StationCoordinate?
    ): RouteResult? {
        if (origin == null || destination == null) return null
        return routeRepository.calculateRoute(origin, destination)
    }
}
