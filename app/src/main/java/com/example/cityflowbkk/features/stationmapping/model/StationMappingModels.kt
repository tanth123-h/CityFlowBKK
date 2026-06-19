package com.example.cityflowbkk.features.stationmapping.model

import androidx.compose.ui.graphics.Color

enum class StationLine(val displayName: String) {
    SUKHUMVIT("Sukhumvit Line"),
    SILOM("Silom Line"),
    GOLD("Gold Line"),
    AIRPORT_RAIL_LINK("Airport Rail Link"),
    MRT_BLUE("MRT Blue Line"),
    MRT_PURPLE("MRT Purple Line"),
    MRT_YELLOW("MRT Yellow Line"),
    MRT_PINK("MRT Pink Line"),
    BTS_EXTENSION("BTS Extension")
}

data class BtsStation(
    val id: String,
    val name: String,
    val line: StationLine
)

data class StationCoordinate(
    val stationId: String,
    val stationName: String,
    val line: StationLine,
    val x: Float, // Normalised 0..1
    val y: Float, // Normalised 0..1
    val absX: Int = 0, // Absolute pixel X (from JSON)
    val absY: Int = 0, // Absolute pixel Y (from JSON)
    val mappedX: Int = absX, // Calibrated pixel X (used for rendering)
    val mappedY: Int = absY  // Calibrated pixel Y (used for rendering)
)
