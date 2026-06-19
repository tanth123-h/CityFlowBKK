package com.example.cityflowbkk.features.tour.data

import android.content.Context
import android.util.Log
import com.example.cityflowbkk.data.places.PlacesRepository
import com.example.cityflowbkk.data.places.model.GooglePlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.json.JSONArray

class TourPlacesRepository(
    private val context: Context,
    private val placesRepository: PlacesRepository
) {
    private val tag = "TourPlacesRepo"

    // ── Public API ────────────────────────────────────────────────────────────

    suspend fun loadAttractions(): List<AttractionUiModel> = withContext(Dispatchers.IO) {
        val seeds = loadSeedsFromJson()
        Log.d(tag, "Loaded ${seeds.size} seeds — fetching all in parallel")

        // Fetch all 20 attractions concurrently instead of sequentially.
        // Each coroutine is independent; failures fall back gracefully.
        val attractions = seeds.map { seed ->
            async { fetchAttraction(seed) }
        }.awaitAll()

        Log.d(tag, "Finished loading ${attractions.size} attractions")
        attractions
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun loadSeedsFromJson(): List<LandmarkSeed> {
        val jsonString = context.assets.open("bangkok_landmarks.json")
            .bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        return buildList {
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                add(
                    LandmarkSeed(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        description = obj.getString("description"),
                        category = obj.getString("category")
                    )
                )
            }
        }
    }

    private suspend fun fetchAttraction(seed: LandmarkSeed): AttractionUiModel {
        val query = "${seed.name}, Bangkok, Thailand"
        Log.d(tag, "Fetching: '${seed.name}' (id=${seed.id})")
        return try {
            val places = placesRepository.searchPlace(
                query = query,
                fieldMask = PLACE_SEARCH_FIELD_MASK
            )
            if (places.isEmpty()) {
                Log.w(tag, "⚠ No results for '${seed.name}' — fallback")
                fallback(seed)
            } else {
                val place = places[0]
                logPlaceDebug(seed.name, place)
                parseGooglePlace(seed, place)
            }
        } catch (e: Exception) {
            Log.e(tag, "✗ Exception fetching '${seed.name}': ${e.message}", e)
            fallback(seed)
        }
    }

    private fun parseGooglePlace(seed: LandmarkSeed, place: GooglePlace): AttractionUiModel {
        val hours = place.regularOpeningHours
        val photoName = place.photos?.firstOrNull()?.name?.takeIf { it.isNotBlank() }
        val photoUrl = photoName?.let { placesRepository.getPhotoUrl(it, 800) }

        Log.d(tag, "Parsed '${seed.name}': " +
                "rating=${place.rating}, " +
                "reviews=${place.userRatingCount}, " +
                "hasPhoto=${photoUrl != null}, " +
                "lat=${place.location?.latitude}, " +
                "lng=${place.location?.longitude}")
        Log.d(tag, "  Photo URL: $photoUrl")

        return AttractionUiModel(
            id = seed.id,
            name = seed.name,
            // Prefer editorial summary from API; fall back to seed description
            description = place.editorialSummary?.text
                ?.takeIf { it.isNotBlank() } ?: seed.description,
            category = seed.category,
            photoUrl = photoUrl,
            rating = place.rating,
            userRatingsTotal = place.userRatingCount,
            address = place.formattedAddress?.takeIf { it.isNotBlank() },
            openingHours = hours?.weekdayDescriptions ?: emptyList(),
            isOpenNow = hours?.openNow,
            // Rich fields — no second API call needed in detail screen
            placeId = place.id?.takeIf { it.isNotBlank() },
            latitude = place.location?.latitude,
            longitude = place.location?.longitude,
            website = place.websiteUri?.takeIf { it.isNotBlank() },
            phoneNumber = place.internationalPhoneNumber?.takeIf { it.isNotBlank() }
        )
    }

    private fun fallback(seed: LandmarkSeed) = AttractionUiModel(
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

    private fun logPlaceDebug(name: String, place: GooglePlace) {
        Log.d(tag, "── API result for '$name' ──────────────────")
        Log.d(tag, "  Place ID        : ${place.id ?: "MISSING"}")
        Log.d(tag, "  Display Name    : ${place.displayName?.text ?: "MISSING"}")
        Log.d(tag, "  Rating          : ${place.rating ?: "MISSING"}")
        Log.d(tag, "  Review Count    : ${place.userRatingCount ?: "MISSING"}")
        Log.d(tag, "  Address         : ${place.formattedAddress ?: "MISSING"}")
        Log.d(tag, "  Website         : ${place.websiteUri ?: "MISSING"}")
        Log.d(tag, "  Phone           : ${place.internationalPhoneNumber ?: "MISSING"}")
        Log.d(tag, "  Lat/Lng         : ${place.location?.latitude}/${place.location?.longitude}")
        Log.d(tag, "  Editorial       : ${place.editorialSummary?.text ?: "MISSING"}")

        val photos = place.photos
        if (photos.isNullOrEmpty()) {
            Log.w(tag, "  Photos          : NONE returned — check field mask / billing")
        } else {
            Log.d(tag, "  Photos count    : ${photos.size}")
            val firstName = photos.firstOrNull()?.name
            if (firstName != null) {
                Log.d(tag, "  Photo[0] name   : $firstName")
                Log.d(tag, "  Photo[0] URL    : ${placesRepository.getPhotoUrl(firstName, 800)}")
            }
        }

        if (place.rating == null)
            Log.w(tag, "  ⚠ rating null — ensure billing and Enterprise SKU enabled")
        if (place.photos.isNullOrEmpty())
            Log.w(tag, "  ⚠ photos null — ensure 'photos' is in field mask")
    }

    companion object {
        /**
         * All fields fetched in a single call.
         * SKU tiers (for cost awareness):
         *   Pro   : displayName, formattedAddress, photos, location
         *   Enterprise: rating, userRatingCount, regularOpeningHours,
         *               websiteUri, internationalPhoneNumber, editorialSummary
         */
        private val FIELDS = listOf(
            "id",
            "displayName",
            "formattedAddress",
            "photos",
            "rating",
            "userRatingCount",
            "regularOpeningHours",
            "location",
            "websiteUri",
            "internationalPhoneNumber",
            "editorialSummary"
        )
        val PLACE_SEARCH_FIELD_MASK = FIELDS.joinToString(",") { "places.$it" }
    }
}
