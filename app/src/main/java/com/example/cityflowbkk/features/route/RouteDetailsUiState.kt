package com.example.cityflowbkk.features.route

data class RouteDetailsUiState(
    val routeTitle: String = "Transit details",
    val totalDurationText: String = "",
    val totalDistanceText: String = "",
    val fareText: String? = null,
    val timelineItems: List<RouteTimelineItemUiModel> = emptyList(),
    val errorMessage: String? = null,
)

sealed class RouteTimelineItemUiModel {
    data class Origin(
        val label: String,
    ) : RouteTimelineItemUiModel()

    data class WalkingSegment(
        val distanceText: String,
        val durationText: String,
    ) : RouteTimelineItemUiModel()

    data class TransitSegment(
        val lineBadge: String,
        val lineName: String,
        val departureStation: String,
        val arrivalStation: String,
        val stopCount: Int,
        val durationText: String,
    ) : RouteTimelineItemUiModel()

    data class Destination(
        val placeName: String,
        val address: String?,
    ) : RouteTimelineItemUiModel()
}
