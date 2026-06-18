package com.example.cityflowbkk.features.tour.data

import android.content.Context
import android.util.Log
import com.example.cityflowbkk.data.places.PlacesRepository
import com.example.cityflowbkk.data.places.model.GooglePlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class TourPlacesRepository(
    private val context: Context,
    private val placesRepository: PlacesRepository
) {
    private val tag = "TourPlacesRepo"

    suspend fun loadAttractions(): List<AttractionUiModel> = withContext(Dispatchers.IO) {
        val seeds = loadSeedsFromJson()
        Log.d(tag, "Loaded ${seeds.size} seeds from bangkok_landmarks.json")
        val attractions = mutableListOf<AttractionUiModel>()

        for (seed in seeds) {
            val attraction = fetchPlaceDetails(seed)
            attractions.add(attraction)
        }

        Log.d(tag, "Finished loading ${attractions.size} attractions")
        attractions
    }

    private fun loadSeedsFromJson(): List<LandmarkSeed> {
        val jsonString = context.assets.open("bangkok_landmarks.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        val seeds = mutableListOf<LandmarkSeed>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            seeds.add(
                LandmarkSeed(
                    id = obj.getInt("id"),
                    name = obj.getString("name"),
                    description = obj.getString("description"),
                    category = obj.getString("category")
                )
            )
        }
        return seeds
    }

    private suspend fun fetchPlaceDetails(seed: LandmarkSeed): AttractionUiModel {
        val query = "${seed.name}, Bangkok, Thailand"
        Log.d(tag, "─────────────────────────────────────────────")
        Log.d(tag, "Fetching: '${seed.name}' (id=${seed.id})")
        Log.d(tag, "Query: '$query'")
        Log.d(tag, "FieldMask: $PLACE_SEARCH_FIELD_MASK")
        try {
            val places = placesRepository.searchPlace(
                query = query,
                fieldMask = PLACE_SEARCH_FIELD_MASK
            )
            if (places.isEmpty()) {
                Log.w(tag, "⚠ No results for '${seed.name}' — using fallback")
                return createFallbackAttraction(seed, "No places found")
            }

            val place = places[0]
            logPlaceDebug(seed.name, place)
            return parseGooglePlace(seed, place)
        } catch (e: Exception) {
            Log.e(tag, "✗ Exception fetching '${seed.name}': ${e.message}", e)
            return createFallbackAttraction(seed, e.message)
        }
    }

    private fun logPlaceDebug(name: String, place: GooglePlace) {
        Log.d(tag, "── API result for '$name' ──")
        Log.d(tag, "  Place ID        : ${place.id ?: "MISSING"}")
        Log.d(tag, "  Display Name    : ${place.displayName?.text ?: "MISSING"}")
        Log.d(tag, "  Rating          : ${place.rating ?: "MISSING (null)"}")
        Log.d(tag, "  Review Count    : ${place.userRatingCount ?: "MISSING (null)"}")
        Log.d(tag, "  Address         : ${place.formattedAddress ?: "MISSING (null)"}")

        val openingHours = place.regularOpeningHours
        if (openingHours == null) {
            Log.w(tag, "  Opening Hours   : MISSING (null) — field may not be in response or API key lacks billing")
        } else {
            Log.d(tag, "  Open Now        : ${openingHours.openNow}")
            Log.d(tag, "  Weekly Hours    : ${openingHours.weekdayDescriptions}")
        }

        val photos = place.photos
        if (photos.isNullOrEmpty()) {
            Log.w(tag, "  Photos          : MISSING — no photos returned. Check field mask includes 'photos'")
        } else {
            Log.d(tag, "  Photos count    : ${photos.size}")
            photos.forEachIndexed { i, photo ->
                Log.d(tag, "  Photo[$i] name  : ${photo.name}")
                Log.d(tag, "  Photo[$i] size  : ${photo.widthPx}x${photo.heightPx}")
            }
            val firstPhotoName = photos.firstOrNull()?.name
            if (firstPhotoName != null) {
                val url = placesRepository.getPhotoUrl(firstPhotoName, 800)
                Log.d(tag, "  Photo URL       : $url")
            } else {
                Log.w(tag, "  Photo URL       : SKIPPED — first photo has null name")
            }
        }

        // Warn on missing fields
        if (place.rating == null)
            Log.w(tag, "  ⚠ rating is null — ensure 'places.rating' is in field mask AND API key has billing enabled")
        if (place.userRatingCount == null)
            Log.w(tag, "  ⚠ userRatingCount is null — ensure 'places.userRatingCount' is in field mask AND API key has billing enabled")
    }

    private fun parseGooglePlace(seed: LandmarkSeed, place: GooglePlace): AttractionUiModel {
        val openingHoursJson = place.regularOpeningHours
        val openingHours = openingHoursJson?.weekdayDescriptions ?: emptyList()

        val photoName = place.photos?.firstOrNull()?.name?.takeIf { it.isNotBlank() }

        val address = place.formattedAddress?.takeIf { it.isNotBlank() }
        val rating = place.rating
        val userRatingsTotal = place.userRatingCount
        val isOpenNow = openingHoursJson?.openNow

        val photoUrl = photoName?.let { placesRepository.getPhotoUrl(it, 800) }

        Log.d(tag, "Parsed '${seed.name}': rating=$rating, reviews=$userRatingsTotal, hasPhoto=${photoUrl != null}, address=$address, hours=${openingHours.size} entries")

        return AttractionUiModel(
            id = seed.id,
            name = seed.name,
            description = seed.description,
            category = seed.category,
            photoUrl = photoUrl,
            rating = rating,
            userRatingsTotal = userRatingsTotal,
            address = address,
            openingHours = openingHours,
            isOpenNow = isOpenNow
        )
    }

    private fun createFallbackAttraction(seed: LandmarkSeed, exceptionMessage: String?): AttractionUiModel {
        Log.d(tag, "Creating fallback for '${seed.name}' reason='$exceptionMessage'")
        return AttractionUiModel(
            id = seed.id,
            name = seed.name,
            description = seed.description,
            category = seed.category,
            photoUrl = null,
            rating = null,
            userRatingsTotal = null,
            address = "Bangkok, Thailand",
            openingHours = emptyList(),
            isOpenNow = null
        )
    }

    companion object {
        private val placeDetailsFields = listOf(
            "id",
            "displayName",
            "photos",
            "rating",
            "userRatingCount",
            "formattedAddress",
            "regularOpeningHours"
        )
        val PLACE_SEARCH_FIELD_MASK = placeDetailsFields.joinToString(",") { "places.$it" }
    }
}
