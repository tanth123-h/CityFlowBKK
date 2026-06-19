package com.example.cityflowbkk.features.route

import com.example.cityflowbkk.features.map.RouteSegment
import com.example.cityflowbkk.features.map.TransitDetails
import com.example.cityflowbkk.features.map.TravelMode
import java.util.Locale

/**
 * Local fare calculation database for Bangkok transit systems.
 *
 * Google route data is used only to identify transit segments, station names,
 * line names, and stop counts. Fare amounts are calculated from local tables.
 */
object FareRepository {

    private val btsFareTable = mapOf(
        1 to 17,
        2 to 23,
        3 to 26,
        4 to 30,
        5 to 33,
        6 to 37,
        7 to 40,
        8 to 44,
        9 to 47,
    )
    private const val BTS_FARE_CAP = 47

    private val mrtBlueFareTable = mapOf(
        1 to 17,
        2 to 19,
        3 to 21,
        4 to 24,
        5 to 26,
        6 to 28,
        7 to 31,
        8 to 33,
        9 to 35,
        10 to 38,
        11 to 40,
    )
    private const val MRT_BLUE_FARE_CAP = 42

    private val mrtPinkFareTable = mapOf(
        0 to 15,
        1 to 18,
        2 to 25,
        3 to 28,
        4 to 30,
        5 to 34,
        6 to 37,
        7 to 41,
        8 to 44,
    )
    private const val MRT_PINK_FARE_CAP = 45

    private val mrtYellowFareTable = mapOf(
        0 to 15,
        1 to 19,
        2 to 23,
        3 to 27,
        4 to 30,
        5 to 33,
        6 to 34,
        7 to 38,
        8 to 41,
    )
    private const val MRT_YELLOW_FARE_CAP = 45

    private val btsKeywords = listOf(
        "bts",
        "skytrain",
        "sukhumvit",
        "silom",
        "gold line",
        "airport rail link",
        "arl",
    )

    // BTS station names for station-based classification fallback
    private val btsStations = setOf(
        "siam", "chit lom", "phloen chit", "nana", "asok", "phrom phong", "thong lo", "ekkamai",
        "phra khanong", "on nut", "bang chak", "punnawithi", "udom suk", "bang na", "bearing",
        "samrong", "pu chao", "chang erawan", "royal thai navy", "pak nam", "srinagarindra",
        "phraek sa", "sai luat", "kheha",
        "national stadium", "ratchathewi", "phaya thai", "victory monument", "sanam pao",
        "ari", "mo chit", "ha yaek lat phrao", "phahon yothin 24", "ratchayothin", "sena ruam",
        "saphan khwai", "kasetsart university", "ng wongwan", "bang sue", "tao poon",
        "sala daeng", "chong nonsi", "surasak", "saphan taksin", "krung thonburi", "wongwian yai",
        "pho nimit", "talat phlu", "wutthakat", "bang wa",
        "krung thon buri", "charoen nakhon", "khlong san", "itsaraphap", "bang khun non",
        "bang yi khan", "sirindhorn", "bang phlat", "bang o", "bang pho", "talat phlu",
        "ratchapruek", "phasi charoen",
    )

    private val mrtKeywords = listOf(
        "mrt",
        "metro",
        "subway",
        "blue line",
        "purple line",
        "pink line",
        "yellow line",
    )

    enum class TransitType(val label: String) {
        BTS("BTS"),
        MRT("MRT"),
        UNKNOWN("Transit"),
    }

    private enum class MrtLine {
        BLUE,
        PURPLE,
        PINK,
        YELLOW,
        UNKNOWN,
    }

