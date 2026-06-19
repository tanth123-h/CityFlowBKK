package com.example.cityflowbkk.data.places

import com.example.cityflowbkk.data.places.model.PlaceSearchRequest
import com.example.cityflowbkk.data.places.model.PlaceSearchResponse
import com.example.cityflowbkk.data.places.model.GooglePlace
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GooglePlacesService {
    @POST("v1/places:searchText")
    suspend fun searchText(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String,
        @Body request: PlaceSearchRequest
    ): PlaceSearchResponse

    @GET("v1/places/{placeId}")
    suspend fun getPlaceDetails(
        @Path("placeId") placeId: String,
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String
    ): GooglePlace

    @GET("v1/{photoName}/media")
    suspend fun getPlacePhoto(
        @Path(value = "photoName", encoded = true) photoName: String,
        @Query("key") apiKey: String,
        @Query("maxWidthPx") maxWidthPx: Int?
    ): ResponseBody
}
