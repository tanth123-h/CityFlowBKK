package com.example.cityflowbkk.features.home

import org.junit.Assert.assertEquals
import org.junit.Test

class RecommendationEngineTest {
    @Test
    fun `recommendations put requested category first and preserve other destinations`() {
        val places = listOf(
            BangkokPlace(
                name = "Wat Arun",
                nearestStation = "Sanam Chai MRT",
                primaryCategory = Category.CULTURE,
                imageRes = 1,
                travelNotice = "Dress modestly before entering temple grounds.",
            ),
            BangkokPlace(
                name = "ICONSIAM",
                nearestStation = "Charoen Nakhon BTS",
                primaryCategory = Category.SHOPPING,
                imageRes = 2,
                travelNotice = "Riverside piers get busy around sunset.",
            ),
            BangkokPlace(
                name = "Yaowarat",
                nearestStation = "Wat Mangkon MRT",
                primaryCategory = Category.FOODIE,
                imageRes = 3,
                travelNotice = "Evening food stalls are busiest after 7 PM.",
            ),
        )

        val result = RecommendationEngine.recommend(
            places = places,
            preferredCategory = Category.FOODIE,
        )

        assertEquals("Yaowarat", result.first().name)
        assertEquals(places.map { it.name }.toSet(), result.map { it.name }.toSet())
    }
}