    fun calculateFareSummary(segments: List<RouteSegment>): TransitFareSummary {
        val transitSegments = segments.filter {
            it.travelMode == TravelMode.TRANSIT && it.transitDetails != null
        }

        val btsSegments = mutableListOf<FareSegment>()
        val mrtSegments = mutableListOf<FareSegment>()

        android.util.Log.d("FareRepository", "=== calculateFareSummary START ===")
        android.util.Log.d("FareRepository", "Total segments: ${segments.size}, Transit segments: ${transitSegments.size}")
        
        transitSegments.forEachIndexed { index, segment ->
            val details = segment.transitDetails ?: return@forEachIndexed
            val fareSegment = FareSegment(
                originStation = details.departureStop,
                destinationStation = details.arrivalStop,
                stopCount = details.numStops,
                fare = calculateFare(details, details.numStops),
            )

            val type = detectTransitType(details)
            android.util.Log.d("FareRepository", "Segment[$index]: ${details.departureStop} → ${details.arrivalStop}")
            android.util.Log.d("FareRepository", "  Classified as: $type")
            android.util.Log.d("FareRepository", "  Fare: ${fareSegment.fare}")
            
            when (type) {
                TransitType.BTS -> {
                    btsSegments += fareSegment
                    android.util.Log.d("FareRepository", "  → Added to BTS segments")
                }
                TransitType.MRT -> {
                    mrtSegments += fareSegment
                    android.util.Log.d("FareRepository", "  → Added to MRT segments")
                }
                TransitType.UNKNOWN -> {
                    android.util.Log.d("FareRepository", "  → UNKNOWN, not added to fare")
                }
            }
        }

        val btsFare = btsSegments.sumKnownFares()
        val mrtFare = mrtSegments.sumKnownFares()
        val totalFare = listOfNotNull(btsFare, mrtFare)
            .takeIf { it.isNotEmpty() }
            ?.sum()

        android.util.Log.d("FareRepository", "=== calculateFareSummary RESULT ===")
        android.util.Log.d("FareRepository", "BTS segments count: ${btsSegments.size}, total fare: $btsFare")
        android.util.Log.d("FareRepository", "MRT segments count: ${mrtSegments.size}, total fare: $mrtFare")
        android.util.Log.d("FareRepository", "BTS origin: ${btsSegments.firstOrNull()?.originStation}")
        android.util.Log.d("FareRepository", "BTS destination: ${btsSegments.lastOrNull()?.destinationStation}")
        android.util.Log.d("FareRepository", "MRT origin: ${mrtSegments.firstOrNull()?.originStation}")
        android.util.Log.d("FareRepository", "MRT destination: ${mrtSegments.lastOrNull()?.destinationStation}")

        return TransitFareSummary(
            btsFareBaht = btsFare,
            mrtFareBaht = mrtFare,
            totalFareBaht = totalFare,
            btsOriginStation = btsSegments.firstOrNull()?.originStation,
            btsDestinationStation = btsSegments.lastOrNull()?.destinationStation,
            mrtOriginStation = mrtSegments.firstOrNull()?.originStation,
            mrtDestinationStation = mrtSegments.lastOrNull()?.destinationStation,
        )
    }

    fun detectTransitType(details: TransitDetails): TransitType {
        val vehicleType = details.vehicleType?.uppercase(Locale.US).orEmpty()
        val lineText = details.lineNameText()

        android.util.Log.d("FareRepository", "=== detectTransitType DEBUG ===")
        android.util.Log.d("FareRepository", "vehicleType: '$vehicleType'")
        android.util.Log.d("FareRepository", "lineText: '$lineText'")
        android.util.Log.d("FareRepository", "lineName: '${details.lineName}'")
        android.util.Log.d("FareRepository", "lineShortName: '${details.lineShortName}'")
        android.util.Log.d("FareRepository", "departureStop: '${details.departureStop}'")
        android.util.Log.d("FareRepository", "arrivalStop: '${details.arrivalStop}'")
        android.util.Log.d("FareRepository", "agencies: ${details.agencies}")
        android.util.Log.d("FareRepository", "vehicleName: '${details.vehicleName}'")

        // PRIORITY 1: Check station names first - most reliable for Bangkok
        val departureStopLower = details.departureStop.lowercase(Locale.US)
        val arrivalStopLower = details.arrivalStop.lowercase(Locale.US)
        val stationMatch = btsStations.any { station ->
            departureStopLower.contains(station) || arrivalStopLower.contains(station)
        }
        if (stationMatch) {
            android.util.Log.d("FareRepository", "→ Classified as BTS (station name match)")
            return TransitType.BTS
        }

        // PRIORITY 2: BTS vehicle types and line names
        // BTS: Google returns TRAM/MONORAIL for Skytrain, or the line name contains "BTS".
        if (vehicleType in setOf("TRAM", "MONORAIL") || lineText.contains("bts")) {
            android.util.Log.d("FareRepository", "→ Classified as BTS (TRAM/MONORAIL or lineText contains bts)")
            return TransitType.BTS
        }

        // PRIORITY 3: MRT vehicle types and line names
        // MRT: subway-class vehicles or line names that explicitly reference MRT.
        if (vehicleType in setOf("HEAVY_RAIL", "SUBWAY", "METRO_RAIL") || lineText.contains("mrt")) {
            android.util.Log.d("FareRepository", "→ Classified as MRT (HEAVY_RAIL/SUBWAY/METRO_RAIL or lineText contains mrt)")
            return TransitType.MRT
        }

        // PRIORITY 4: Airport Rail Link
        // Airport Rail Link — bucket with BTS for fare display.
        if (vehicleType in setOf("COMMUTER_TRAIN", "RAIL")) {
            android.util.Log.d("FareRepository", "→ Classified as BTS (ARL - COMMUTER_TRAIN/RAIL)")
            return TransitType.BTS
        }

        if (vehicleType == "BUS") {
            android.util.Log.d("FareRepository", "→ Classified as UNKNOWN (BUS)")
            return TransitType.UNKNOWN
        }

        // PRIORITY 5: Agency matching
        val agencyText = details.agencies.joinToString(" ").lowercase(Locale.US)
        android.util.Log.d("FareRepository", "agencyText: '$agencyText'")
        when {
            agencyText.contains("bts") ||
                agencyText.contains("bangkok mass transit system") ||
                agencyText.contains("krungthep thanakom") ||
                agencyText.contains("state railway") ||
                agencyText.contains("srtet") ||
                agencyText.contains("airport rail link") -> {
                android.util.Log.d("FareRepository", "→ Classified as BTS (agency match)")
                return TransitType.BTS
            }
            agencyText.contains("mrta") ||
                agencyText.contains("metropolitan rapid transit") ||
                agencyText.contains("northern bangkok monorail") ||
                agencyText.contains("eastern bangkok monorail") -> {
                android.util.Log.d("FareRepository", "→ Classified as MRT (agency match)")
                return TransitType.MRT
            }
        }

        // PRIORITY 6: Keyword matching in all text
        val searchableText = details.searchableText()
        android.util.Log.d("FareRepository", "searchableText: '$searchableText'")
        if (btsKeywords.any { searchableText.contains(it) }) {
            android.util.Log.d("FareRepository", "→ Classified as BTS (keyword match)")
            return TransitType.BTS
        }
        if (mrtKeywords.any { searchableText.contains(it) }) {
            android.util.Log.d("FareRepository", "→ Classified as MRT (keyword match)")
            return TransitType.MRT
        }
        android.util.Log.d("FareRepository", "→ Classified as UNKNOWN (no match)")
        return TransitType.UNKNOWN
    }

