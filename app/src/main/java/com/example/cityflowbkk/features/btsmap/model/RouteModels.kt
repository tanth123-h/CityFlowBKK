package com.example.cityflowbkk.features.btsmap.model

/**
 * Flat summary of a trip, used by the UI bottom card.
 *
 * Can be constructed directly or converted from a [RouteResult] via [from].
 */
data class TripResult(
    val originName: String,
    val destinationName: String,
    val fare: Int,
    val durationMinutes: Int,
    val stationCount: Int,
    val routeName: String = "",
    /** Ordered station names along the route (empty for legacy usage). */
    val stationNames: List<String> = emptyList(),
    val hasTransfer: Boolean = false
) {
    companion object {
        /** Convert a graph [RouteResult] into the flat [TripResult] the UI expects. */
        fun from(result: RouteResult): TripResult = TripResult(
            originName      = result.originName,
            destinationName = result.destinationName,
            fare            = result.totalFare,
            durationMinutes = result.totalDuration,
            stationCount    = result.stationCount,
            routeName       = result.lineSegments.joinToString(" → "),
            stationNames    = result.path.map { it.nameEn },
            hasTransfer     = result.hasTransfer
        )
    }
}
