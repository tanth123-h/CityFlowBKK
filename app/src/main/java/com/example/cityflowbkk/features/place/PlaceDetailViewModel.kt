package com.example.cityflowbkk.features.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cityflowbkk.BuildConfig
import com.example.cityflowbkk.features.home.PopularPlaceUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaceDetailViewModel(
    private val repository: PlaceDetailsRepository = PlaceDetailsRepository(BuildConfig.GOOGLE_MAPS_API_KEY),
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlaceDetailUiState())
    val uiState: StateFlow<PlaceDetailUiState> = _uiState.asStateFlow()

    fun loadPlace(place: PopularPlaceUiModel) {
        if (BuildConfig.GOOGLE_MAPS_API_KEY.isBlank()) {
            _uiState.value = PlaceDetailUiState(
                place = place.toFallbackPlaceDetail(),
                errorMessage = "Missing Google Places API key. Add GOOGLE_MAPS_API_KEY to local.properties.",
            )
            return
        }

        _uiState.value = PlaceDetailUiState(
            isLoading = true,
            place = place.toFallbackPlaceDetail(),
        )
        viewModelScope.launch {
            _uiState.value = try {
                PlaceDetailUiState(place = repository.fetchPlaceDetails(place))
            } catch (exception: Exception) {
                PlaceDetailUiState(
                    place = place.toFallbackPlaceDetail(),
                    errorMessage = exception.message ?: "Could not load place details.",
                )
            }
        }
    }

    fun clear() {
        _uiState.value = PlaceDetailUiState()
    }

    private fun PopularPlaceUiModel.toFallbackPlaceDetail(): PlaceDetailUiModel {
        return PlaceDetailUiModel(
            placeId = placeId ?: "Not loaded from Google Places yet",
            name = name,
            photoUrl = null,
            rating = rating.toDoubleOrNull(),
            userRatingsTotal = null,
            address = "Near $nearestStation, Bangkok",
            isOpenNow = null,
            openingHours = emptyList(),
            website = null,
            phoneNumber = null,
            description = fallbackDescription(),
            latitude = latitude,
            longitude = longitude,
        )
    }

    private fun PopularPlaceUiModel.fallbackDescription(): String {
        return when (name) {
            "ICONSIAM" -> "A riverside shopping, dining, and lifestyle destination on the Chao Phraya River."
            "Siam Paragon" -> "A major Bangkok shopping complex at Siam with dining, luxury retail, cinema, and family attractions."
            "Chatuchak Market" -> "One of Bangkok's largest weekend markets, known for local shops, food, fashion, plants, and souvenirs."
            "Asiatique" -> "A riverfront night market and entertainment area with restaurants, shops, and evening views."
            "Grand Palace" -> "A historic royal complex and one of Bangkok's best-known cultural landmarks."
            else -> "A popular Bangkok destination selected from CityFlowBKK."
        }
    }
}
