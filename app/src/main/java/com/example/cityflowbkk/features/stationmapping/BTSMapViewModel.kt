package com.example.cityflowbkk.features.stationmapping

import android.app.Application
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cityflowbkk.R
import com.example.cityflowbkk.features.stationmapping.data.BtsStationRepository
import com.example.cityflowbkk.features.stationmapping.data.StationMappingRepository
import com.example.cityflowbkk.features.stationmapping.model.BtsStation
import com.example.cityflowbkk.features.stationmapping.model.StationCoordinate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BTSMapUiState(
    val allStations: List<BtsStation> = emptyList(),
    val mappedCoordinates: List<StationCoordinate> = emptyList(),
    val selectedStation: BtsStation? = null,
    val editingCoordinate: StationCoordinate? = null,
    val isLoading: Boolean = false,
    val exportPath: String? = null,
    val errorMessage: String? = null,
    val imgWidth: Int = 0,
    val imgHeight: Int = 0
)

class BTSMapViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = StationMappingRepository(application)
    private val _uiState = MutableStateFlow(BTSMapUiState())
    val uiState: StateFlow<BTSMapUiState> = _uiState.asStateFlow()

    init {
        // Load image dimensions to calculate absolute pixels later
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(application.resources, R.drawable.btsmap, options)
        
        _uiState.update { 
            it.copy(
                allStations = BtsStationRepository.getAllStations(),
                imgWidth = options.outWidth,
                imgHeight = options.outHeight
            ) 
        }
        viewModelScope.launch { loadSaved() }
    }

    private suspend fun loadSaved() {
        val saved = repo.loadCoordinates()
        _uiState.update { it.copy(mappedCoordinates = saved) }
    }

    fun onStationSelected(station: BtsStation?) {
        _uiState.update { it.copy(selectedStation = station) }
    }

    fun onMapTapped(x: Float, y: Float) {
        val station = _uiState.value.selectedStation ?: return
        saveCoordinate(station, x, y)
    }

    fun onMarkerTapped(coordinate: StationCoordinate) {
        val station = _uiState.value.allStations.firstOrNull { it.id == coordinate.stationId }
        _uiState.update { it.copy(editingCoordinate = coordinate, selectedStation = station) }
    }

    fun deleteMarker(stationId: String) {
        _uiState.update { state ->
            state.copy(mappedCoordinates = state.mappedCoordinates.filter { it.stationId != stationId })
        }
        viewModelScope.launch { repo.deleteCoordinate(stationId) }
    }

    private fun saveCoordinate(station: BtsStation, x: Float, y: Float) {
        val absX = (x * _uiState.value.imgWidth).toInt()
        val absY = (y * _uiState.value.imgHeight).toInt()
        
        val coord = StationCoordinate(
            stationId = station.id,
            stationName = station.name,
            line = station.line,
            x = x,
            y = y,
            absX = absX,
            absY = absY
        )
        
        _uiState.update { state ->
            val updated = state.mappedCoordinates.toMutableList()
            val idx = updated.indexOfFirst { it.stationId == coord.stationId }
            if (idx >= 0) updated[idx] = coord else updated.add(coord)
            state.copy(mappedCoordinates = updated, editingCoordinate = null)
        }
        viewModelScope.launch { repo.addOrUpdateCoordinate(coord) }
    }

    fun export() {
        viewModelScope.launch {
            val path = repo.exportCoordinates(_uiState.value.mappedCoordinates)
            _uiState.update { it.copy(exportPath = path) }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(exportPath = null, errorMessage = null) }
    }
}
