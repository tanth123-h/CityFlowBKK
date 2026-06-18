package com.example.cityflowbkk.features.map

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import android.text.Html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale

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
                                val vehicle = line?.optJSONObject("vehicle")
                                val agencyNames = line?.optJSONArray("agencies").toStringList("name")
                                val departureLocation = departureStop?.optJSONObject("location")
                                val arrivalLocation = arrivalStop?.optJSONObject("location")

                                TransitDetails(
                                    lineName = line?.optString("name") ?: "Transit",
                                    lineShortName = line?.optString("short_name"),
                                    lineColor = line?.optString("color"),
                                    agencies = agencyNames,
                                    departureStop = departureStop?.optString("name") ?: "",
                                    arrivalStop = arrivalStop?.optString("name") ?: "",
                                    departureLocation = departureLocation?.toMapLatLng(),
                                    arrivalLocation = arrivalLocation?.toMapLatLng(),
                                    numStops = transit.optInt("num_stops", 0),
                                    vehicleType = vehicle?.optString("type"),
                                    vehicleName = vehicle?.optString("name"),
                                )
                            }
                        } else {
                            null
                        }

                        val stepDistance = step.optJSONObject("distance")
                        val stepDuration = step.optJSONObject("duration")
                        val startLocation = step.optJSONObject("start_location")
                        val endLocation = step.optJSONObject("end_location")
                        val transportType = classifyTransportType(travelMode, transitDetails)

                        add(
                            RouteSegment(
                                index = index,
                                points = stepPoints,
                                travelMode = travelMode,
                                transportType = transportType,
                                transitDetails = transitDetails,
                                instruction = buildNavigationInstruction(
                                    htmlInstruction = step.optString("html_instructions").toPlainText(),
                                    transportType = transportType,
                                    transitDetails = transitDetails,
                                    distanceText = stepDistance?.optString("text").orEmpty(),
                                ),
                                distanceText = stepDistance?.optString("text").orEmpty(),
                                distanceMeters = stepDistance?.optInt("value", 0) ?: 0,
                                durationText = stepDuration?.optString("text").orEmpty(),
                                durationSeconds = stepDuration?.optInt("value", 0) ?: 0,
                                startLocation = startLocation?.toMapLatLng(),
                                endLocation = endLocation?.toMapLatLng(),
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
                        val segment = segments.firstOrNull { it.index == index }

                        add(
                            NavigationStepUiModel(
                                index = index,
                                instruction = step.optString("html_instructions")
                                    .toPlainText()
                                    .let { htmlInstruction ->
                                        buildNavigationInstruction(
                                            htmlInstruction = htmlInstruction,
                                            transportType = segment?.transportType ?: RouteTransportType.WALKING,
                                            transitDetails = segment?.transitDetails,
                                            distanceText = stepDistance.optString("text"),
                                        )
                                    },
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
                                points = segment?.points ?: emptyList(),
                                travelMode = segment?.travelMode ?: TravelMode.WALKING,
                                transportType = segment?.transportType ?: RouteTransportType.WALKING,
                                transitDetails = segment?.transitDetails,
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

    private fun JSONObject.toMapLatLng(): MapLatLng? {
        if (!has("lat") || !has("lng")) return null
        return MapLatLng(
            latitude = optDouble("lat"),
            longitude = optDouble("lng"),
        )
    }

    private fun org.json.JSONArray?.toStringList(key: String): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val value = optJSONObject(index)?.optString(key)
                if (!value.isNullOrBlank()) add(value)
            }
        }
    }

    private fun classifyTransportType(
        travelMode: TravelMode,
        details: TransitDetails?,
    ): RouteTransportType {
        if (travelMode == TravelMode.WALKING) return RouteTransportType.WALKING
        if (travelMode == TravelMode.DRIVING) return RouteTransportType.DRIVING
        if (details == null) return RouteTransportType.UNKNOWN_TRANSIT

        val text = details.searchableText()
        return when {
            text.contains("sukhumvit") || text.contains("สายสุขุมวิท") ->
                RouteTransportType.BTS_SUKHUMVIT
            text.contains("silom") || text.contains("สายสีลม") ->
                RouteTransportType.BTS_SILOM
            text.contains("blue line") || text.contains("สายสีน้ำเงิน") ->
                RouteTransportType.MRT_BLUE
            text.contains("purple line") || text.contains("สายสีม่วง") ->
                RouteTransportType.MRT_PURPLE
            text.contains("airport rail link") || text.contains("arl") || text.contains("suvarnabhumi") ->
                RouteTransportType.AIRPORT_RAIL_LINK
            text.contains("bus") || text.contains("รถบัส") || details.vehicleType.equals("BUS", ignoreCase = true) ->
                RouteTransportType.BUS
            text.contains("bts") || text.contains("skytrain") ->
                RouteTransportType.BTS_SUKHUMVIT
            text.contains("mrt") || text.contains("metro") || text.contains("subway") ->
                RouteTransportType.MRT_BLUE
            else -> RouteTransportType.UNKNOWN_TRANSIT
        }
    }

    private fun TransitDetails.searchableText(): String {
        return listOfNotNull(
            lineName,
            lineShortName,
            lineColor,
            vehicleType,
            vehicleName,
            departureStop,
            arrivalStop,
        ).plus(agencies)
            .joinToString(" ")
            .lowercase(Locale.US)
    }

    private fun RouteTransportType.defaultInstruction(details: TransitDetails?): String {
        return when (this) {
            RouteTransportType.WALKING -> "Walk"
            RouteTransportType.DRIVING -> "Drive"
            RouteTransportType.BUS -> "Take bus ${details?.lineShortName ?: details?.lineName.orEmpty()}".trim()
            RouteTransportType.BTS_SUKHUMVIT,
            RouteTransportType.BTS_SILOM,
            RouteTransportType.MRT_BLUE,
            RouteTransportType.MRT_PURPLE,
            RouteTransportType.AIRPORT_RAIL_LINK,
            RouteTransportType.UNKNOWN_TRANSIT -> "Take ${details?.lineName ?: "transit"}"
        }
    }

    private fun buildNavigationInstruction(
        htmlInstruction: String,
        transportType: RouteTransportType,
        transitDetails: TransitDetails?,
        distanceText: String,
    ): String {
        if (transitDetails != null) {
            val line = transitDetails.lineDisplayName(transportType)
            val stopCount = transitDetails.numStops
            val rideText = when {
                stopCount > 1 -> "Ride $stopCount stops"
                stopCount == 1 -> "Ride 1 stop"
                else -> "Ride"
            }
            return listOfNotNull(
                transitDetails.departureStop.takeIf { it.isNotBlank() }?.let { "Board $line at $it" },
                rideText,
                transitDetails.arrivalStop.takeIf { it.isNotBlank() }?.let { "Exit at $it" },
            ).joinToString(". ")
        }

        if (htmlInstruction.isNotBlank()) return htmlInstruction
        return when (transportType) {
            RouteTransportType.WALKING -> "Walk ${distanceText.ifBlank { "to the next step" }}"
            RouteTransportType.DRIVING -> "Drive ${distanceText.ifBlank { "to the next step" }}"
            else -> transportType.defaultInstruction(transitDetails)
        }
    }

    private fun TransitDetails.lineDisplayName(type: RouteTransportType): String {
        return when (type) {
            RouteTransportType.BTS_SUKHUMVIT -> "BTS Sukhumvit Line"
            RouteTransportType.BTS_SILOM -> "BTS Silom Line"
            RouteTransportType.MRT_BLUE -> "MRT Blue Line"
            RouteTransportType.MRT_PURPLE -> "MRT Purple Line"
            RouteTransportType.AIRPORT_RAIL_LINK -> "Airport Rail Link"
            RouteTransportType.BUS -> listOfNotNull("Bus", lineShortName ?: lineName.takeIf { it.isNotBlank() })
                .joinToString(" ")
            RouteTransportType.WALKING -> "walking"
            RouteTransportType.DRIVING -> "driving"
            RouteTransportType.UNKNOWN_TRANSIT -> lineShortName ?: lineName.takeIf { it.isNotBlank() } ?: "transit"
        }
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
    val index: Int,
    val points: List<MapLatLng>,
    val travelMode: TravelMode,
    val transportType: RouteTransportType,
    val transitDetails: TransitDetails? = null,
    val instruction: String,
    val distanceText: String,
    val distanceMeters: Int,
    val durationText: String,
    val durationSeconds: Int,
    val startLocation: MapLatLng?,
    val endLocation: MapLatLng?,
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
    val agencies: List<String> = emptyList(),
    val departureStop: String,
    val arrivalStop: String,
    val departureLocation: MapLatLng? = null,
    val arrivalLocation: MapLatLng? = null,
    val numStops: Int,
    val vehicleType: String?,
    val vehicleName: String? = null,
)
