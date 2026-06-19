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
    /**
     * Fetches transit route alternatives from the Google Directions API.
     *
     * Makes up to three sequential requests with decreasing rail preference:
     *  1. Rail-biased: transit_mode=rail — strongly prefers BTS/MRT/ARL.
     *     No routing preference constraint so walking to/from stations is allowed.
     *  2. Unrestricted transit: Google's default — any transit mode (BTS, MRT, bus).
     *     Only fires when Request 1 returns no results.
     *  3. Walking fallback: absolute last resort when no transit exists at all.
     *     Only fires when both transit requests return nothing.
     *
     * Bus and driving segments are stripped from all results.
     * The caller (RouteViewModel.selectBestRoute) picks the best route from the list.
     * The list is guaranteed to be non-empty — an empty combined response throws.
     */
    suspend fun getRoutes(
        origin: MapLatLng,
        destination: MapLatLng,
        mode: String = "transit",
    ): List<RouteResult> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            error("Missing Google Maps API key. Add GOOGLE_MAPS_API_KEY to local.properties.")
        }

        val originParam = "${origin.latitude},${origin.longitude}"
        val destinationParam = "${destination.latitude},${destination.longitude}"
        val departureTime = System.currentTimeMillis() / 1000L
        val isTransit = mode == "transit"

        // Build a URL with the given extra parameters appended
        fun buildUrl(vararg extras: String): URL {
            val urlString = buildString {
                append("https://maps.googleapis.com/maps/api/directions/json?")
                append("origin=${originParam.urlEncode()}&")
                append("destination=${destinationParam.urlEncode()}&")
                append("mode=${mode.urlEncode()}&")
                append("alternatives=true&")
                append("language=en&")
                append("region=th&")
                if (isTransit) {
                    append("departure_time=$departureTime&")
                }
                extras.forEach { append("$it&") }
                append("key=${apiKey.urlEncode()}")
            }
            return URL(urlString)
        }

        // Request 1 — rail-only bias: transit_mode=rail, no routing preference constraint.
        // Removing less_walking allows Google to return rail routes that require walking
        // to/from the station, which is the normal BTS/MRT usage pattern in Bangkok.
        val railUrl = if (isTransit) {
            buildUrl("transit_mode=rail")
        } else {
            buildUrl()
        }

        // Request 2 — unrestricted transit: Google's default transit routing.
        // No transit_mode filter — allows BTS, MRT, bus, any combination.
        // Only used when Request 1 returns no results (e.g. no rail near origin/dest).
        val defaultTransitUrl = if (isTransit) buildUrl() else null

        // Request 3 — walk-only absolute last resort.
        // Only fires when both transit requests return nothing.
        val walkUrl = if (isTransit) {
            URL(buildString {
                append("https://maps.googleapis.com/maps/api/directions/json?")
                append("origin=${originParam.urlEncode()}&")
                append("destination=${destinationParam.urlEncode()}&")
                append("mode=walking&")
                append("alternatives=true&")
                append("language=en&")
                append("region=th&")
                append("key=${apiKey.urlEncode()}")
            })
        } else {
            null
        }

        val railResults = fetchRoutes(railUrl)
        val defaultResults = if (isTransit && railResults.isEmpty() && defaultTransitUrl != null) {
            fetchRoutes(defaultTransitUrl)
        } else {
            emptyList()
        }
        val walkResults = if (railResults.isEmpty() && defaultResults.isEmpty() && walkUrl != null) {
            fetchRoutes(walkUrl)
        } else {
            emptyList()
        }

        // Merge: rail first, then default transit, then walk last resort.
        // Bus and driving segments are stripped — the app only supports walk + BTS/MRT.
        val seenDurations = mutableSetOf<Int>()
        val merged = buildList {
            for (result in railResults) {
                val stripped = result.stripDrivingAndBusSegments()
                if (seenDurations.add(stripped.route.durationSeconds)) add(stripped)
            }
            for (result in defaultResults) {
                val stripped = result.stripDrivingAndBusSegments()
                if (seenDurations.add(stripped.route.durationSeconds)) add(stripped)
            }
            for (result in walkResults) {
                val stripped = result.stripDrivingAndBusSegments()
                if (seenDurations.add(stripped.route.durationSeconds)) add(stripped)
            }
        }

        merged.also {
            if (it.isEmpty()) error("No route found to this destination.")
        }
    }

    /** Executes one HTTP request and parses all routes in the response. Returns empty list on API errors. */
    private fun fetchRoutes(url: URL): List<RouteResult> {
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 12_000
        connection.readTimeout = 12_000
        connection.requestMethod = "GET"

        return try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode !in 200..299) {
                val message = json.optString("error_message").takeIf { it.isNotBlank() }
                    ?: "Directions request failed with HTTP $responseCode."
                error(message)
            }

            val status = json.optString("status")
            if (status != "OK") {
                // ZERO_RESULTS on the rail request is expected when no rail route exists —
                // return empty so the fallback request is tried.
                if (status == "ZERO_RESULTS") return emptyList()
                error(
                    when (status) {
                        "NOT_FOUND" -> "Could not find a valid route."
                        "REQUEST_DENIED" -> "Directions API access denied. Check your API key and enabled APIs."
                        else -> "Directions request failed: $status"
                    },
                )
            }

            val routesArray = json.optJSONArray("routes") ?: return emptyList()
            buildList {
                for (routeIndex in 0 until routesArray.length()) {
                    val route = routesArray.optJSONObject(routeIndex) ?: continue
                    val parsed = parseRoute(route)
                    if (parsed != null) add(parsed)
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Fetches a transit route between two station coordinates.
     * Used for the middle leg of the 3-leg rail-first routing strategy.
     *
     * Both endpoints are already BTS/MRT station coordinates, so Google has
     * no reason to return walking or bus — it will route via the rail network.
     * No transit_mode filter is applied; Google picks the correct line naturally.
     *
     * Returns null if no transit route exists between the two stations.
     */
    suspend fun getStationTransitRoute(
        originStation: MapLatLng,
        destinationStation: MapLatLng,
    ): RouteResult? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            error("Missing Google Maps API key. Add GOOGLE_MAPS_API_KEY to local.properties.")
        }

        val originParam      = "${originStation.latitude},${originStation.longitude}"
        val destinationParam = "${destinationStation.latitude},${destinationStation.longitude}"
        val departureTime    = System.currentTimeMillis() / 1000L

        val urlString = buildString {
            append("https://maps.googleapis.com/maps/api/directions/json?")
            append("origin=${originParam.urlEncode()}&")
            append("destination=${destinationParam.urlEncode()}&")
            append("mode=transit&")
            append("alternatives=true&")
            append("departure_time=$departureTime&")
            append("language=en&")
            append("region=th&")
            append("key=${apiKey.urlEncode()}")
        }

        val results = fetchRoutes(URL(urlString))
        if (results.isEmpty()) return@withContext null

        // Prefer the result with the most rail segments, then shortest duration.
        val railTypes = setOf(
            RouteTransportType.BTS_SUKHUMVIT,
            RouteTransportType.BTS_SILOM,
            RouteTransportType.MRT_BLUE,
            RouteTransportType.MRT_PURPLE,
            RouteTransportType.AIRPORT_RAIL_LINK,
        )
        results
            .filter { it.segments.any { seg -> seg.transportType in railTypes } }
            .minByOrNull { it.route.durationSeconds }
            ?: results.minByOrNull { it.route.durationSeconds }
    }

    /**
     * Requests a rail-first transit route by passing the two nearest BTS/MRT stations
     * as waypoints. This forces Google to route through the rail network while still
     * computing correct walking legs to/from each station.
     *
     * Returns null (without throwing) when Google finds no valid route through the
     * given waypoints — the caller should fall back to [getRoutes] in that case.
     */
    suspend fun getRailFirstRoute(
        origin: MapLatLng,
        destination: MapLatLng,
        originStation: MapLatLng,
        destinationStation: MapLatLng,
    ): RouteResult? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            error("Missing Google Maps API key. Add GOOGLE_MAPS_API_KEY to local.properties.")
        }

        val originParam      = "${origin.latitude},${origin.longitude}"
        val destinationParam = "${destination.latitude},${destination.longitude}"
        val departureTime    = System.currentTimeMillis() / 1000L

        // Waypoints: origin station then destination station.
        // Using the "via:" prefix keeps them as pass-through waypoints (not stopovers),
        // so Google doesn't add extra legs — the route remains a single coherent trip.
        val waypointsParam = "via:${originStation.latitude},${originStation.longitude}" +
            "|via:${destinationStation.latitude},${destinationStation.longitude}"

        val urlString = buildString {
            append("https://maps.googleapis.com/maps/api/directions/json?")
            append("origin=${originParam.urlEncode()}&")
            append("destination=${destinationParam.urlEncode()}&")
            append("waypoints=${waypointsParam.urlEncode()}&")
            append("mode=transit&")
            append("transit_mode=rail&")
            append("alternatives=false&")
            append("departure_time=$departureTime&")
            append("language=en&")
            append("region=th&")
            append("key=${apiKey.urlEncode()}")
        }

        val results = fetchRoutes(URL(urlString))
        if (results.isEmpty()) return@withContext null

        // Keep only results that actually contain at least one rail transit segment.
        // If Google routed entirely by walking despite the waypoints, discard it.
        val railResult = results.firstOrNull { routeResult ->
            routeResult.segments.any { seg ->
                seg.travelMode == TravelMode.TRANSIT && seg.transitDetails != null
            }
        }
        railResult
    }

    /**
     * Convenience method that fetches a single walking route between two points.
     * Used by the station-first routing flow for the walk legs:
     *   user → origin station  and  destination station → final destination.
     *
     * Returns the first (shortest) walking route Google provides.
     * Throws if no walking route is found.
     */
    suspend fun getWalkingRoute(
        origin: MapLatLng,
        destination: MapLatLng,
    ): RouteResult = getRoutes(origin, destination, mode = "walking").first()

    /**
     * Removes any DRIVING or BUS segments from a RouteResult.
     * The app only navigates via walking and BTS/MRT rail transit.
     */
    private fun RouteResult.stripDrivingAndBusSegments(): RouteResult {
        val allowedTypes = setOf(
            RouteTransportType.WALKING,
            RouteTransportType.BTS_SUKHUMVIT,
            RouteTransportType.BTS_SILOM,
            RouteTransportType.MRT_BLUE,
            RouteTransportType.MRT_PURPLE,
            RouteTransportType.AIRPORT_RAIL_LINK,
            RouteTransportType.UNKNOWN_TRANSIT,
        )
        val filteredSegments = segments.filter { it.transportType in allowedTypes }
        val filteredSteps = steps.filter { it.transportType in allowedTypes }
        return copy(segments = filteredSegments, steps = filteredSteps)
    }

    /** Parses a single route JSON object into a [RouteResult], or null if the route is malformed. */
    private fun parseRoute(route: JSONObject): RouteResult? {
        val leg = route.optJSONArray("legs")?.optJSONObject(0) ?: return null
        val distance = leg.optJSONObject("distance") ?: return null
        val duration = leg.optJSONObject("duration") ?: return null
        val fare = route.optJSONObject("fare")
        val encodedPolyline = route.optJSONObject("overview_polyline")?.optString("points")
            ?.takeIf { it.isNotBlank() } ?: return null
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
                                departureTimeText = transit.optJSONObject("departure_time")?.optString("text"),
                                arrivalTimeText = transit.optJSONObject("arrival_time")?.optString("text"),
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

        val navigationSteps = buildList {
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
        }

        return RouteResult(
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
            steps = navigationSteps,
        )
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

        // Step 1 — use the structured vehicle.type field Google reliably returns.
        // This is far more robust than text matching on localised line names.
        val vehicleType = details.vehicleType?.uppercase(Locale.US).orEmpty()
        val text = details.searchableText()
        val lineText = details.lineNameText()

        val railResult = when {
            // BTS / Skytrain — TRAM/MONORAIL, BTS agency, or explicit BTS line name.
            vehicleType in setOf("TRAM", "MONORAIL") ||
                isBtsAgency(details) ||
                lineText.contains("bts") -> {
                classifyBtsLine(text)
            }
            // MRT / Metro — subway-class vehicles or explicit MRT line name.
            vehicleType in setOf("HEAVY_RAIL", "SUBWAY", "METRO_RAIL") ||
                isMrtAgency(details) ||
                lineText.contains("mrt") -> {
                classifyMrtLine(text)
            }
            // Airport Rail Link (COMMUTER_TRAIN) — identified by line/agency, not station names.
            vehicleType == "COMMUTER_TRAIN" || vehicleType == "RAIL" ||
                text.contains("airport rail link") ||
                text.contains("arl") || text.contains("suvarnabhumi") -> {
                RouteTransportType.AIRPORT_RAIL_LINK
            }
            vehicleType == "BUS" || text.contains("bus") || text.contains("รถบัส") -> {
                RouteTransportType.BUS
            }
            else -> classifyByText(text)
        }
        return railResult
    }

    /** True when any agency name clearly identifies this as a BTS Skytrain service. */
    private fun isBtsAgency(details: TransitDetails): Boolean {
        val agencyText = details.agencies.joinToString(" ").lowercase(Locale.US)
        return agencyText.contains("bts") ||
            agencyText.contains("bangkok mass transit system") ||
            agencyText.contains("krungthep thanakom")
    }

    /** True when any agency name clearly identifies this as an MRTA/MRT service. */
    private fun isMrtAgency(details: TransitDetails): Boolean {
        val agencyText = details.agencies.joinToString(" ").lowercase(Locale.US)
        return agencyText.contains("mrta") ||
            agencyText.contains("metropolitan rapid transit") ||
            agencyText.contains("northern bangkok monorail") ||
            agencyText.contains("eastern bangkok monorail")
    }

    private fun classifyBtsLine(text: String): RouteTransportType = when {
        text.contains("silom") || text.contains("สายสีลม") -> RouteTransportType.BTS_SILOM
        text.contains("sukhumvit") || text.contains("สายสุขุมวิท") -> RouteTransportType.BTS_SUKHUMVIT
        // Gold Line is BTS-operated but a separate monorail spur — treat as Sukhumvit for colour
        text.contains("gold") || text.contains("สายสีทอง") -> RouteTransportType.BTS_SUKHUMVIT
        else -> RouteTransportType.BTS_SUKHUMVIT  // default for any unspecified BTS service
    }

    private fun classifyMrtLine(text: String): RouteTransportType = when {
        text.contains("purple") || text.contains("สายสีม่วง") -> RouteTransportType.MRT_PURPLE
        text.contains("pink") || text.contains("สายสีชมพู") -> RouteTransportType.MRT_PURPLE  // nearest colour match
        text.contains("yellow") || text.contains("สายสีเหลือง") -> RouteTransportType.MRT_BLUE  // nearest colour match
        text.contains("blue") || text.contains("สายสีน้ำเงิน") -> RouteTransportType.MRT_BLUE
        else -> RouteTransportType.MRT_BLUE  // default for any unspecified MRT service
    }

    /** Last-resort classification using only line/stop/agency name text. */
    private fun classifyByText(text: String): RouteTransportType = when {
        text.contains("airport rail link") || text.contains("arl") ||
            text.contains("suvarnabhumi") -> RouteTransportType.AIRPORT_RAIL_LINK
        text.contains("silom") || text.contains("สายสีลม") -> RouteTransportType.BTS_SILOM
        text.contains("sukhumvit") || text.contains("สายสุขุมวิท") -> RouteTransportType.BTS_SUKHUMVIT
        text.contains("bts") || text.contains("skytrain") -> RouteTransportType.BTS_SUKHUMVIT
        text.contains("purple") || text.contains("สายสีม่วง") -> RouteTransportType.MRT_PURPLE
        text.contains("blue line") || text.contains("สายสีน้ำเงิน") -> RouteTransportType.MRT_BLUE
        text.contains("mrt") || text.contains("metro") || text.contains("subway") -> RouteTransportType.MRT_BLUE
        text.contains("bus") || text.contains("รถบัส") -> RouteTransportType.BUS
        else -> RouteTransportType.UNKNOWN_TRANSIT
    }

    private fun TransitDetails.lineNameText(): String {
        return listOfNotNull(lineName, lineShortName)
            .joinToString(" ")
            .lowercase(Locale.US)
    }

    private fun TransitDetails.searchableText(): String {
        return listOfNotNull(
            lineName,
            lineShortName,
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
    val departureTimeText: String? = null,
    val arrivalTimeText: String? = null,
)
