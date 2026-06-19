package com.example.cityflowbkk.features.stationmapping

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cityflowbkk.features.stationmapping.data.BtsStationRepository
import com.example.cityflowbkk.features.stationmapping.data.StationMappingRepository
import com.example.cityflowbkk.features.stationmapping.model.BtsStation
import com.example.cityflowbkk.features.stationmapping.model.StationCoordinate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StationMappingUiState(
    val allStations: List<BtsStation> = emptyList(),
    val mappedCoordinates: List<StationCoordinate> = emptyList(),
    val selectedStation: BtsStation? = null,
    val pendingTap: Pair<Float, Float>? = null,   // normalised x,y waiting to confirm
    val editingCoordinate: StationCoordinate? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val exportPath: String? = null,
    val errorMessage: String? = null,
) {
    val filteredStations: List<BtsStation>
        get() = if (searchQuery.isBlank()) allStations
        else allStations.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.line.displayName.contains(searchQuery, ignoreCase = true)
        }

    val mappedStationIds: Set<String>
        get() = mappedCoordinates.map { it.stationId }.toSet()
}

class StationMappingViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = StationMappingRepository(application)

    private val _uiState = MutableStateFlow(StationMappingUiState())
    val uiState: StateFlow<StationMappingUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(allStations = BtsStationRepository.getAllStations()) }
        viewModelScope.launch { loadSaved() }
    }

    private suspend fun loadSaved() {
        _uiState.update { it.copy(isLoading = true) }
        val saved = repo.loadCoordinates()
        _uiState.update { it.copy(mappedCoordinates = saved, isLoading = false) }
    }

    fun onStationSelected(station: BtsStation) {
        _uiState.update { it.copy(selectedStation = station, pendingTap = null) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /** Called when user taps on the map image (normalised coords 0–1) */
    fun onMapTapped(x: Float, y: Float) {
        val station = _uiState.value.selectedStation ?: return
        _uiState.update { it.copy(pendingTap = Pair(x, y)) }
        confirmPlacement(station, x, y)
    }

    /** Called when user taps an existing marker to start editing */
    fun onMarkerTapped(coordinate: StationCoordinate) {
        val station = _uiState.value.allStations.firstOrNull { it.id == coordinate.stationId }
        _uiState.update {
            it.copy(
                editingCoordinate = coordinate,
                selectedStation = station,
            )
        }
    }

    /** Confirm placing/moving a marker for the given station */
    fun confirmPlacement(station: BtsStation, x: Float, y: Float) {
        val coord = StationCoordinate(
            stationId = station.id,
            stationName = station.name,
            line = station.line,
            x = x,
            y = y,
        )
        _uiState.update { state ->
            val updated = state.mappedCoordinates.toMutableList()
            val idx = updated.indexOfFirst { it.stationId == coord.stationId }
            if (idx >= 0) updated[idx] = coord else updated.add(coord)
            state.copy(
                mappedCoordinates = updated,
                pendingTap = null,
                editingCoordinate = null,
                selectedStation = null,
            )
        }
        viewModelScope.launch { repo.addOrUpdateCoordinate(coord) }
    }

    /** Move an existing marker to a new tap location */
    fun onEditingMarkerMoved(x: Float, y: Float) {
        val editing = _uiState.value.editingCoordinate ?: return
        val station = _uiState.value.allStations.firstOrNull { it.id == editing.stationId } ?: return
        confirmPlacement(station, x, y)
    }

    fun deleteMarker(stationId: String) {
        _uiState.update { state ->
            state.copy(
                mappedCoordinates = state.mappedCoordinates.filter { it.stationId != stationId },
                editingCoordinate = null,
                selectedStation = null,
            )
        }
        viewModelScope.launch { repo.deleteCoordinate(stationId) }
    }

    fun dismissEditing() {
        _uiState.update { it.copy(editingCoordinate = null) }
    }

    fun exportCoordinates() {
        viewModelScope.launch {
            val path = repo.exportCoordinates(_uiState.value.mappedCoordinates)
            _uiState.update {
                it.copy(
                    exportPath = path,
                    errorMessage = if (path == null) "Export failed" else null,
                )
            }
        }
    }

    fun clearExportMessage() {
        _uiState.update { it.copy(exportPath = null, errorMessage = null) }
    }
}
