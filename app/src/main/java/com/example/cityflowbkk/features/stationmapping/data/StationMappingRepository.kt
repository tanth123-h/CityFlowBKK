package com.example.cityflowbkk.features.stationmapping.data

import android.content.Context
import com.example.cityflowbkk.features.stationmapping.model.StationCoordinate
import com.example.cityflowbkk.features.stationmapping.model.StationLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class StationMappingRepository(private val context: Context) {
    
    private val coordinatesFile: File
        get() = File(context.filesDir, "station_coordinates.json")
    
    private val exportFile: File
        get() = File(context.getExternalFilesDir(null), "stations_coordinates.json")
    
    suspend fun loadCoordinates(): List<StationCoordinate> = withContext(Dispatchers.IO) {
        if (!coordinatesFile.exists()) return@withContext emptyList()
        
        try {
            val json = coordinatesFile.readText()
            val jsonArray = JSONArray(json)
            val result = mutableListOf<StationCoordinate>()
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(
                    StationCoordinate(
                        stationId = obj.getString("stationId"),
                        stationName = obj.getString("stationName"),
                        line = StationLine.valueOf(obj.getString("line")),
                        x = obj.getDouble("x").toFloat(),
                        y = obj.getDouble("y").toFloat(),
                        absX = obj.optInt("absX", 0),
                        absY = obj.optInt("absY", 0)
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun saveCoordinates(coordinates: List<StationCoordinate>) = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            coordinates.forEach { coord ->
                val obj = JSONObject().apply {
                    put("stationId", coord.stationId)
                    put("stationName", coord.stationName)
                    put("line", coord.line.name)
                    put("x", coord.x.toDouble())
                    put("y", coord.y.toDouble())
                    put("absX", coord.absX)
                    put("absY", coord.absY)
                }
                jsonArray.put(obj)
            }
            coordinatesFile.writeText(jsonArray.toString(2))
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    suspend fun exportCoordinates(coordinates: List<StationCoordinate>): String? = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            coordinates.forEach { coord ->
                val obj = JSONObject().apply {
                    put("name", coord.stationName)
                    put("x", coord.absX)
                    put("y", coord.absY)
                }
                jsonArray.put(obj)
            }
            exportFile.parentFile?.mkdirs()
            exportFile.writeText(jsonArray.toString(2))
            exportFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun addOrUpdateCoordinate(coordinate: StationCoordinate) {
        val current = loadCoordinates().toMutableList()
        val index = current.indexOfFirst { it.stationId == coordinate.stationId }
        if (index >= 0) {
            current[index] = coordinate
        } else {
            current.add(coordinate)
        }
        saveCoordinates(current)
    }
    
    suspend fun deleteCoordinate(stationId: String) {
        val current = loadCoordinates().toMutableList()
        current.removeAll { it.stationId == stationId }
        saveCoordinates(current)
    }
}
