package com.example.cityflowbkk.features.route

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.cityflowbkk.features.map.TravelMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RouteDetailsViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteDetailsUiState())
    val uiState: StateFlow<RouteDetailsUiState> = _uiState.asStateFlow()

    init {
        val routeDetailsId = savedStateHandle.get<String>("routeDetailsId").orEmpty()
        val payload = RouteDetailsStore.get(routeDetailsId)

        _uiState.value = if (payload == null) {
            RouteDetailsUiState(
                errorMessage = "Route details are no longer available. Please calculate the route again.",
            )
        } else {
            payload.toUiState()
        }
    }

    private fun RouteDetailsPayload.toUiState(): RouteDetailsUiState {
        val timelineItems = buildList {
            add(RouteTimelineItemUiModel.Origin(label = "Current location"))

            routeResult.segments.forEach { segment ->
                when (segment.travelMode) {
                    TravelMode.WALKING -> add(
                        RouteTimelineItemUiModel.WalkingSegment(
                            distanceText = segment.distanceText.ifBlank { segment.distanceMeters.toDistanceText() },
                            durationText = segment.durationText.ifBlank { segment.durationSeconds.toDurationText() },
                        ),
                    )

                    TravelMode.TRANSIT -> {
                        val transit = segment.transitDetails ?: return@forEach
                        add(
                            RouteTimelineItemUiModel.TransitSegment(
                                lineBadge = transit.lineShortName
                                    ?: transit.lineName.takeIf { it.isNotBlank() }
                                    ?: "Transit",
                                lineName = transit.lineName.ifBlank { transit.lineShortName ?: "Transit" },
                                departureStation = transit.departureStop,
                                arrivalStation = transit.arrivalStop,
                                stopCount = transit.numStops,
                                durationText = segment.durationText.ifBlank { segment.durationSeconds.toDurationText() },
                            ),
                        )
                    }

                    TravelMode.DRIVING -> Unit
                }
            }

            add(
                RouteTimelineItemUiModel.Destination(
                    placeName = destinationName,
                    address = destinationAddress,
                ),
            )
        }

        return RouteDetailsUiState(
            routeTitle = "Transit details",
            totalDurationText = routeResult.route.durationText,
            totalDistanceText = routeResult.route.distanceText,
            fareText = routeResult.fareText,
            timelineItems = timelineItems,
        )
    }

    private fun Int.toDurationText(): String {
        if (this <= 0) return ""
        val minutes = (this + 59) / 60
        return if (minutes < 60) {
            "$minutes min"
        } else {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            if (remainingMinutes == 0) "$hours hr" else "$hours hr $remainingMinutes min"
        }
    }

    private fun Int.toDistanceText(): String {
        return if (this >= 1000) {
            String.format("%.1f km", this / 1000.0)
        } else {
            "$this m"
        }
    }
}
