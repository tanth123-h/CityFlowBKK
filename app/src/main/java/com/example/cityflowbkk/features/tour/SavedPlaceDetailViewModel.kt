package com.example.cityflowbkk.features.tour

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cityflowbkk.BuildConfig
import com.example.cityflowbkk.features.tour.data.AttractionUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SavedPlaceDetailViewModel : ViewModel() {

    private val tag = "SavedPlaceDetailVM"
    private val apiKey = BuildConfig.GOOGLE_PLACES_API_KEY

    private val _uiState = MutableStateFlow(SavedPlaceDetailUiState())
    val uiState: StateFlow<SavedPlaceDetailUiState> = _uiState.asStateFlow()

    /**
     * Seed instantly from the cached AttractionUiModel, then enrich with
     * a Place Details call to get phone, website, coordinates, and current hours.
     */
    fun load(attraction: AttractionUiModel) {
        // 1. Render immediately with cached data so screen is never blank
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
            openingHours = attraction.openingHours
        )

        // 2. Fetch enriched details in background
        viewModelScope.launch {
            try {
                val detail = fetchDetails("${attraction.name}, Bangkok, Thailand")
                Log.d(tag, "Enriched detail for '${attraction.name}': $detail")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    // Prefer API data; fall back to cached value
                    photoUrl = detail.photoUrl ?: attraction.photoUrl,
                    rating = detail.rating ?: attraction.rating,
                    userRatingsTotal = detail.userRatingsTotal ?: attraction.userRatingsTotal,
                    address = detail.address ?: attraction.address,
                    isOpenNow = detail.isOpenNow ?: attraction.isOpenNow,
                    openingHours = detail.openingHours.ifEmpty { attraction.openingHours },
                    phoneNumber = detail.phoneNumber,
                    website = detail.website,
                    placeId = detail.placeId,
                    latitude = detail.latitude,
                    longitude = detail.longitude
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to enrich details for '${attraction.name}'", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Could not load full details: ${e.message}"
                )
            }
        }
    }

    private suspend fun fetchDetails(query: String): RichDetail = withContext(Dispatchers.IO) {
        Log.d(tag, "fetchDetails query='$query'")
        Log.d(tag, "FieldMask: $FIELD_MASK")

        val body = JSONObject()
            .put("textQuery", query)
            .put("maxResultCount", 1)
            .toString()

        val url = URL("https://places.googleapis.com/v1/places:searchText")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 12_000
        connection.readTimeout = 12_000
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("X-Goog-Api-Key", apiKey)
        connection.setRequestProperty("X-Goog-FieldMask", FIELD_MASK)
        connection.doOutput = true
        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        val statusCode = connection.responseCode
        Log.d(tag, "HTTP status: $statusCode")

        val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
        val responseText = stream.bufferedReader().use { it.readText() }
        connection.disconnect()

        Log.d(tag, "Raw response: $responseText")

        val root = JSONObject(responseText)

        if (statusCode !in 200..299) {
            val msg = root.optJSONObject("error")?.optString("message")
                ?: "Places API returned HTTP $statusCode"
            Log.e(tag, "API error: $msg")
            error(msg)
        }

        val places = root.optJSONArray("places")
        if (places == null || places.length() == 0) {
            Log.w(tag, "No places found for query='$query'")
            error("No results returned by Google Places for '$query'")
        }

        val place = places.getJSONObject(0)
        Log.d(tag, "Place JSON: $place")

        parsePlace(place)
    }

    private fun parsePlace(place: JSONObject): RichDetail {
        val id = place.optString("id").ifBlank { null }
        val displayName = place.optJSONObject("displayName")?.optString("text")?.ifBlank { null }
        val address = place.optString("formattedAddress").ifBlank { null }
        val phone = place.optString("internationalPhoneNumber").ifBlank { null }
        val website = place.optString("websiteUri").ifBlank { null }
        val rating = if (place.has("rating")) place.optDouble("rating").takeIf { !it.isNaN() } else null
        val reviewCount = if (place.has("userRatingCount")) place.optInt("userRatingCount").takeIf { it > 0 } else null

        val regularHours = place.optJSONObject("regularOpeningHours")
        val isOpenNow = regularHours?.takeIf { it.has("openNow") }?.optBoolean("openNow")
        val hoursArray = regularHours?.optJSONArray("weekdayDescriptions")
        val openingHours = buildList {
            if (hoursArray != null) {
                for (i in 0 until hoursArray.length()) add(hoursArray.optString(i))
            }
        }

        val location = place.optJSONObject("location")
        val lat = location?.optDouble("latitude")?.takeIf { !it.isNaN() }
        val lng = location?.optDouble("longitude")?.takeIf { !it.isNaN() }

        // Photo URL
        val photoName = place.optJSONArray("photos")
            ?.optJSONObject(0)
            ?.optString("name")
            ?.takeIf { it.isNotBlank() }
        val photoUrl = photoName?.let {
            "https://places.googleapis.com/v1/$it/media?maxWidthPx=1200&key=$apiKey"
        }

        Log.d(tag, "Parsed ─────────────────────────")
        Log.d(tag, "  ID           : $id")
        Log.d(tag, "  Display Name : $displayName")
        Log.d(tag, "  Rating       : $rating")
        Log.d(tag, "  Review Count : $reviewCount")
        Log.d(tag, "  Address      : $address")
        Log.d(tag, "  Phone        : $phone")
        Log.d(tag, "  Website      : $website")
        Log.d(tag, "  Open Now     : $isOpenNow")
        Log.d(tag, "  Hours count  : ${openingHours.size}")
        Log.d(tag, "  Latitude     : $lat")
        Log.d(tag, "  Longitude    : $lng")
        Log.d(tag, "  Photo URL    : $photoUrl")
        if (rating == null) Log.w(tag, "  ⚠ rating missing — ensure billing is enabled")
        if (photoUrl == null) Log.w(tag, "  ⚠ photo missing — no photos in response")

        return RichDetail(
            placeId = id,
            photoUrl = photoUrl,
            rating = rating,
            userRatingsTotal = reviewCount,
            address = address,
            isOpenNow = isOpenNow,
            openingHours = openingHours,
            phoneNumber = phone,
            website = website,
            latitude = lat,
            longitude = lng
        )
    }

    private data class RichDetail(
        val placeId: String?,
        val photoUrl: String?,
        val rating: Double?,
        val userRatingsTotal: Int?,
        val address: String?,
        val isOpenNow: Boolean?,
        val openingHours: List<String>,
        val phoneNumber: String?,
        val website: String?,
        val latitude: Double?,
        val longitude: Double?
    )

    companion object {
        // All fields needed for the detail screen
        // rating + userRatingCount + regularOpeningHours = Enterprise SKU
        // internationalPhoneNumber = Enterprise SKU
        private val FIELD_MASK = listOf(
            "places.id",
            "places.displayName",
            "places.formattedAddress",
            "places.internationalPhoneNumber",
            "places.websiteUri",
            "places.rating",
            "places.userRatingCount",
            "places.regularOpeningHours",
            "places.photos",
            "places.location"
        ).joinToString(",")
    }
}
