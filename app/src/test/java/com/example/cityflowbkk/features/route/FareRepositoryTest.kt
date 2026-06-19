package com.example.cityflowbkk.features.route

import com.example.cityflowbkk.features.map.TransitDetails
import org.junit.Assert.assertEquals
import org.junit.Test

class FareRepositoryTest {

    @Test
    fun detectTransitType_classifiesTramAsBts() {
        val details = btsDetails(vehicleType = "TRAM")
        assertEquals(FareRepository.TransitType.BTS, FareRepository.detectTransitType(details))
    }

    @Test
    fun detectTransitType_classifiesBtsLineNameAsBtsEvenWithSubwayVehicle() {
        val details = btsDetails(vehicleType = "SUBWAY")
        assertEquals(FareRepository.TransitType.BTS, FareRepository.detectTransitType(details))
    }

    @Test
    fun detectTransitType_classifiesSubwayAsMrtWhenLineIsMrt() {
        val details = TransitDetails(
            lineName = "MRT Blue Line",
            lineShortName = "BL",
            lineColor = null,
            agencies = listOf("MRTA"),
            departureStop = "Sukhumvit",
            arrivalStop = "Silom",
            departureLocation = null,
            arrivalLocation = null,
            numStops = 2,
            vehicleType = "SUBWAY",
            vehicleName = "Metro",
            departureTimeText = null,
            arrivalTimeText = null,
        )
        assertEquals(FareRepository.TransitType.MRT, FareRepository.detectTransitType(details))
    }

    @Test
    fun calculateFareSummary_usesBtsFareMatrixForBtsRoute() {
        val summary = FareRepository.calculateFareSummary(
            listOf(
                transitSegment(
                    TransitDetails(
                        lineName = "BTS Sukhumvit Line",
                        lineShortName = "BTS",
                        lineColor = null,
                        agencies = emptyList(),
                        departureStop = "Phaya Thai",
                        arrivalStop = "Siam",
                        departureLocation = null,
                        arrivalLocation = null,
                        numStops = 2,
                        vehicleType = "TRAM",
                        vehicleName = "Train",
                        departureTimeText = null,
                        arrivalTimeText = null,
                    ),
                ),
            ),
        )

        assertEquals(23, summary.btsFareBaht)
        assertEquals(null, summary.mrtFareBaht)
        assertEquals(23, summary.totalFareBaht)
        assertEquals("Phaya Thai", summary.btsOriginStation)
        assertEquals("Siam", summary.btsDestinationStation)
        assertEquals(null, summary.mrtOriginStation)
    }

    private fun btsDetails(vehicleType: String): TransitDetails {
        return TransitDetails(
            lineName = "BTS Sukhumvit Line",
            lineShortName = "BTS",
            lineColor = null,
            agencies = emptyList(),
            departureStop = "Phaya Thai",
            arrivalStop = "Siam",
            departureLocation = null,
            arrivalLocation = null,
            numStops = 2,
            vehicleType = vehicleType,
            vehicleName = "Train",
            departureTimeText = null,
            arrivalTimeText = null,
        )
    }

    private fun transitSegment(details: TransitDetails) = com.example.cityflowbkk.features.map.RouteSegment(
        index = 0,
        points = emptyList(),
        travelMode = com.example.cityflowbkk.features.map.TravelMode.TRANSIT,
        transportType = com.example.cityflowbkk.features.map.RouteTransportType.BTS_SUKHUMVIT,
        transitDetails = details,
        instruction = "Take BTS",
        distanceText = "1 km",
        distanceMeters = 1000,
        durationText = "5 min",
        durationSeconds = 300,
        startLocation = null,
        endLocation = null,
    )
}
