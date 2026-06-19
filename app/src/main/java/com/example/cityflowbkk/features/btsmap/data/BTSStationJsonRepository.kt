package com.example.cityflowbkk.features.btsmap.data

import android.content.Context
import com.example.cityflowbkk.features.stationmapping.model.StationCoordinate
import com.example.cityflowbkk.features.stationmapping.model.StationLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * Loads BTS station data from assets/stations.json.
 *
 * Station positions are overridden by VERIFIED_COORDS -- pixel coordinates
 * extracted from BTS_CAL_RESULT logs after manual drag-calibration on the
 * real btsmap.jpg image. These are the single source of truth.
 * No runtime calibration file or scale/offset math is applied.
 */
object BTSStationJsonRepository {

 /**
 * Verified pixel coordinates on btsmap.jpg.
 * Source: BTS_CAL_RESULT logcat "target" values, 2026-06-19.
 * Key = stationCode, Value = Pair(x, y) in image pixels.
 */
 private val VERIFIED_COORDS: Map<String, Pair<Int, Int>> = mapOf(
 // Sukhumvit line
 "E1" to Pair(2651, 2863),
 "E2" to Pair(2785, 2863),
 "E3" to Pair(2928, 2863),
 "E4" to Pair(3073, 2864),
 "E5" to Pair(3199, 2863),
 "E6" to Pair(3313, 2933),
 "E7" to Pair(3393, 3014),
 "E8" to Pair(3473, 3094),
 "E9" to Pair(3553, 3175),
 "E10" to Pair(3634, 3265),
 "E11" to Pair(3676, 3361),
 "E12" to Pair(3676, 3482),
 "E13" to Pair(3676, 3603),
 "E14" to Pair(3676, 3724),
 "E15" to Pair(3676, 3844),
 "E16" to Pair(3676, 3961),
 "E17" to Pair(3676, 4086),
 "E18" to Pair(3675, 4201),
 "E19" to Pair(3676, 4318),
 "E20" to Pair(3675, 4432),
 "E21" to Pair(3676, 4538),
 "E22" to Pair(3676, 4654),
 "E23" to Pair(3801, 4768),
 // Central interchange
 "CEN" to Pair(2506, 2879),
 // North extension
 "N1" to Pair(2465, 2695),
 "N2" to Pair(2465, 2503),
 "N3" to Pair(2465, 2395),
 "N4" to Pair(2464, 2305),
 "N5" to Pair(2464, 2216),
 // N6 absent from calibration log -- falls back to JSON coords
 "N7" to Pair(2464, 2028),
 "N8" to Pair(2464, 1926),
 "N9" to Pair(2467, 1792),
 "N10" to Pair(2464, 1645),
 "N11" to Pair(2465, 1523),
 "N12" to Pair(2465, 1403),
 "N13" to Pair(2464, 1266),
 "N14" to Pair(2465, 1165),
 "N15" to Pair(2464, 1035),
 "N16" to Pair(2464, 926),
 "N17" to Pair(2464, 793),
 "N18" to Pair(2553, 661),
 "N19" to Pair(2642, 582),
 "N20" to Pair(2721, 504),
 "N21" to Pair(2791, 425),
 "N22" to Pair(2880, 345),
 "N23" to Pair(3064, 245),
 "N24" to Pair(3259, 245),
 // West (Silom extension)
 "W1" to Pair(2315, 2886),
 // Silom line
 "S1" to Pair(2545, 3006),
 "S2" to Pair(2545, 3126),
 "S3" to Pair(2546, 3292),
 "S4" to Pair(2516, 3442),
 "S5" to Pair(2456, 3513),
 "S6" to Pair(2385, 3583),
 "S7" to Pair(2185, 3644),
 "S8" to Pair(1964, 3643),
 "S9" to Pair(1747, 3643),
 "S10" to Pair(1521, 3643),
 "S11" to Pair(1281, 3532),
 "S12" to Pair(1103, 3352),
 // Gold line
 "G1" to Pair(2185, 3574),
 "G2" to Pair(2118, 3465),
 "G3" to Pair(1994, 3388),
 )

 suspend fun loadStations(
 context: Context,
 imageWidth: Float,
 imageHeight: Float,
 ): List<StationCoordinate> = withContext(Dispatchers.IO) {
 try {
 val json = context.assets
 .open("stations.json")
 .bufferedReader()
 .use { it.readText() }
 parseStations(json, imageWidth, imageHeight)
 } catch (e: Exception) {
 android.util.Log.e("BTSStationJsonRepository", "Failed to load stations", e)
 emptyList()
 }
 }

 private fun parseStations(
 json: String,
 imageWidth: Float,
 imageHeight: Float,
 ): List<StationCoordinate> {
 val array = JSONArray(json)
 val result = mutableListOf<StationCoordinate>()

 for (i in 0 until array.length()) {
 val obj = array.getJSONObject(i)
 val code = obj.getString("code")
 val nameEn = obj.getString("name_en")
 val lineName = obj.getString("line")
 val jsonX = obj.getInt("x")
 val jsonY = obj.getInt("y")

 val line = parseLineEnum(lineName) ?: continue

 // Use verified coords when available; fall back to JSON for unknown stations.
 val (finalX, finalY) = VERIFIED_COORDS[code] ?: Pair(jsonX, jsonY)
 val verified = VERIFIED_COORDS.containsKey(code)

 val normalizedX = if (imageWidth > 0) finalX / imageWidth else 0f
 val normalizedY = if (imageHeight > 0) finalY / imageHeight else 0f

 result.add(
 StationCoordinate(
 stationId = code,
 stationName = nameEn,
 line = line,
 x = normalizedX,
 y = normalizedY,
 absX = finalX,
 absY = finalY,
 mappedX = finalX,
 mappedY = finalY,
 )
 )

 android.util.Log.d(
 "BTS_MARKER",
 "${code} image=(${finalX},${finalY}) ${if (verified) "[verified]" else "[json fallback]"}"
 )
 }

 val verifiedCount = result.count { VERIFIED_COORDS.containsKey(it.stationId) }
 android.util.Log.d(
 "BTSStationJsonRepository",
 "${result.size} stations ($verifiedCount verified, ${result.size - verifiedCount} fallback)"
 )
 return result
 }

 private fun parseLineEnum(lineName: String): StationLine? = when (lineName) {
 "Sukhumvit" -> StationLine.SUKHUMVIT
 "Silom" -> StationLine.SILOM
 "Gold" -> StationLine.GOLD
 "Airport Rail Link" -> StationLine.AIRPORT_RAIL_LINK
 "MRT Blue" -> StationLine.MRT_BLUE
 "MRT Purple" -> StationLine.MRT_PURPLE
 "MRT Yellow" -> StationLine.MRT_YELLOW
 "MRT Pink" -> StationLine.MRT_PINK
 else -> null
 }
}
