package com.example.cityflowbkk.features.route

import com.example.cityflowbkk.features.map.MapLatLng
import com.example.cityflowbkk.features.map.NavigationStepUiModel
import com.example.cityflowbkk.features.map.RouteTransportType
import com.example.cityflowbkk.features.map.TransitDetails
import com.example.cityflowbkk.features.map.TravelMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TransitArrivalAlertTrackerTest {
    @Test
    fun arrivalAlertTriggersOnceForSupportedTransitSegmentWithinThreshold() {
        val tracker = TransitArrivalAlertTracker()
        val arrivalLocation = MapLatLng(latitude = 13.7456, longitude = 100.5345)
        val step = transitStep(arrivalLocation = arrivalLocation)

        val firstAlert = tracker.arrivalAlert(
            location = MapLatLng(latitude = 13.7457, longitude = 100.5345),
            steps = listOf(step),
            activeStepIndex = step.index,
            thresholdMeters = 100,
        )
        val secondAlert = tracker.arrivalAlert(
            location = MapLatLng(latitude = 13.7457, longitude = 100.5345),
            steps = listOf(step),
            activeStepIndex = step.index,
            thresholdMeters = 100,
        )

        assertNotNull(firstAlert)
        assertEquals("Siam", firstAlert?.stationName)
        assertNull(secondAlert)
    }

    @Test
    fun arrivalAlertIgnoresUnsupportedTransitTypes() {
        val tracker = TransitArrivalAlertTracker()
        val step = transitStep(
            arrivalLocation = MapLatLng(latitude = 13.7456, longitude = 100.5345),
            transportType = RouteTransportType.BUS,
        )

        val alert = tracker.arrivalAlert(
            location = MapLatLng(latitude = 13.7457, longitude = 100.5345),
            steps = listOf(step),
            activeStepIndex = step.index,
            thresholdMeters = 100,
        )

        assertNull(alert)
    }

    private fun transitStep(
        arrivalLocation: MapLatLng,
        transportType: RouteTransportType = RouteTransportType.BTS_SUKHUMVIT,
    ): NavigationStepUiModel {
        return NavigationStepUiModel(
            index = 2,
            instruction = "Exit at Siam",
            distanceText = "1 km",
            distanceMeters = 1000,
            durationText = "4 min",
            durationSeconds = 240,
            startLocation = MapLatLng(latitude = 13.7370, longitude = 100.5600),
            endLocation = arrivalLocation,
            points = listOf(
                MapLatLng(latitude = 13.7370, longitude = 100.5600),
                arrivalLocation,
            ),
            travelMode = TravelMode.TRANSIT,
            transportType = transportType,
            transitDetails = TransitDetails(
                lineName = "BTS Sukhumvit Line",
                lineShortName = "BTS",
                lineColor = null,
                departureStop = "Phrom Phong",
                arrivalStop = "Siam",
                departureLocation = MapLatLng(latitude = 13.7302, longitude = 100.5697),
                arrivalLocation = arrivalLocation,
                numStops = 4,
                vehicleType = "SUBWAY",
            ),
        )
    }
}
