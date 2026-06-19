package com.example.cityflowbkk.features.route

import com.example.cityflowbkk.features.map.RouteResult
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object RouteDetailsStore {
    private val routes = ConcurrentHashMap<String, RouteDetailsPayload>()

    fun put(payload: RouteDetailsPayload): String {
        val id = UUID.randomUUID().toString()
        routes[id] = payload.copy(id = id)
        return id
    }

    fun get(id: String): RouteDetailsPayload? = routes[id]
}

data class RouteDetailsPayload(
    val id: String = "",
    val destinationName: String,
    val destinationAddress: String?,
    val routeResult: RouteResult,
)
