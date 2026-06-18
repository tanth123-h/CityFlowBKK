package com.example.cityflowbkk.features.map

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import android.text.Html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DirectionsRepository(
    private val apiKey: String,
) {
    suspend fun getRoute(
        origin: MapLatLng,
        destination: MapLatLng,
        mode: String = "driving",
    ): RouteResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            error("Missing Google Maps API key. Add GOOGLE_MAPS_API_KEY to local.properties.")
        }

        val originParam = "${origin.latitude},${origin.longitude}"
        val destinationParam = "${destination.latitude},${destination.longitude}"
        val url = URL(
            "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${originParam.urlEncode()}&" +
                "destination=${destinationParam.urlEncode()}&" +
                "mode=${mode.urlEncode()}&" +
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
            val fare = route.optJSONObject("fare")
            val encodedPolyline = route.getJSONObject("overview_polyline").getString("points")
            val steps = leg.optJSONArray("steps")

            // Parse route segments (for multi-modal transit)
            val segments = buildList {
                if (steps != null) {
                    for (index in 0 until steps.length()) {
                        val step = steps.optJSONObject(index) ?: continue
                        val stepTravelMode = step.optString("travel_mode")
                        val stepPolyline = step.optJSONObject("polyline")?.optString("points")
                        
                        if (stepPolyline.isNullOrBlank()) continue
                        
                        val stepPoints = PolylineDecoder.decode(stepPolyline)
                        
                        val travelMode = when (stepTravelMode) {
                            "WALKING" -> TravelMode.WALKING
                            "TRANSIT" -> TravelMode.TRANSIT
                            "DRIVING" -> TravelMode.DRIVING
                            else -> TravelMode.WALKING
                        }
                        
                        val transitDetails = if (stepTravelMode == "TRANSIT") {
                            step.optJSONObject("transit_details")?.let { transit ->
                                val line = transit.optJSONObject("line")
                                val departureStop = transit.optJSONObject("departure_stop")
                                val arrivalStop = transit.optJSONObject("arrival_stop")
                                
                                TransitDetails(
                                    lineName = line?.optString("name") ?: "Transit",
                                    lineShortName = line?.optString("short_name"),
                                    lineColor = line?.optString("color"),
                                    departureStop = departureStop?.optString("name") ?: "",
                                    arrivalStop = arrivalStop?.optString("name") ?: "",
                                    numStops = transit.optInt("num_stops", 0),
                                    vehicleType = line?.optJSONObject("vehicle")?.optString("name"),
                                )
                            }
                        } else {
                            null
                        }
                        
                        val stepDistance = step.optJSONObject("distance")
                        val stepDuration = step.optJSONObject("duration")

                        add(
                            RouteSegment(
                                points = stepPoints,
                                travelMode = travelMode,
                                transitDetails = transitDetails,
                                distanceText = stepDistance?.optString("text").orEmpty(),
                                distanceMeters = stepDistance?.optInt("value", 0) ?: 0,
                                durationText = stepDuration?.optString("text").orEmpty(),
                                durationSeconds = stepDuration?.optInt("value", 0) ?: 0,
                            ),
                        )
                    }
                }
            }

            RouteResult(
                route = RouteUiModel(
                    distanceText = distance.getString("text"),
                    distanceMeters = distance.getInt("value"),
                    durationText = duration.getString("text"),
                    durationSeconds = duration.getInt("value"),
                    arrivalTimeText = formatArrivalTime(duration.getInt("value")),
                ),
                points = PolylineDecoder.decode(encodedPolyline),
                segments = segments,
                fareText = fare?.optString("text")?.takeIf { it.isNotBlank() },
                fareCurrency = fare?.optString("currency")?.takeIf { it.isNotBlank() },
                fareValue = if (fare != null && fare.has("value")) fare.optDouble("value") else null,
                steps = buildList {
                    if (steps == null) return@buildList

                    for (index in 0 until steps.length()) {
                        val step = steps.optJSONObject(index) ?: continue
                        val stepDistance = step.optJSONObject("distance") ?: continue
                        val stepDuration = step.optJSONObject("duration") ?: continue
                        val startLocation = step.optJSONObject("start_location") ?: continue
                        val endLocation = step.optJSONObject("end_location") ?: continue

                        add(
                            NavigationStepUiModel(
                                instruction = step.optString("html_instructions")
                                    .toPlainText()
                                    .ifBlank { "Continue" },
                                distanceText = stepDistance.optString("text"),
                                distanceMeters = stepDistance.optInt("value"),
                                durationText = stepDuration.optString("text"),
                                durationSeconds = stepDuration.optInt("value"),
                                startLocation = MapLatLng(
                                    latitude = startLocation.getDouble("lat"),
                                    longitude = startLocation.getDouble("lng"),
                                ),
                                endLocation = MapLatLng(
                                    latitude = endLocation.getDouble("lat"),
                                    longitude = endLocation.getDouble("lng"),
                                ),
                            ),
                        )
                    }
                },
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

    private fun String.toPlainText(): String {
        return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
            .toString()
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

data class RouteResult(
    val route: RouteUiModel,
    val points: List<MapLatLng>,
    val steps: List<NavigationStepUiModel>,
    val segments: List<RouteSegment> = emptyList(),
    val fareText: String? = null,
    val fareCurrency: String? = null,
    val fareValue: Double? = null,
)

data class RouteSegment(
    val points: List<MapLatLng>,
    val travelMode: TravelMode,
    val transitDetails: TransitDetails? = null,
    val distanceText: String,
    val distanceMeters: Int,
    val durationText: String,
    val durationSeconds: Int,
)

enum class TravelMode {
    WALKING,
    TRANSIT,
    DRIVING,
}

data class TransitDetails(
    val lineName: String,
    val lineShortName: String?,
    val lineColor: String?,
    val departureStop: String,
    val arrivalStop: String,
    val numStops: Int,
    val vehicleType: String?, // "BTS", "MRT", "BUS", etc.
)
