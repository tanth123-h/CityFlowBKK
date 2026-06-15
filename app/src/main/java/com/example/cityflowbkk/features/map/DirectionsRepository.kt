package com.example.cityflowbkk.features.map

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DirectionsRepository(
    private val apiKey: String,
) {
    suspend fun getRoute(
        origin: MapLatLng,
        destination: MapLatLng,
    ): RouteResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            error("Missing Google Maps API key. Add MAPS_API_KEY to local.properties.")
        }

        val originParam = "${origin.latitude},${origin.longitude}"
        val destinationParam = "${destination.latitude},${destination.longitude}"
        val url = URL(
            "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${originParam.urlEncode()}&" +
                "destination=${destinationParam.urlEncode()}&" +
                "mode=driving&" +
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
                    ?: "Directions request failed with HTTP ${connection.responseCode}."
                error(message)
            }

            val status = json.optString("status")
            if (status != "OK") {
                error(
                    when (status) {
                        "ZERO_RESULTS" -> "No route found to this destination."
                        "NOT_FOUND" -> "Could not find a valid route."
                        "REQUEST_DENIED" -> "Directions API access denied. Check your API key and enabled APIs."
                        else -> "Directions request failed: $status"
                    },
                )
            }

            val route = json.getJSONArray("routes").getJSONObject(0)
            val leg = route.getJSONArray("legs").getJSONObject(0)
            val distance = leg.getJSONObject("distance")
            val duration = leg.getJSONObject("duration")
            val encodedPolyline = route.getJSONObject("overview_polyline").getString("points")

            RouteResult(
                route = RouteUiModel(
                    distanceText = distance.getString("text"),
                    distanceMeters = distance.getInt("value"),
                    durationText = duration.getString("text"),
                    durationSeconds = duration.getInt("value"),
                    arrivalTimeText = formatArrivalTime(duration.getInt("value")),
                ),
                points = PolylineDecoder.decode(encodedPolyline),
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun formatArrivalTime(durationSeconds: Int): String {
        val arrivalMillis = System.currentTimeMillis() + durationSeconds * 1000L
        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return formatter.format(java.util.Date(arrivalMillis))
    }

    private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())
}

data class RouteResult(
    val route: RouteUiModel,
    val points: List<MapLatLng>,
)
