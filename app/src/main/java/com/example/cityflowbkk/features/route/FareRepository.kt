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

        transitSegments.forEach { segment ->
            val details = segment.transitDetails ?: return@forEach
            val fareSegment = FareSegment(
                originStation = details.departureStop,
                destinationStation = details.arrivalStop,
                stopCount = details.numStops,
                fare = calculateFare(details, details.numStops),
            )

            when (detectTransitType(details)) {
                TransitType.BTS -> btsSegments += fareSegment
                TransitType.MRT -> mrtSegments += fareSegment
                TransitType.UNKNOWN -> Unit
            }
        }

        val btsFare = btsSegments.sumKnownFares()
        val mrtFare = mrtSegments.sumKnownFares()
        val totalFare = listOfNotNull(btsFare, mrtFare)
            .takeIf { it.isNotEmpty() }
            ?.sum()

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
        // Step 1 — structured vehicle type (most reliable)
        val vehicleType = details.vehicleType?.uppercase(Locale.US).orEmpty()
        when {
            vehicleType in setOf("TRAM", "MONORAIL") -> return TransitType.BTS
            vehicleType in setOf("HEAVY_RAIL", "SUBWAY", "METRO_RAIL") -> return TransitType.MRT
            vehicleType == "BUS" -> return TransitType.UNKNOWN
        }

        // Step 2 — agency name (second most reliable)
        val agencyText = details.agencies.joinToString(" ").lowercase(Locale.US)
        when {
            agencyText.contains("bts") ||
                agencyText.contains("bangkok mass transit system") ||
                agencyText.contains("krungthep thanakom") -> return TransitType.BTS
            agencyText.contains("mrta") ||
                agencyText.contains("metropolitan rapid transit") ||
                agencyText.contains("northern bangkok monorail") ||
                agencyText.contains("eastern bangkok monorail") -> return TransitType.MRT
        }

        // Step 3 — fall back to keyword matching on line/stop names
        val searchableText = details.searchableText()
        if (btsKeywords.any { searchableText.contains(it) }) return TransitType.BTS
        if (mrtKeywords.any { searchableText.contains(it) }) return TransitType.MRT
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
