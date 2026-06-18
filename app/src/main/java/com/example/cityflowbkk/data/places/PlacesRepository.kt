package com.example.cityflowbkk.data.places

import android.util.Log
import com.example.cityflowbkk.data.places.model.GooglePlace
import com.example.cityflowbkk.data.places.model.PlaceSearchRequest
import okhttp3.ResponseBody

class PlacesRepository(
    private val service: GooglePlacesService,
    private val apiKey: String
) {
    private val tag = "PlacesRepository"

    suspend fun searchPlace(query: String, fieldMask: String): List<GooglePlace> {
        Log.d(tag, "searchPlace started - Query: '$query'")
        Log.d(tag, "FieldMask: '$fieldMask'")
        try {
            val response = service.searchText(
                apiKey = apiKey,
                fieldMask = fieldMask,
                request = PlaceSearchRequest(textQuery = query)
            )
            Log.d(tag, "searchPlace raw response object: $response")
            val places = response.places
            if (places == null) {
                Log.e(tag, "⚠ response.places is NULL — Gson failed to deserialise 'places' array. Raw response: $response")
                return emptyList()
            }
            Log.d(tag, "searchPlace total results: ${places.size}")

            for ((index, place) in places.withIndex()) {
                Log.d(tag, "Result [$index]:")
                Log.d(tag, "  - Place ID: ${place.id}")
                Log.d(tag, "  - Place Name: ${place.displayName?.text}")
                Log.d(tag, "  - Rating: ${place.rating}")
                Log.d(tag, "  - Review Count: ${place.userRatingCount}")
                Log.d(tag, "  - Photos: ${place.photos?.size ?: 0} returned")
                Log.d(tag, "  - Photo Metadata: ${place.photos?.map { "Name: ${it.name}, Width: ${it.widthPx}, Height: ${it.heightPx}" }}")

                val firstPhotoName = place.photos?.firstOrNull()?.name
                if (firstPhotoName != null) {
                    val url = getPhotoUrl(firstPhotoName, 800)
                    Log.d(tag, "  - Generated Photo URL: $url")
                } else {
                    Log.w(tag, "  - Generated Photo URL: NONE — photos list is ${if (place.photos == null) "null" else "empty"}")
                }
            }
            return places
        } catch (e: Exception) {
            Log.e(tag, "Error in searchPlace query: '$query'", e)
            throw e
        }
    }

    suspend fun getPlaceDetails(placeId: String, fieldMask: String): GooglePlace {
        Log.d(tag, "getPlaceDetails started - Place ID: '$placeId', FieldMask: '$fieldMask'")
        try {
            val place = service.getPlaceDetails(
                placeId = placeId,
                apiKey = apiKey,
                fieldMask = fieldMask
            )
            Log.d(tag, "getPlaceDetails API Response: $place")
            Log.d(tag, "Parsed Details:")
            Log.d(tag, "  - Place ID: ${place.id}")
            Log.d(tag, "  - Place Name: ${place.displayName?.text}")
            Log.d(tag, "  - Rating: ${place.rating}")
            Log.d(tag, "  - Review Count: ${place.userRatingCount}")
            Log.d(tag, "  - Photo Metadata: ${place.photos?.map { "Name: ${it.name}, Width: ${it.widthPx}, Height: ${it.heightPx}" }}")
            
            val firstPhotoName = place.photos?.firstOrNull()?.name
            if (firstPhotoName != null) {
                val url = getPhotoUrl(firstPhotoName, 800)
                Log.d(tag, "  - Generated Photo URL: $url")
            } else {
                Log.d(tag, "  - Generated Photo URL: None (No photos available)")
            }
            return place
        } catch (e: Exception) {
            Log.e(tag, "Error in getPlaceDetails for Place ID: '$placeId'", e)
            throw e
        }
    }

    fun getPhotoUrl(photoName: String, maxWidthPx: Int): String {
        val url = "https://places.googleapis.com/v1/$photoName/media?maxWidthPx=$maxWidthPx&key=$apiKey"
        Log.d(tag, "getPhotoUrl - photoName: '$photoName', maxWidthPx: $maxWidthPx -> Generated URL: $url")
        return url
    }

    suspend fun getPhotoBytes(photoName: String, maxWidthPx: Int): ByteArray {
        Log.d(tag, "getPhotoBytes started - photoName: '$photoName', maxWidthPx: $maxWidthPx")
        try {
            val responseBody = service.getPlacePhoto(
                photoName = photoName,
                apiKey = apiKey,
                maxWidthPx = maxWidthPx
            )
            val bytes = responseBody.bytes()
            Log.d(tag, "getPhotoBytes successful - Byte length: ${bytes.size}")
            return bytes
        } catch (e: Exception) {
            Log.e(tag, "Error in getPhotoBytes for photoName: '$photoName'", e)
            throw e
        }
    }
}
