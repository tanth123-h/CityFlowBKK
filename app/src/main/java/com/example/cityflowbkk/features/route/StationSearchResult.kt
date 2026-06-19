package com.example.cityflowbkk.features.route

import com.example.cityflowbkk.features.map.MapLatLng
import com.example.cityflowbkk.features.map.RouteTransportType

/** A single BTS, MRT, or Airport Rail Link station from the local database. */
data class RailStation(
    val name: String,
    val line: RouteTransportType,
    val location: MapLatLng,
) {
    val lineName: String get() = when (line) {
        RouteTransportType.BTS_SUKHUMVIT    -> "BTS Sukhumvit Line"
        RouteTransportType.BTS_SILOM         -> "BTS Silom Line"
        RouteTransportType.MRT_BLUE          -> "MRT Blue Line"
        RouteTransportType.MRT_PURPLE        -> "MRT Purple Line"
        RouteTransportType.AIRPORT_RAIL_LINK -> "Airport Rail Link"
        else                                -> "Transit"
    }
}

/** The nearest origin and destination stations found for a trip. */
data class NearestStationPair(
    val originStation: RailStation,
    val originDistanceMeters: Double,
    val destinationStation: RailStation,
    val destinationDistanceMeters: Double,
)

/** Diagnostic snapshot of a station search — logged and available for debugging. */
data class StationSearchDiagnostics(
    val userLocation: MapLatLng,
    val destinationLocation: MapLatLng,
    val searchRadiusMeters: Double,
    /** All candidate origin stations within radius, sorted by distance ascending. */
    val candidateOriginStations: List<Pair<RailStation, Double>>,
    /** All candidate destination stations within radius, sorted by distance ascending. */
    val candidateDestStations: List<Pair<RailStation, Double>>,
    /** The pair that was ultimately selected, or null if none found. */
    val selectedPair: NearestStationPair?,
)
