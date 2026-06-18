package com.example.myapplication.features.tutorials

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TutorialsViewModel : ViewModel() {
    var isFareInformationExpanded by mutableStateOf(false)
        private set

    var fromStation by mutableStateOf<String?>(null)
    var toStation by mutableStateOf<String?>(null)
    var mrtFromStation by mutableStateOf<String?>(null)
    var mrtToStation by mutableStateOf<String?>(null)

    fun onFareInformationClick() {
        isFareInformationExpanded = !isFareInformationExpanded
    }

    fun getCalculatedFare(): Int? {
        val from = fromStation ?: return null
        val to = toStation ?: return null
        
        val fromIndex = FareMatrixData.stationCodes.indexOf(from)
        val toIndex = FareMatrixData.stationCodes.indexOf(to)
        
        if (fromIndex == -1 || toIndex == -1) return null
        
        val row = FareMatrixData.rows.find { it.stationCode == from }
        return row?.fares?.getOrNull(toIndex)
    }

    fun getCalculatedMrtFare(): Int? {
        val from = mrtFromStation ?: return null
        val to = mrtToStation ?: return null

        return MrtFareData.calculateFare(from, to)
    }
}
