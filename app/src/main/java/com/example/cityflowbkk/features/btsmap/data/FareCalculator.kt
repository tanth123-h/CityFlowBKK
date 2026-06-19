package com.example.cityflowbkk.features.btsmap.data

import com.example.cityflowbkk.features.stationmapping.model.StationLine

/**
 * BTS Group fare calculation engine.
 * Source: Bangkok BTS Group Rail Transit Database Specification (effective 2026-01-01).
 *
 * Networks and fare rules:
 *  - Core Network  (BTS Sukhumvit/Silom core concession): 17–47 THB
 *  - Extension     (BTS Green Line extensions): 17–45 THB, cross-network cap 65 THB
 *  - Gold Line     : flat 17 THB
 *  - Yellow Line   : 15–45 THB
 *  - Pink Line     : 15–45 THB
 *
 * Travel time rules (per prompt):
 *  - 2 minutes per station hop
 *  - +3 minutes per interchange/line transfer
 */
object FareCalculator {

    // ── Core Network fareSteps (hops 0-8, capped at 47) ──────────────────────
    private val CORE_STEPS = intArrayOf(17, 17, 25, 28, 32, 35, 40, 43, 47)
    private const val CORE_MAX = 47

    // ── Extension Network fareSteps (hops 0-12, capped at 45) ────────────────
    private val EXT_STEPS  = intArrayOf(17, 17, 19, 21, 23, 25, 28, 31, 34, 37, 40, 43, 45)
    private const val EXT_MAX  = 45
    private const val CROSS_NETWORK_CAP = 65

    // ── Monorail (Yellow / Pink) fareSteps (hops 0-11, capped at 45) ─────────
    private val MONO_STEPS = intArrayOf(15, 15, 18, 21, 24, 27, 30, 33, 36, 39, 42, 45)
    private const val MONO_MAX = 45

    // ── Gold Line flat fare ───────────────────────────────────────────────────
    private const val GOLD_FARE = 17

    // ── Travel time constants ─────────────────────────────────────────────────
    private const val MINS_PER_HOP      = 2
    private const val MINS_PER_TRANSFER = 3

    /**
     * Calculate fare for [hopCount] hops given the dominant [line] of the journey.
     *
     * For cross-network journeys (Sukhumvit↔extension or vice-versa), the
     * combined fare is capped at [CROSS_NETWORK_CAP].
     *
     * Returns null when hopCount < 0 (invalid input).
     */
    fun calculate(hopCount: Int, line: StationLine = StationLine.SUKHUMVIT): Int? {
        if (hopCount < 0) return null
        if (hopCount == 0) return 0
        return when (line) {
            StationLine.SUKHUMVIT,
            StationLine.SILOM,
            StationLine.BTS_EXTENSION -> lookupSteps(hopCount, CORE_STEPS, CORE_MAX)
            StationLine.GOLD          -> GOLD_FARE
            StationLine.MRT_YELLOW    -> lookupSteps(hopCount, MONO_STEPS, MONO_MAX)
            StationLine.MRT_PINK      -> lookupSteps(hopCount, MONO_STEPS, MONO_MAX)
            else                      -> lookupSteps(hopCount, CORE_STEPS, CORE_MAX)
        }
    }

    /**
     * Legacy single-argument overload used by [RouteGraphRepository].
     * Defaults to core network fare.
     */
    fun calculate(hopCount: Int): Int {
        return calculate(hopCount, StationLine.SUKHUMVIT) ?: 0
    }

    /**
     * Estimate travel time in minutes.
     *   totalTime = hops * 2 + transfers * 3
     */
    fun estimateDuration(hopCount: Int, transferCount: Int = 0): Int {
        if (hopCount <= 0) return 0
        return hopCount * MINS_PER_HOP + transferCount * MINS_PER_TRANSFER
    }

    // ── internal helpers ──────────────────────────────────────────────────────

    private fun lookupSteps(hops: Int, steps: IntArray, cap: Int): Int {
        return if (hops < steps.size) steps[hops] else cap
    }

    /** Extension-aware cross-network cap. */
    fun calculateCrossNetwork(coreHops: Int, extHops: Int): Int {
        val coreFare = lookupSteps(coreHops, CORE_STEPS, CORE_MAX)
        val extFare  = lookupSteps(extHops,  EXT_STEPS,  EXT_MAX)
        return (coreFare + extFare).coerceAtMost(CROSS_NETWORK_CAP)
    }

    // Kept for compatibility
    data class Breakdown(
        val hopCount: Int,
        val baseFare: Int,
        val distanceFare: Int,
        val totalFare: Int,
        val isCapped: Boolean
    )

    fun breakdown(hopCount: Int): Breakdown {
        val total = calculate(hopCount) ?: 0
        return Breakdown(
            hopCount     = hopCount,
            baseFare     = if (hopCount > 0) CORE_STEPS[0] else 0,
            distanceFare = (total - (if (hopCount > 0) CORE_STEPS[0] else 0)).coerceAtLeast(0),
            totalFare    = total,
            isCapped     = hopCount >= CORE_STEPS.size
        )
    }
}
