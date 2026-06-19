package com.example.cityflowbkk.features.btsmap.data

import android.content.Context
import com.example.cityflowbkk.features.stationmapping.model.StationLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Loads BTS Group fare data from assets/fare_table.json.
 *
 * The JSON has per-network sections: coreNetwork, extensionNetwork,
 * goldLine, yellowLine, pinkLine. Each section has a fareSteps array
 * mapping station count → fare THB, or a flatFare for Gold Line.
 *
 * Falls back to [FareCalculator] if the file hasn't been loaded or a
 * step is missing.
 *
 * Usage:
 *   FareRepository.init(context)               // once, from ViewModel.init
 *   FareRepository.getFare(hops, line)         // any thread, after init
 *   FareRepository.estimateTime(hops, transfers)
 */
object FareRepository {

    // Per-network fareStep arrays: index = station count, value = fare THB
    private val coreSteps      = mutableListOf<Int>()
    private val extensionSteps = mutableListOf<Int>()
    private val yellowSteps    = mutableListOf<Int>()
    private val pinkSteps      = mutableListOf<Int>()
    private var goldFlatFare   = 17
    private var crossNetworkCap = 65

    @Volatile private var isLoaded = false

    // ── init ──────────────────────────────────────────────────────────────────

    suspend fun init(context: Context) = withContext(Dispatchers.IO) {
        try {
            val json = context.assets.open("fare_table.json")
                .bufferedReader().use { it.readText() }
            val root = JSONObject(json)

            coreSteps.clear()
            extensionSteps.clear()
            yellowSteps.clear()
            pinkSteps.clear()

            parseSteps(root.optJSONObject("coreNetwork"),      coreSteps)
            parseSteps(root.optJSONObject("extensionNetwork"), extensionSteps)
            parseSteps(root.optJSONObject("yellowLine"),       yellowSteps)
            parseSteps(root.optJSONObject("pinkLine"),         pinkSteps)

            root.optJSONObject("goldLine")?.let { goldFlatFare = it.optInt("flatFare", 17) }
            root.optJSONObject("extensionNetwork")?.let {
                crossNetworkCap = it.optInt("crossNetworkCap", 65)
            }

            isLoaded = true
            android.util.Log.d("FareRepository",
                "Loaded: core=${coreSteps.size} ext=${extensionSteps.size} " +
                "yellow=${yellowSteps.size} pink=${pinkSteps.size} gold=$goldFlatFare")
        } catch (e: Exception) {
            android.util.Log.e("FareRepository", "Failed to load fare_table.json", e)
            isLoaded = false
        }
    }

    private fun parseSteps(obj: JSONObject?, target: MutableList<Int>) {
        obj ?: return
        val arr = obj.optJSONArray("fareSteps") ?: return
        for (i in 0 until arr.length()) {
            val step     = arr.getJSONObject(i)
            val stations = step.getInt("stations")
            val fare     = step.getInt("fare")
            while (target.size <= stations) target.add(0)
            target[stations] = fare
        }
    }

    // ── public API ────────────────────────────────────────────────────────────

    /**
     * Return the fare in THB for [hopCount] station hops on [line].
     * Returns null when fare data is unavailable.
     */
    fun getFare(hopCount: Int, line: StationLine = StationLine.SUKHUMVIT): Int? {
        if (hopCount < 0) return null
        if (hopCount == 0) return 0

        if (!isLoaded) return FareCalculator.calculate(hopCount, line)

        return when (line) {
            StationLine.GOLD       -> goldFlatFare
            StationLine.MRT_YELLOW -> lookupList(hopCount, yellowSteps)
                ?: FareCalculator.calculate(hopCount, line)
            StationLine.MRT_PINK   -> lookupList(hopCount, pinkSteps)
                ?: FareCalculator.calculate(hopCount, line)
            StationLine.BTS_EXTENSION -> lookupList(hopCount, extensionSteps)
                ?: FareCalculator.calculate(hopCount, line)
            else -> lookupList(hopCount, coreSteps)
                ?: FareCalculator.calculate(hopCount, line)
        }
    }

    /** Convenience: returns fare label string or "Fare unavailable". */
    fun fareLabel(hopCount: Int, line: StationLine = StationLine.SUKHUMVIT): String {
        val fare = getFare(hopCount, line) ?: return "Fare unavailable"
        if (fare == 0) return "Free"
        return "$fare THB"
    }

    /**
     * Estimate travel time in minutes.
     * Rule: 2 min/hop + 3 min/transfer.
     */
    fun estimateTime(hopCount: Int, transferCount: Int = 0): Int =
        FareCalculator.estimateDuration(hopCount, transferCount)

    fun isReady(): Boolean = isLoaded

    // ── internal ──────────────────────────────────────────────────────────────

    private fun lookupList(hops: Int, steps: List<Int>): Int? {
        if (steps.isEmpty()) return null
        return if (hops < steps.size) {
            val v = steps[hops]
            if (v == 0) null else v          // 0 means unpopulated slot
        } else {
            steps.lastOrNull { it > 0 }      // cap at highest defined value
        }
    }
}
