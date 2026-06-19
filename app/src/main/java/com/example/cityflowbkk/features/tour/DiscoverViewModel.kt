package com.example.cityflowbkk.features.tour

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cityflowbkk.BuildConfig
import com.example.cityflowbkk.features.tour.data.AttractionUiModel
import com.example.cityflowbkk.features.tour.data.TourPlacesRepository
import com.example.cityflowbkk.data.places.RetrofitClient
import com.example.cityflowbkk.data.places.PlacesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val isLoading: Boolean = true,
    val currentAttraction: AttractionUiModel? = null,
    val isAllExplored: Boolean = false,
    val errorMessage: String? = null,
    val savedCount: Int = 0
)

data class SavedPlacesUiState(
    val places: List<AttractionUiModel> = emptyList()
)

class DiscoverViewModel(application: Application) : AndroidViewModel(application) {
    private val placesService = RetrofitClient.createService()
    private val placesRepository = PlacesRepository(placesService, BuildConfig.GOOGLE_PLACES_API_KEY)
    private val repository = TourPlacesRepository(application, placesRepository)

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private val _savedPlacesState = MutableStateFlow(SavedPlacesUiState())
    val savedPlacesState: StateFlow<SavedPlacesUiState> = _savedPlacesState.asStateFlow()

    private var allAttractions = mutableListOf<AttractionUiModel>()
    private var currentIndex = 0
    private val viewedIds = mutableSetOf<Int>()
    private val savedPlacesList = mutableListOf<AttractionUiModel>()

    init {
        loadAttractions()
    }

    fun retry() {
        loadAttractions()
    }

    private fun loadAttractions() {
        _uiState.value = DiscoverUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val attractions = repository.loadAttractions()
                allAttractions.clear()
                allAttractions.addAll(attractions.shuffled())
                currentIndex = 0
                viewedIds.clear()
                updateCurrentAttraction()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = DiscoverUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load attractions"
                )
            }
        }
    }

    private fun updateCurrentAttraction() {
        if (currentIndex < allAttractions.size) {
            val attraction = allAttractions[currentIndex]
            if (viewedIds.contains(attraction.id)) {
                currentIndex++
                updateCurrentAttraction()
                return
            }
            viewedIds.add(attraction.id)
            _uiState.value = DiscoverUiState(
                isLoading = false,
                currentAttraction = attraction,
                savedCount = savedPlacesList.size
            )
        } else {
            _uiState.value = DiscoverUiState(
                isLoading = false,
                isAllExplored = true,
                savedCount = savedPlacesList.size
            )
        }
    }

    fun onSwipeRight() {
        val attraction = _uiState.value.currentAttraction ?: return
        if (!savedPlacesList.contains(attraction)) {
            savedPlacesList.add(attraction)
            _savedPlacesState.value = SavedPlacesUiState(savedPlacesList.toList())
        }
        advance()
    }

    fun onSwipeLeft() {
        advance()
    }

    private fun advance() {
        currentIndex++
        updateCurrentAttraction()
    }

    fun removeSavedPlace(id: Int) {
        savedPlacesList.removeAll { it.id == id }
        _savedPlacesState.value = SavedPlacesUiState(savedPlacesList.toList())
        _uiState.value = _uiState.value.copy(savedCount = savedPlacesList.size)
    }

    fun getSavedPlaceById(id: Int): AttractionUiModel? =
        savedPlacesList.firstOrNull { it.id == id }
}
