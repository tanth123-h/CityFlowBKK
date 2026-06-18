package com.example.cityflowbkk.features.route

import com.example.cityflowbkk.features.map.MapLatLng
import com.example.cityflowbkk.features.map.NavigationStepUiModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object NavigationTracker {
    const val OFF_ROUTE_THRESHOLD_METERS = 120.0
    private const val STEP_ENDPOINT_THRESHOLD_METERS = 45.0

    fun activeStepIndex(
        location: MapLatLng,
        steps: List<NavigationStepUiModel>,
        previousIndex: Int?,
    ): Int? {
        if (steps.isEmpty()) return null

        val startIndex = previousIndex?.coerceIn(0, steps.lastIndex) ?: 0
        val candidateRange = startIndex..min(steps.lastIndex, startIndex + 2)
        val nearby = candidateRange.minByOrNull { index ->
            distanceToStepMeters(location, steps[index])
        }

        val previous = previousIndex?.let { steps.getOrNull(it) }
        if (previous != null && distanceMeters(location, previous.endLocation) <= STEP_ENDPOINT_THRESHOLD_METERS) {
            return min(steps.lastIndex, previousIndex + 1)
        }

        return nearby ?: steps.indices.minByOrNull { index -> distanceToStepMeters(location, steps[index]) }
    }

    fun isOffRoute(
        location: MapLatLng,
        steps: List<NavigationStepUiModel>,
    ): Boolean {
        if (steps.isEmpty()) return false
        val nearestDistance = steps.minOf { distanceToStepMeters(location, it) }
        return nearestDistance > OFF_ROUTE_THRESHOLD_METERS
    }

    private fun distanceToStepMeters(
        location: MapLatLng,
        step: NavigationStepUiModel,
    ): Double {
        val points = step.points.ifEmpty { listOf(step.startLocation, step.endLocation) }
        if (points.size == 1) return distanceMeters(location, points.first())

        return points.windowed(2).minOf { pair ->
            distanceToSegmentMeters(location, pair[0], pair[1])
        }
    }

    private fun distanceToSegmentMeters(
        point: MapLatLng,
        start: MapLatLng,
        end: MapLatLng,
    ): Double {
        val referenceLat = Math.toRadians(point.latitude)
        val metersPerDegreeLat = 111_320.0
        val metersPerDegreeLng = metersPerDegreeLat * cos(referenceLat)

        val px = point.longitude * metersPerDegreeLng
        val py = point.latitude * metersPerDegreeLat
        val ax = start.longitude * metersPerDegreeLng
        val ay = start.latitude * metersPerDegreeLat
        val bx = end.longitude * metersPerDegreeLng
        val by = end.latitude * metersPerDegreeLat

        val dx = bx - ax
        val dy = by - ay
        if (dx == 0.0 && dy == 0.0) return sqrt((px - ax).pow(2) + (py - ay).pow(2))

        val t = max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)))
        val nearestX = ax + t * dx
        val nearestY = ay + t * dy
        return sqrt((px - nearestX).pow(2) + (py - nearestY).pow(2))
    }

    fun distanceMeters(
        start: MapLatLng,
        end: MapLatLng,
    ): Double {
        val earthRadiusMeters = 6_371_000.0
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val deltaLat = Math.toRadians(end.latitude - start.latitude)
        val deltaLng = Math.toRadians(end.longitude - start.longitude)

        val a = sin(deltaLat / 2).pow(2) +
            cos(lat1) * cos(lat2) * sin(deltaLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMeters * c
    }
}
