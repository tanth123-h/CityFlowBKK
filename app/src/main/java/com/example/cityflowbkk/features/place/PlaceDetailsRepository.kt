package com.example.cityflowbkk.features.place

import com.example.cityflowbkk.features.home.PopularPlaceUiModel
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class PlaceDetailsRepository(
    private val apiKey: String,
) {
    suspend fun fetchPlaceDetails(place: PopularPlaceUiModel): PlaceDetailUiModel = withContext(Dispatchers.IO) {
        val json = if (place.placeId.isNullOrBlank()) {
            searchPlace(place)
        } else {
            fetchPlaceById(place.placeId)
        }

        json.toPlaceDetailUiModel(apiKey)
    }

    private fun searchPlace(place: PopularPlaceUiModel): JSONObject {
        val body = JSONObject()
            .put("textQuery", "${place.name}, Bangkok, Thailand")
            .put("maxResultCount", 1)

        val latitude = place.latitude
        val longitude = place.longitude
        if (latitude != null && longitude != null) {
            body.put(
                "locationBias",
                JSONObject().put(
                    "circle",
                    JSONObject()
                        .put(
                            "center",
                            JSONObject()
                                .put("latitude", latitude)
                                .put("longitude", longitude),
                        )
                        .put("radius", 2500.0),
                ),
            )
        }

        val response = URL("https://places.googleapis.com/v1/places:searchText")
            .readJsonResponse(
                method = "POST",
                body = body.toString(),
                fieldMask = PLACE_SEARCH_FIELD_MASK,
            )

        val places = response.optJSONArray("places")
        if (places == null || places.length() == 0) {
            error("Google Places did not find details for ${place.name}.")
        }
        return places.getJSONObject(0)
    }

    private fun fetchPlaceById(placeId: String): JSONObject {
        return URL("https://places.googleapis.com/v1/places/$placeId")
            .readJsonResponse(
                method = "GET",
                body = null,
                fieldMask = PLACE_DETAILS_FIELD_MASK,
            )
    }

    private fun JSONObject.toPlaceDetailUiModel(apiKey: String): PlaceDetailUiModel {
        val openingHoursJson = optJSONObject("regularOpeningHours")
        val hoursArray = openingHoursJson?.optJSONArray("weekdayDescriptions")
        val openingHours = buildList {
            if (hoursArray != null) {
                for (index in 0 until hoursArray.length()) {
                    add(hoursArray.optString(index))
                }
            }
        }

        val location = optJSONObject("location")
        val photoName = optJSONArray("photos")
            ?.optJSONObject(0)
            ?.optString("name")
            ?.takeIf { it.isNotBlank() }

        val name = optJSONObject("displayName")?.optString("text")?.ifBlank { null } ?: "Selected place"
        val address = optString("formattedAddress").ifBlank { null }
        val summary = optJSONObject("editorialSummary")?.optString("text")?.takeIf { it.isNotBlank() }

        return PlaceDetailUiModel(
            placeId = optString("id"),
            name = name,
            photoUrl = photoName?.let {
                "https://places.googleapis.com/v1/$it/media?maxWidthPx=1200&key=$apiKey"
            },
            rating = if (has("rating")) optDouble("rating") else null,
            userRatingsTotal = if (has("userRatingCount")) optInt("userRatingCount") else null,
            address = address,
            isOpenNow = openingHoursJson?.takeIf { it.has("openNow") }?.optBoolean("openNow"),
            openingHours = openingHours,
            website = optString("websiteUri").ifBlank { null },
            phoneNumber = optString("nationalPhoneNumber").ifBlank { null },
            description = summary ?: "Discover current visitor information, contact details, and opening hours for $name from Google Places.",
            latitude = location?.optDouble("latitude"),
            longitude = location?.optDouble("longitude"),
        )
    }

    private fun URL.readJsonResponse(
        method: String,
        body: String?,
        fieldMask: String,
    ): JSONObject {
        val connection = openConnection() as HttpURLConnection
        connection.connectTimeout = 12_000
        connection.readTimeout = 12_000
        connection.requestMethod = method
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("X-Goog-Api-Key", apiKey)
        connection.setRequestProperty("X-Goog-FieldMask", fieldMask)

        if (body != null) {
            connection.doOutput = true
            connection.outputStream.use { stream ->
                stream.write(body.toByteArray(Charsets.UTF_8))
            }
        }

        return try {
            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val responseText = stream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)
            if (connection.responseCode !in 200..299) {
                val message = json.optJSONObject("error")?.optString("message")
                    ?: "Google Places request failed with HTTP ${connection.responseCode}."
                error(message)
            }
            json
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val PLACE_DETAILS_FIELD_MASK =
            "id,displayName,photos,rating,userRatingCount,formattedAddress,regularOpeningHours,websiteUri,nationalPhoneNumber,editorialSummary,location"

        const val PLACE_SEARCH_FIELD_MASK =
            "places.$PLACE_DETAILS_FIELD_MASK"
    }
}
