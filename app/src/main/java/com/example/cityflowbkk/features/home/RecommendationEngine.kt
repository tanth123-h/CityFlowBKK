package com.example.cityflowbkk.features.home

object RecommendationEngine {
    fun recommend(
        places: List<BangkokPlace>,
        preferredCategory: Category,
    ): List<BangkokPlace> {
        return places.sortedBy { place ->
            if (place.primaryCategory == preferredCategory) 0 else 1
        }
    }
}
