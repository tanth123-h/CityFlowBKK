package com.example.cityflowbkk.features.btsmap.data

import com.example.cityflowbkk.features.btsmap.model.RouteResult
import com.example.cityflowbkk.features.stationmapping.model.StationCoordinate

/**
 * RouteRepository
 *
 * Public facade for route-related queries.
 * Delegates all real work to [RouteGraphRepository] (Dijkstra graph) and
 * [FareCalculator] / [DurationCalculator].
 *
 * The old heuristic (comparing numeric parts of station codes) has been
 * replaced with a proper shortest-path result from the graph.
 *
 * Usage:
 *   1. Make sure [RouteGraphRepository.init(context)] has been called once
 *      (done in BTSMapViewModel.init).
 *   2. Call [calculateRoute] with two [StationCoordinate] objects.
 *   3. Returns a [RouteResult] with the full path, fare, and duration.
 */
class RouteRepository {

    /**
     * Find the shortest route (by travel time) between [origin] and [destination].
     *
     * Returns null when:
     *  - The graph has not been initialised yet.
     *  - Either station ID is not found in the graph.
     *  - No connecting path exists.
     */
    fun calculateRoute(
        origin: StationCoordinate,
        destination: StationCoordinate
    ): RouteResult? {
        return RouteGraphRepository.findShortestPath(
            originId      = origin.stationId,
            destinationId = destination.stationId
        )
    }

    /**
     * Convenience overload that accepts station IDs directly.
     */
    fun calculateRoute(originId: String, destinationId: String): RouteResult? {
        return RouteGraphRepository.findShortestPath(originId, destinationId)
    }

    /**
     * Quick fare-only estimate without a full path query.
     * Useful for showing a preview before the route is confirmed.
     *
     * Falls back to [FareCalculator] using hop count derived from the graph
     * path length, or an approximation if the graph path is unavailable.
     */
    fun estimateFare(origin: StationCoordinate, destination: StationCoordinate): Int {
        val result = RouteGraphRepository.findShortestPath(
            origin.stationId, destination.stationId
        )
        return result?.totalFare
            ?: FareCalculator.calculate(approximateHops(origin, destination))
    }

    /**
     * Quick duration-only estimate (in minutes) without a full path query.
     */
    fun estimateDuration(origin: StationCoordinate, destination: StationCoordinate): Int {
        val result = RouteGraphRepository.findShortestPath(
            origin.stationId, destination.stationId
        )
        return result?.totalDuration
            ?: DurationCalculator.calculate(approximateHops(origin, destination))
    }

    // ── fallback when graph is not ready ─────────────────────────────────────

    /**
     * Very rough hop count estimate used only as a last resort fallback.
     * Compares numeric parts of station codes on the same line,
     * or returns a fixed cross-line estimate.
     */
    private fun approximateHops(a: StationCoordinate, b: StationCoordinate): Int {
        if (a.line == b.line) {
            val ai = a.stationId.filter { it.isDigit() }.toIntOrNull() ?: 0
            val bi = b.stationId.filter { it.isDigit() }.toIntOrNull() ?: 0
            val diff = kotlin.math.abs(ai - bi)
            return if (diff == 0) 1 else diff
        }
        return 12 // cross-line default
    }
}
