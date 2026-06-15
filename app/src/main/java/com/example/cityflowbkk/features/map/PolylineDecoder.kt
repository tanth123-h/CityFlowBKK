package com.example.cityflowbkk.features.map

object PolylineDecoder {
    fun decode(encoded: String): List<MapLatLng> {
        val poly = mutableListOf<MapLatLng>()
        var index = 0
        val length = encoded.length
        var lat = 0
        var lng = 0

        while (index < length) {
            var shift = 0
            var result = 0
            var byte: Int
            do {
                byte = encoded[index++].code - 63
                result = result or ((byte and 0x1f) shl shift)
                shift += 5
            } while (byte >= 0x20)
            val deltaLat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += deltaLat

            shift = 0
            result = 0
            do {
                byte = encoded[index++].code - 63
                result = result or ((byte and 0x1f) shl shift)
                shift += 5
            } while (byte >= 0x20)
            val deltaLng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += deltaLng

            poly.add(
                MapLatLng(
                    latitude = lat / 1E5,
                    longitude = lng / 1E5,
                ),
            )
        }

        return poly
    }
}
