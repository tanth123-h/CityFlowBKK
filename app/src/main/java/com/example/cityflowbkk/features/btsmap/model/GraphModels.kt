package com.example.cityflowbkk.features.btsmap.model

import com.example.cityflowbkk.features.stationmapping.model.StationLine

/**
 * A node in the BTS route graph.
 *
 * [connections] is populated by RouteGraphRepository after all nodes are created.
 * It maps neighbour station ID → the edge connecting this node to that neighbour.
 */
data class StationNode(
    val id: String,                          // e.g. "N8", "CEN", "S2"
    val nameEn: String,                      // e.g. "Mo Chit"
    val nameTh: String,                      // e.g. "หมอชิต"
    val line: StationLine,
    val stationType: StationNodeType,
    val connectedLines: List<String>,        // raw strings from JSON, e.g. ["MRT Blue"]
    val absX: Int,                           // pixel X on original map image
    val absY: Int,                           // pixel Y on original map image
    val connections: MutableMap<String, RouteEdge> = mutableMapOf()
) {
    /** True when this node is a BTS↔BTS interchange (Siam, Asok via E4/S2, etc.) */
    val isInterchange: Boolean
        get() = stationType == StationNodeType.INTERCHANGE
}

/**
 * A directed edge in the route graph.
 *
 * The graph is undirected in practice — RouteGraphRepository inserts one
 * RouteEdge for each direction so BFS can traverse both ways.
 *
 * [distanceKm]     – straight-line approximation derived from pixel distance.
 * [durationMin]    – travel time in minutes (computed by DurationCalculator).
 * [fare]           – fare contribution of this single hop (0 for transfers).
 * [isTransfer]     – true when crossing between two different lines (e.g. Siam interchange).
 */
data class RouteEdge(
    val fromId: String,
    val toId: String,
    val distanceKm: Float,
    val durationMin: Int,
    val fare: Int,
    val isTransfer: Boolean = false
)

enum class StationNodeType {
    REGULAR,
    TERMINAL,
    INTERCHANGE
}

/**
 * The computed result of a shortest-path query.
 *
 * [path]           – ordered list of StationNodes from origin to destination (inclusive).
 * [edges]          – ordered list of edges traversed (size == path.size - 1).
 * [totalFare]      – total fare in THB.
 * [totalDuration]  – total travel time in minutes.
 * [hasTransfer]    – true when the route crosses at least one interchange.
 * [lineSegments]   – human-readable list of line names the passenger rides.
 */
data class RouteResult(
    val path: List<StationNode>,
    val edges: List<RouteEdge>,
    val totalFare: Int,
    val totalDuration: Int,
    val hasTransfer: Boolean,
    val lineSegments: List<String>
) {
    val stationCount: Int get() = if (path.isEmpty()) 0 else path.size - 1
    val originName: String get() = path.firstOrNull()?.nameEn ?: ""
    val destinationName: String get() = path.lastOrNull()?.nameEn ?: ""
}