    fun calculateFare(type: TransitType, stopCount: Int): Int {
        if (stopCount <= 0) return 0
        return when (type) {
            TransitType.BTS -> btsFareTable[stopCount] ?: BTS_FARE_CAP
            TransitType.MRT -> mrtBlueFareTable[stopCount] ?: MRT_BLUE_FARE_CAP
            TransitType.UNKNOWN -> 0
        }
    }

    private fun calculateFare(details: TransitDetails, stopCount: Int): Int? {
        if (stopCount < 0) return null
        return when (detectTransitType(details)) {
            TransitType.BTS -> if (stopCount > 0) btsFareTable[stopCount] ?: BTS_FARE_CAP else null
            TransitType.MRT -> calculateMrtFare(detectMrtLine(details), stopCount)
            TransitType.UNKNOWN -> null
        }
    }

    private fun calculateMrtFare(line: MrtLine, stopCount: Int): Int {
        return when (line) {
            MrtLine.PINK -> mrtPinkFareTable[stopCount] ?: MRT_PINK_FARE_CAP
            MrtLine.YELLOW -> mrtYellowFareTable[stopCount] ?: MRT_YELLOW_FARE_CAP
            MrtLine.BLUE,
            MrtLine.PURPLE,
            MrtLine.UNKNOWN -> mrtBlueFareTable[stopCount] ?: MRT_BLUE_FARE_CAP
        }
    }

    private fun detectMrtLine(details: TransitDetails): MrtLine {
        val text = details.searchableText()
        return when {
            text.contains("pink") || text.contains("pk") -> MrtLine.PINK
            text.contains("yellow") || text.contains("yl") -> MrtLine.YELLOW
            text.contains("purple") -> MrtLine.PURPLE
            text.contains("blue") -> MrtLine.BLUE
            else -> MrtLine.UNKNOWN
        }
    }

    private fun TransitDetails.lineNameText(): String {
        return listOfNotNull(lineName, lineShortName)
            .joinToString(" ")
            .lowercase(Locale.US)
    }

    private fun TransitDetails.searchableText(): String {
        return listOfNotNull(
            vehicleType,
            vehicleName,
            lineName,
            lineShortName,
            departureStop,
            arrivalStop,
        ).plus(agencies)
            .joinToString(" ")
            .lowercase(Locale.US)
    }

    private fun List<FareSegment>.sumKnownFares(): Int? {
        if (isEmpty()) return null
        val fares = mapNotNull { it.fare }
        return fares.takeIf { it.isNotEmpty() }?.sum()
    }
}

data class TransitFareSummary(
    val btsFareBaht: Int?,
    val mrtFareBaht: Int?,
    val totalFareBaht: Int?,
    val btsOriginStation: String?,
    val btsDestinationStation: String?,
    val mrtOriginStation: String?,
    val mrtDestinationStation: String?,
) {
    val hasBts: Boolean = btsOriginStation != null || btsDestinationStation != null
    val hasMrt: Boolean = mrtOriginStation != null || mrtDestinationStation != null
}

private data class FareSegment(
    val originStation: String,
    val destinationStation: String,
    val stopCount: Int,
    val fare: Int?,
)
