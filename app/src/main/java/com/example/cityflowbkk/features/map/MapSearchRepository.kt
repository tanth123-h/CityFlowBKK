package com.example.cityflowbkk.features.map

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MapSearchRepository(
    private val apiKey: String,
) {
    suspend fun autocomplete(
        query: String,
        locationBias: MapLatLng? = null,
    ): List<PlaceSuggestionUiModel> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            error("Missing Google Places API key. Add GOOGLE_MAPS_API_KEY to local.properties.")
        }
        if (query.isBlank()) {
            return@withContext emptyList()
        }

        val biasCenter = locationBias ?: MapLatLng(BANGKOK_LATITUDE, BANGKOK_LONGITUDE)
        val body = JSONObject()
            .put("input", query)
            .put(
                "locationBias",
                JSONObject().put(
                    "circle",
                    JSONObject()
                        .put(
                            "center",
                            JSONObject()
                                .put("latitude", biasCenter.latitude)
                                .put("longitude", biasCenter.longitude),
                        )
                        .put("radius", 50_000.0),
                ),
            )

        val response = URL("https://places.googleapis.com/v1/places:autocomplete")
            .readJsonResponse(
                method = "POST",
                body = body.toString(),
                fieldMask = AUTOCOMPLETE_FIELD_MASK,
            )

        val suggestions = response.optJSONArray("suggestions") ?: return@withContext emptyList()
        buildList {
            for (index in 0 until suggestions.length()) {
                val prediction = suggestions
                    .optJSONObject(index)
                    ?.optJSONObject("placePrediction")
                    ?: continue

                val placeId = prediction.optString("placeId")
                if (placeId.isBlank()) continue

                val structuredFormat = prediction.optJSONObject("structuredFormat")
                val mainText = structuredFormat
                    ?.optJSONObject("mainText")
                    ?.optString("text")
                    ?.takeIf { it.isNotBlank() }
                    ?: prediction.optJSONObject("text")?.optString("text")
                    ?: continue

                val secondaryText = structuredFormat
                    ?.optJSONObject("secondaryText")
                    ?.optString("text")
                    ?.takeIf { it.isNotBlank() }

                add(
                    PlaceSuggestionUiModel(
                        placeId = placeId,
                        primaryText = mainText,
                        secondaryText = secondaryText,
                    ),
                )
            }
        }
    }

    suspend fun fetchPlaceDetails(placeId: String): MapPlaceUiModel = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            error("Missing Google Places API key. Add GOOGLE_MAPS_API_KEY to local.properties.")
        }

        val response = URL("https://places.googleapis.com/v1/places/$placeId")
            .readJsonResponse(
                method = "GET",
                body = null,
                fieldMask = PLACE_DETAILS_FIELD_MASK,
            )

        val location = response.optJSONObject("location")
            ?: error("Selected place does not have location coordinates.")

        MapPlaceUiModel(
            placeId = response.optString("id", placeId),
            name = response.optJSONObject("displayName")?.optString("text") ?: "Selected place",
            address = response.optString("formattedAddress").ifBlank { null },
            latitude = location.getDouble("latitude"),
            longitude = location.getDouble("longitude"),
        )
    }

    suspend fun reverseGeocode(location: MapLatLng): MapPlaceUiModel = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            error("Missing Google Maps API key. Add GOOGLE_MAPS_API_KEY to local.properties.")
        }

        val latLng = "${location.latitude},${location.longitude}"
        val url = URL(
            "https://maps.googleapis.com/maps/api/geocode/json?" +
                "latlng=${latLng.urlEncode()}&" +
                "key=${apiKey.urlEncode()}",
        )
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 12_000
        connection.readTimeout = 12_000
        connection.requestMethod = "GET"

        try {
            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val responseText = stream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)
            if (connection.responseCode !in 200..299) {
                val message = json.optString("error_message").takeIf { it.isNotBlank() }
                    ?: "Reverse geocoding failed with HTTP ${connection.responseCode}."
                error(message)
            }

            val status = json.optString("status")
            if (status != "OK") {
                error(
                    when (status) {
                        "ZERO_RESULTS" -> "No address found for this location."
                        "REQUEST_DENIED" -> "Geocoding API access denied. Check your API key and enabled APIs."
                        else -> "Reverse geocoding failed: $status"
                    },
                )
            }

            val result = json.optJSONArray("results")?.optJSONObject(0)
                ?: error("No address found for this location.")
            val formattedAddress = result.optString("formatted_address").takeIf { it.isNotBlank() }
            val placeName = result.optJSONArray("address_components")
                ?.optJSONObject(0)
                ?.optString("long_name")
                ?.takeIf { it.isNotBlank() }
                ?: formattedAddress?.substringBefore(",")
                ?: "Dropped pin"

            MapPlaceUiModel(
                placeId = result.optString("place_id").ifBlank {
                    "dropped:${location.latitude},${location.longitude}"
                },
                name = placeName,
                address = formattedAddress,
                latitude = location.latitude,
                longitude = location.longitude,
            )
        } finally {
            connection.disconnect()
        }
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
}

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

private const val AUTOCOMPLETE_FIELD_MASK =
    "suggestions.placePrediction.placeId," +
        "suggestions.placePrediction.text," +
        "suggestions.placePrediction.structuredFormat.mainText," +
        "suggestions.placePrediction.structuredFormat.secondaryText"

private const val PLACE_DETAILS_FIELD_MASK =
    "id,displayName,formattedAddress,location"
