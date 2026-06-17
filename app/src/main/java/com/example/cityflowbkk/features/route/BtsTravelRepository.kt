package com.example.cityflowbkk.features.route

import com.example.cityflowbkk.features.map.MapLatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class BtsTravelRepository {
    fun buildRecommendation(
        origin: MapLatLng,
        destination: MapLatLng,
    ): TravelRecommendationUiModel {
        val originStation = stations.minBy { it.location.distanceMetersTo(origin) }
        val destinationStation = stations.minBy { it.location.distanceMetersTo(destination) }
        val accessMeters = origin.distanceMetersTo(originStation.location)
        val egressMeters = destinationStation.location.distanceMetersTo(destination)
        val accessMinutes = walkingMinutes(accessMeters)
        val egressMinutes = walkingMinutes(egressMeters)
        val stationCount = estimateStationCount(originStation, destinationStation)
        val trainMinutes = estimateTrainMinutes(originStation, destinationStation, stationCount)
        val totalMinutes = accessMinutes + trainMinutes + egressMinutes
        val fare = calculateFare(originStation, destinationStation, stationCount)

        return TravelRecommendationUiModel(
            mode = TravelMode.Bts,
            durationMinutes = totalMinutes.coerceAtLeast(1),
            estimatedCostBaht = fare,
            routeSummary = "${originStation.name} to ${destinationStation.name}",
            instructions = listOf(
                "Walk ${formatDistance(accessMeters)} to ${originStation.name} (${originStation.code}).",
                "Board ${originStation.line.shortName} at ${originStation.name}.",
                if (originStation.line == destinationStation.line) {
                    "Ride about $stationCount stations to ${destinationStation.name}."
                } else {
                    "Transfer as needed within the BTS Group network, then continue to ${destinationStation.name}."
                },
                "Exit at ${destinationStation.name} (${destinationStation.code}).",
                "Walk ${formatDistance(egressMeters)} to your destination.",
            ),
        )
    }

    private fun estimateStationCount(origin: BtsStation, destination: BtsStation): Int {
        if (origin.code == destination.code) return 0
        if (origin.line == destination.line) {
            val originIndex = origin.stationIndex ?: return DEFAULT_MIXED_STATIONS
            val destinationIndex = destination.stationIndex ?: return DEFAULT_MIXED_STATIONS
            return kotlin.math.abs(destinationIndex - originIndex).coerceAtLeast(1)
        }
        return DEFAULT_MIXED_STATIONS
    }

    private fun estimateTrainMinutes(origin: BtsStation, destination: BtsStation, stationCount: Int): Int {
        val baseSeconds = if (origin.line == destination.line) {
            origin.line.interStationSeconds * stationCount + origin.line.offPeakWaitSeconds
        } else {
            ((origin.line.interStationSeconds + destination.line.interStationSeconds) / 2) * stationCount +
                origin.line.offPeakWaitSeconds +
                destination.line.offPeakWaitSeconds +
                TRANSFER_SECONDS
        }
        return (baseSeconds / 60.0).roundToInt().coerceAtLeast(1)
    }

    private fun calculateFare(origin: BtsStation, destination: BtsStation, stationCount: Int): Int {
        if (origin.code == destination.code) return 0
        return when {
            origin.line == BtsLine.Gold && destination.line == BtsLine.Gold -> 17
            origin.line == BtsLine.Yellow && destination.line == BtsLine.Yellow -> monorailFare(stationCount)
            origin.line == BtsLine.Pink && destination.line == BtsLine.Pink -> monorailFare(stationCount)
            origin.line.isGreen && destination.line.isGreen -> greenLineFare(stationCount)
            else -> (greenLineFare(stationCount) + TRANSFER_FARE_BUFFER).coerceAtMost(75)
        }
    }

    private fun greenLineFare(stationCount: Int): Int {
        val fares = listOf(17, 17, 25, 28, 32, 35, 40, 43, 47)
        return fares.getOrElse(stationCount.coerceAtLeast(0)) { 65 }
    }

    private fun monorailFare(stationCount: Int): Int {
        val fares = listOf(15, 15, 18, 21, 24, 27, 30, 33, 36, 39, 42, 45)
        return fares.getOrElse(stationCount.coerceAtLeast(0)) { 45 }
    }

    private fun walkingMinutes(distanceMeters: Double): Int {
        return (distanceMeters / WALKING_METERS_PER_MINUTE).roundToInt().coerceAtLeast(1)
    }

    private fun formatDistance(distanceMeters: Double): String {
        return if (distanceMeters >= 1000) {
            "${"%.1f".format(distanceMeters / 1000.0)} km"
        } else {
            "${distanceMeters.roundToInt()} m"
        }
    }

    private fun MapLatLng.distanceMetersTo(other: MapLatLng): Double {
        val earthRadiusMeters = 6_371_000.0
        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(other.latitude)
        val deltaLat = Math.toRadians(other.latitude - latitude)
        val deltaLng = Math.toRadians(other.longitude - longitude)
        val a = sin(deltaLat / 2).pow(2) +
            cos(lat1) * cos(lat2) * sin(deltaLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMeters * c
    }

    private enum class BtsLine(
        val shortName: String,
        val interStationSeconds: Int,
        val offPeakWaitSeconds: Int,
        val isGreen: Boolean = false,
    ) {
        Sukhumvit("BTS Sukhumvit Line", 120, 180, isGreen = true),
        Silom("BTS Silom Line", 110, 240, isGreen = true),
        Gold("BTS Gold Line", 120, 300),
        Yellow("MRT Yellow Line", 115, 300),
        Pink("MRT Pink Line", 120, 300),
    }

    private data class BtsStation(
        val code: String,
        val name: String,
        val line: BtsLine,
        val latitude: Double,
        val longitude: Double,
        val stationIndex: Int?,
    ) {
        val location: MapLatLng = MapLatLng(latitude, longitude)
    }

    private companion object {
        private const val WALKING_METERS_PER_MINUTE = 75.0
        private const val TRANSFER_SECONDS = 480
        private const val TRANSFER_FARE_BUFFER = 17
        private const val DEFAULT_MIXED_STATIONS = 8

        private val stations = listOf(
            BtsStation("N17", "Wat Phra Sri Mahathat", BtsLine.Sukhumvit, 13.8756, 100.5969, 17),
            BtsStation("N8", "Mo Chit", BtsLine.Sukhumvit, 13.8024, 100.5539, 8),
            BtsStation("CEN", "Siam", BtsLine.Sukhumvit, 13.7444, 100.5341, 0),
            BtsStation("E4", "Asok", BtsLine.Sukhumvit, 13.73726, 100.560319, 4),
            BtsStation("E15", "Samrong", BtsLine.Sukhumvit, 13.647363, 100.596153, 15),
            BtsStation("E23", "Kheha", BtsLine.Sukhumvit, 13.5677, 100.6077, 23),
            BtsStation("W1", "National Stadium", BtsLine.Silom, 13.746514, 100.529108, 1),
            BtsStation("S2", "Sala Daeng", BtsLine.Silom, 13.7285, 100.5343, 2),
            BtsStation("S6", "Saphan Taksin", BtsLine.Silom, 13.7196, 100.5149, 6),
            BtsStation("S7", "Krung Thon Buri", BtsLine.Silom, 13.7209, 100.5039, 7),
            BtsStation("S12", "Bang Wa", BtsLine.Silom, 13.720625, 100.457869, 12),
            BtsStation("G1", "Krung Thon Buri", BtsLine.Gold, 13.7209, 100.5039, 1),
            BtsStation("G2", "Charoen Nakhon", BtsLine.Gold, 13.7271, 100.5108, 2),
            BtsStation("G3", "Khlong San", BtsLine.Gold, 13.7303, 100.5114, 3),
            BtsStation("YL01", "Lat Phrao", BtsLine.Yellow, 13.8036, 100.5739, 1),
            BtsStation("YL17", "Si Iam", BtsLine.Yellow, 13.6681, 100.6644, 17),
            BtsStation("YL23", "Samrong", BtsLine.Yellow, 13.647363, 100.596153, 23),
            BtsStation("PK01", "Nonthaburi Civic Center", BtsLine.Pink, 13.8617, 100.5133, 1),
            BtsStation("PK16", "Wat Phra Sri Mahathat", BtsLine.Pink, 13.8756, 100.5969, 16),
            BtsStation("PK30", "Min Buri", BtsLine.Pink, 13.8129, 100.7208, 30),
            BtsStation("MT01", "Impact Muang Thong Thani", BtsLine.Pink, 13.9104, 100.5443, 31),
            BtsStation("MT02", "Lake Muang Thong Thani", BtsLine.Pink, 13.9125, 100.5401, 32),
        )
    }
}
