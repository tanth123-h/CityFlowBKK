package com.example.cityflowbkk.features.route

import com.example.cityflowbkk.features.map.MapLatLng
import com.example.cityflowbkk.features.map.NavigationStepUiModel
import com.example.cityflowbkk.features.map.RouteTransportType

class TransitArrivalAlertTracker {
    private val alertedSegmentIds = mutableSetOf<String>()

    fun reset() {
        alertedSegmentIds.clear()
    }

    fun arrivalAlert(
        location: MapLatLng,
        steps: List<NavigationStepUiModel>,
        activeStepIndex: Int?,
        thresholdMeters: Int,
    ): TransitArrivalAlert? {
        val activeStep = steps.firstOrNull { it.index == activeStepIndex } ?: return null
        if (!activeStep.transportType.supportsArrivalAlerts()) return null

        val transitDetails = activeStep.transitDetails ?: return null
        val arrivalLocation = transitDetails.arrivalLocation ?: return null
        val arrivalStation = transitDetails.arrivalStop.takeIf { it.isNotBlank() } ?: return null
        val segmentId = activeStep.arrivalAlertSegmentId(arrivalStation, arrivalLocation)
        if (segmentId in alertedSegmentIds) return null

        val distanceMeters = NavigationTracker.distanceMeters(location, arrivalLocation)
        if (distanceMeters > thresholdMeters) return null

        alertedSegmentIds += segmentId
        return TransitArrivalAlert(
            stationName = arrivalStation,
            segmentId = segmentId,
            distanceMeters = distanceMeters,
        )
    }

    private fun NavigationStepUiModel.arrivalAlertSegmentId(
        stationName: String,
        arrivalLocation: MapLatLng,
    ): String {
        return listOf(
            index.toString(),
            transportType.name,
            stationName,
            "%.6f".format(arrivalLocation.latitude),
            "%.6f".format(arrivalLocation.longitude),
        ).joinToString(":")
    }

    private fun RouteTransportType.supportsArrivalAlerts(): Boolean {
        return when (this) {
            RouteTransportType.BTS_SUKHUMVIT,
            RouteTransportType.BTS_SILOM,
            RouteTransportType.MRT_BLUE,
            RouteTransportType.MRT_PURPLE,
            RouteTransportType.AIRPORT_RAIL_LINK -> true
            RouteTransportType.WALKING,
            RouteTransportType.DRIVING,
            RouteTransportType.BUS,
            RouteTransportType.UNKNOWN_TRANSIT -> false
        }
    }
}

data class TransitArrivalAlert(
    val stationName: String,
    val segmentId: String,
    val distanceMeters: Double,
)
