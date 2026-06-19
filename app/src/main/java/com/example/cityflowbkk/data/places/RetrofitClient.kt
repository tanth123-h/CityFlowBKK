package com.example.cityflowbkk.data.places

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://places.googleapis.com/"

    fun createService(): GooglePlacesService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // Use a lenient Gson instance so that integer-valued ratings (e.g. "4" not "4.0")
        // are correctly coerced into Double?, and unexpected fields in photo objects
        // (flagContentUri, googleMapsUri) are silently ignored.
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GooglePlacesService::class.java)
    }
}
