package com.example.cityflowbkk.features.tour

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cityflowbkk.BuildConfig
import com.example.cityflowbkk.data.places.PlacesRepository
import com.example.cityflowbkk.data.places.RetrofitClient
import com.example.cityflowbkk.features.tour.data.AttractionUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SavedPlaceDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = "SavedPlaceDetailVM"

    // Reuse the same Retrofit client/repo that powers the swipe cards
    private val placesRepository = PlacesRepository(
        service = RetrofitClient.createService(),
        apiKey = BuildConfig.GOOGLE_PLACES_API_KEY
    )

    private val _uiState = MutableStateFlow(SavedPlaceDetailUiState())
    val uiState: StateFlow<SavedPlaceDetailUiState> = _uiState.asStateFlow()

    /**
     * Seed instantly from the cached [AttractionUiModel] so the screen is
     * never blank, then fire an enriched fetch in the background.
     *
     * Because [AttractionUiModel] now carries placeId, latitude, longitude,
     * website and phoneNumber (populated during swipe-card loading), the detail
     * screen usually has everything it needs before the background call returns.
     */
    fun load(attraction: AttractionUiModel) {
        // ── Step 1: render immediately from cache ─────────────────────────
        _uiState.value = SavedPlaceDetailUiState(
            isLoading = true,
            name = attraction.name,
            category = attraction.category,
            description = attraction.description,
            photoUrl = attraction.photoUrl,
            rating = attraction.rating,
            userRatingsTotal = attraction.userRatingsTotal,
            address = attraction.address,
            isOpenNow = attraction.isOpenNow,
            openingHours = attraction.openingHours,
            placeId = attraction.placeId,
            latitude = attraction.latitude,
            longitude = attraction.longitude,
            website = attraction.website,
            phoneNumber = attraction.phoneNumber
        )

        // ── Step 2: enrich in background ──────────────────────────────────
        viewModelScope.launch {
            try {
                val query = "${attraction.name}, Bangkok, Thailand"
                Log.d(tag, "Enriching '${attraction.name}' via Retrofit — query='$query'")
                Log.d(tag, "FieldMask: $DETAIL_FIELD_MASK")

                val places = placesRepository.searchPlace(
                    query = query,
                    fieldMask = DETAIL_FIELD_MASK
                )

                if (places.isEmpty()) {
                    Log.w(tag, "No enrichment results for '${attraction.name}'")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }

                val place = places[0]

                Log.d(tag, "Enriched '${attraction.name}':")
                Log.d(tag, "  Place ID  : ${place.id}")
                Log.d(tag, "  Rating    : ${place.rating}")
                Log.d(tag, "  Reviews   : ${place.userRatingCount}")
                Log.d(tag, "  Phone     : ${place.internationalPhoneNumber}")
                Log.d(tag, "  Website   : ${place.websiteUri}")
                Log.d(tag, "  Lat/Lng   : ${place.location?.latitude}/${place.location?.longitude}")
                Log.d(tag, "  Photos    : ${place.photos?.size ?: 0}")

                val photoName = place.photos?.firstOrNull()?.name?.takeIf { it.isNotBlank() }
                val photoUrl = photoName?.let { placesRepository.getPhotoUrl(it, 1200) }
                Log.d(tag, "  Photo URL : $photoUrl")

                val hours = place.regularOpeningHours
                val description = place.editorialSummary?.text
                    ?.takeIf { it.isNotBlank() } ?: attraction.description

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    description = description,
                    // Prefer fresh API data; fall back to cached seed values
                    photoUrl = photoUrl ?: attraction.photoUrl,
                    rating = place.rating ?: attraction.rating,
                    userRatingsTotal = place.userRatingCount ?: attraction.userRatingsTotal,
                    address = place.formattedAddress?.takeIf { it.isNotBlank() }
                        ?: attraction.address,
                    isOpenNow = hours?.openNow ?: attraction.isOpenNow,
                    openingHours = hours?.weekdayDescriptions
                        ?.takeIf { it.isNotEmpty() } ?: attraction.openingHours,
                    placeId = place.id?.takeIf { it.isNotBlank() } ?: attraction.placeId,
                    latitude = place.location?.latitude ?: attraction.latitude,
                    longitude = place.location?.longitude ?: attraction.longitude,
                    website = place.websiteUri?.takeIf { it.isNotBlank() } ?: attraction.website,
                    phoneNumber = place.internationalPhoneNumber
                        ?.takeIf { it.isNotBlank() } ?: attraction.phoneNumber
                )

            } catch (e: Exception) {
                Log.e(tag, "Enrichment failed for '${attraction.name}': ${e.message}", e)
                // Keep cached data visible — just clear the spinner
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Could not refresh details: ${e.message}"
                )
            }
        }
    }

    companion object {
        private val DETAIL_FIELD_MASK = listOf(
            "places.id",
            "places.displayName",
            "places.formattedAddress",
            "places.internationalPhoneNumber",
            "places.websiteUri",
            "places.rating",
            "places.userRatingCount",
            "places.regularOpeningHours",
            "places.photos",
            "places.location",
            "places.editorialSummary"
        ).joinToString(",")
    }
}
