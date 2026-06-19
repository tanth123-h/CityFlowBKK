package com.example.cityflowbkk.features.home

import com.example.cityflowbkk.R

object BangkokData {
    val places = listOf(
        BangkokPlace(
            name = "Wat Arun",
            nearestStation = "Sanam Chai MRT",
            primaryCategory = Category.CULTURE,
            imageRes = R.drawable.wat_arun,
            travelNotice = "Temple dress code is enforced: cover shoulders and knees, and expect hot stone courtyards after late morning.",
        ),
        BangkokPlace(
            name = "Grand Palace",
            nearestStation = "Sanam Chai MRT",
            primaryCategory = Category.CULTURE,
            imageRes = R.drawable.grandplace,
            travelNotice = "Arrive early for cooler queues, bring a cover-up, and watch for strict temple dress checks near the gate.",
        ),
        BangkokPlace(
            name = "ICONSIAM",
            nearestStation = "Charoen Nakhon BTS",
            primaryCategory = Category.SHOPPING,
            imageRes = R.drawable.download,
            travelNotice = "Riverside crowds build before sunset; leave buffer time for the Gold Line platform and pier walkways.",
        ),
        BangkokPlace(
            name = "Siam Paragon",
            nearestStation = "Siam BTS",
            primaryCategory = Category.SHOPPING,
            imageRes = R.drawable.siam_paragon,
            travelNotice = "Siam Station gets packed during school rush and mall closing hours, especially around the central interchange stairs.",
        ),
        BangkokPlace(
            name = "Jodd Fairs",
            nearestStation = "Phra Ram 9 MRT",
            primaryCategory = Category.NIGHTLIFE,
            imageRes = R.drawable.jodd_fairs,
            travelNotice = "Go after 6 PM for full food-stall energy, but keep the last train in mind if you stay for the night market crowd.",
        ),
        BangkokPlace(
            name = "Ari Cafe Street",
            nearestStation = "Ari BTS",
            primaryCategory = Category.CAFE,
            imageRes = R.drawable.ari_cafe_street,
            travelNotice = "Ari side streets are humid and sunny at midday; cafe hopping is easier with a small umbrella during rainy season.",
        ),
        BangkokPlace(
            name = "Chatuchak Market",
            nearestStation = "Kamphaeng Phet MRT",
            primaryCategory = Category.SHOPPING,
            imageRes = R.drawable.images,
            travelNotice = "Weekend lanes get extremely crowded and hot by noon; start early and agree on a meeting point before browsing.",
        ),
        BangkokPlace(
            name = "Yaowarat (Chinatown)",
            nearestStation = "Wat Mangkon MRT",
            primaryCategory = Category.FOODIE,
            imageRes = R.drawable.yaowarat_chinatown,
            travelNotice = "Food stalls peak after 7 PM; pavements are tight, so keep bags close and expect short rainy-season flooding near curbs.",
        ),
        BangkokPlace(
            name = "Asiatique",
            nearestStation = "Saphan Taksin BTS",
            primaryCategory = Category.FAMILY,
            imageRes = R.drawable.asiatique,
            travelNotice = "The riverfront breeze helps at night, but ferry queues grow after dinner and last-train timing matters for families.",
        ),
        BangkokPlace(
            name = "Benchakitti Park",
            nearestStation = "Queen Sirikit National Convention Centre MRT",
            primaryCategory = Category.FAMILY,
            imageRes = R.drawable.benchakitti_park,
            travelNotice = "Best visited near sunset; open walkways have little shade, and sudden rain can make the wetland paths slippery.",
        ),
    )
}
