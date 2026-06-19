package com.example.cityflowbkk.features.route

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cityflowbkk.ui.icons.HomeIcon
import com.example.cityflowbkk.ui.icons.HomeIconGraphic
import com.example.cityflowbkk.ui.theme.CityFlowBKKTheme
import com.example.cityflowbkk.ui.theme.CityFlowBlue
import com.example.cityflowbkk.ui.theme.CityFlowGreen

@Composable
fun RouteDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: RouteDetailsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    RouteDetailsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteDetailsContent(
    uiState: RouteDetailsUiState,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.routeTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        HomeIconGraphic(
                            icon = HomeIcon.Back,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (uiState.errorMessage != null) {
                ErrorCard(message = uiState.errorMessage)
            } else {
                RouteSummaryStrip(uiState = uiState)
                TimelineCard(items = uiState.timelineItems)
            }
        }
    }
}

@Composable
private fun RouteSummaryStrip(uiState: RouteDetailsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryChip(
            label = "Duration",
            value = uiState.totalDurationText,
            modifier = Modifier.weight(1f),
        )
        SummaryChip(
            label = "Distance",
            value = uiState.totalDistanceText,
            modifier = Modifier.weight(1f),
        )
        uiState.fareText?.let {
            SummaryChip(
                label = "Fare",
                value = it,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimelineCard(items: List<RouteTimelineItemUiModel>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            items.forEachIndexed { index, item ->
                TimelineRow(
                    item = item,
                    isLast = index == items.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun TimelineRow(
    item: RouteTimelineItemUiModel,
    isLast: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        TimelineRail(item = item, isLast = isLast)
        TimelineContent(item = item)
    }
}

@Composable
private fun TimelineRail(
    item: RouteTimelineItemUiModel,
    isLast: Boolean,
) {
    Column(
        modifier = Modifier.width(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (item) {
            is RouteTimelineItemUiModel.Origin -> HollowNode()
            is RouteTimelineItemUiModel.Destination -> DestinationNode()
            is RouteTimelineItemUiModel.WalkingSegment -> IconNode(HomeIcon.Walk, CityFlowBlue)
            is RouteTimelineItemUiModel.TransitSegment -> IconNode(HomeIcon.Train, CityFlowGreen)
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(52.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
        }
    }
}

@Composable
private fun HollowNode() {
    Surface(
        modifier = Modifier.size(14.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder(),
    ) {}
}

@Composable
private fun DestinationNode() {
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(MaterialTheme.colorScheme.error, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Color.White, CircleShape),
        )
    }
}

@Composable
private fun IconNode(icon: HomeIcon, color: Color) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(color.copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        HomeIconGraphic(
            icon = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun TimelineContent(item: RouteTimelineItemUiModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp),
    ) {
        when (item) {
            is RouteTimelineItemUiModel.Origin -> LocationContent(
                title = "Origin",
                primary = item.label,
                secondary = null,
                nearestStation = item.nearestStationName,
            )

            is RouteTimelineItemUiModel.WalkingSegment -> WalkingContent(item)
            is RouteTimelineItemUiModel.TransitSegment -> TransitContent(item)

            is RouteTimelineItemUiModel.Destination -> LocationContent(
                title = "Destination",
                primary = item.placeName,
                secondary = item.address,
                nearestStation = item.nearestStationName,
            )
        }
    }
}

@Composable
private fun LocationContent(
    title: String,
    primary: String,
    secondary: String?,
    nearestStation: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        secondary?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        nearestStation?.let {
            Text(
                text = "Nearest station: $it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun WalkingContent(item: RouteTimelineItemUiModel.WalkingSegment) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Walk",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = item.distanceText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = item.durationText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TransitContent(item: RouteTimelineItemUiModel.TransitSegment) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = CityFlowGreen,
            ) {
                Text(
                    text = item.lineBadge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Text(
                text = item.lineName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = item.departureStation,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "↓",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = item.arrivalStation,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "${item.stopCount} stops",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = item.durationText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 860)
@Composable
private fun RouteDetailsPreview() {
    CityFlowBKKTheme(dynamicColor = false) {
        RouteDetailsContent(
            uiState = RouteDetailsUiState(
                totalDurationText = "24 min",
                totalDistanceText = "8.1 km",
                fareText = "฿34.00",
                timelineItems = listOf(
                    RouteTimelineItemUiModel.Origin("Current location"),
                    RouteTimelineItemUiModel.WalkingSegment("650 m", "11 min"),
                    RouteTimelineItemUiModel.TransitSegment(
                        lineBadge = "34 EV",
                        lineName = "Transit Line",
                        departureStation = "MBK Center",
                        arrivalStation = "Victory Monument",
                        stopCount = 5,
                        durationText = "7 min",
                    ),
                    RouteTimelineItemUiModel.WalkingSegment("280 m", "4 min"),
                    RouteTimelineItemUiModel.Destination(
                        placeName = "Victory Monument",
                        address = "Ratchathewi, Bangkok",
                    ),
                ),
            ),
            onNavigateBack = {},
        )
    }
}
