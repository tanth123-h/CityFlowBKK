package com.example.cityflowbkk.features.btsmap.data

/**
 * Travel-time estimator for BTS Skytrain journeys.
 *
 * Averages used:
 *   - Average speed between stations : ~50 km/h
 *   - Average station dwell time     : ~1 minute
 *   - Interchange walking penalty    : ~4 minutes per transfer
 *
 * For a single hop we estimate duration from the pixel distance stored on
 * each RouteEdge (already converted to km).  When distance is unknown we
 * fall back to 2 minutes per hop.
 */
object DurationCalculator {

    private const val DWELL_SECONDS_PER_STATION = 60          // 1 min stop
    private const val SPEED_KM_PER_HOUR         = 50.0
    private const val TRANSFER_PENALTY_MIN       = 4           // walk + wait

    /**
     * Estimate travel time in **whole minutes** for a journey of [hopCount]
     * hops covering [distanceKm] kilometres with [transferCount] line changes.
     *
     * If [distanceKm] is 0f the function falls back to 2 min/hop.
     */
    fun calculate(
        hopCount: Int,
        distanceKm: Float = 0f,
        transferCount: Int = 0
    ): Int {
        if (hopCount <= 0) return 0

        val runMinutes = if (distanceKm > 0f) {
            // time = distance / speed, converted to minutes
            ((distanceKm / SPEED_KM_PER_HOUR) * 60).toInt()
        } else {
            hopCount * 2          // fallback: 2 min per hop
        }

        val dwellMinutes   = hopCount * (DWELL_SECONDS_PER_STATION / 60)
        val transferMinutes = transferCount * TRANSFER_PENALTY_MIN

        return (runMinutes + dwellMinutes + transferMinutes).coerceAtLeast(1)
    }

    /**
     * Estimate duration for a single RouteEdge hop.
     */
    fun forEdge(distanceKm: Float, isTransfer: Boolean): Int {
        val run = if (distanceKm > 0f) {
            ((distanceKm / SPEED_KM_PER_HOUR) * 60).toInt().coerceAtLeast(1)
        } else 2
        return if (isTransfer) run + TRANSFER_PENALTY_MIN else run + 1 // +1 dwell
    }
}
