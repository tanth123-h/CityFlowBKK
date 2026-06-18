package com.example.cityflowbkk.features.route

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cityflowbkk.features.map.DroppedPinUiModel
import com.example.cityflowbkk.features.map.MapLatLng
import com.example.cityflowbkk.features.map.NavigationStepUiModel
import com.example.cityflowbkk.features.map.PlaceSuggestionUiModel
import com.example.cityflowbkk.features.map.RouteTransportType
import com.example.cityflowbkk.ui.icons.HomeIcon
import com.example.cityflowbkk.ui.icons.HomeIconGraphic
import com.example.cityflowbkk.ui.theme.CityFlowBKKTheme
import com.example.cityflowbkk.ui.theme.CityFlowBlue
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.roundToInt

@Composable
fun RouteScreen(
    viewModel: RouteViewModel = viewModel(),
    onNavigateToDetails: (routeDetailsId: String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var hasRequestedPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.refreshLocationPermissionState()
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
    }

    LaunchedEffect(uiState.arrivalAlertsEnabled) {
        if (
            uiState.arrivalAlertsEnabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    RouteContent(
        uiState = uiState,
        onDestinationChange = viewModel::onDestinationChange,
        onMapLoaded = viewModel::onMapLoaded,
        onRecenterMap = {
            if (uiState.hasLocationPermission) {
                viewModel.onMyLocationClick()
            } else if (!hasRequestedPermission) {
                hasRequestedPermission = true
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            }
        },
        onDismissLocationMessage = viewModel::dismissLocationMessage,
        onDestinationSelected = viewModel::onDestinationSelected,
        onMapClick = viewModel::onMapClick,
        onSetPinAsDestination = viewModel::setDroppedPinAsDestination,
        onNavigateToPin = viewModel::navigateToDroppedPin,
        onCalculateRouteToPin = viewModel::calculateRouteToDroppedPin,
        onDismissSearchMessage = viewModel::dismissSearchMessage,
        onDismissRouteMessage = viewModel::dismissRouteMessage,
        onArrivalAlertsEnabledChange = viewModel::onArrivalAlertsEnabledChange,
        onAlertDistanceThresholdChange = viewModel::onAlertDistanceThresholdChange,
        onNavigateToDetails = onNavigateToDetails,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteContent(
    uiState: RouteUiState,
    onDestinationChange: (String) -> Unit,
    onMapLoaded: () -> Unit,
    onRecenterMap: () -> Unit,
    onDismissLocationMessage: () -> Unit,
    onDestinationSelected: (PlaceSuggestionUiModel) -> Unit,
    onMapClick: (MapLatLng) -> Unit,
    onSetPinAsDestination: () -> Unit,
    onNavigateToPin: () -> Unit,
    onCalculateRouteToPin: () -> Unit,
    onDismissSearchMessage: () -> Unit,
    onDismissRouteMessage: () -> Unit,
    onArrivalAlertsEnabledChange: (Boolean) -> Unit,
    onAlertDistanceThresholdChange: (Int) -> Unit,
    onNavigateToDetails: (routeDetailsId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 180.dp,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            RouteBottomSheetContent(
                uiState = uiState,
                onDismissLocationMessage = onDismissLocationMessage,
                onDismissSearchMessage = onDismissSearchMessage,
                onDismissRouteMessage = onDismissRouteMessage,
                onSetPinAsDestination = onSetPinAsDestination,
                onNavigateToPin = onNavigateToPin,
                onCalculateRouteToPin = onCalculateRouteToPin,
                onNavigateToDetails = onNavigateToDetails,
                onArrivalAlertsEnabledChange = onArrivalAlertsEnabledChange,
                onAlertDistanceThresholdChange = onAlertDistanceThresholdChange,
            )
        },
        containerColor = Color.Transparent,
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            RouteMap(
                uiState = uiState,
                onMapLoaded = onMapLoaded,
                onRecenterMap = onRecenterMap,
                onMapClick = onMapClick,
                modifier = Modifier.fillMaxSize(),
            )

            RouteInputSection(
                uiState = uiState,
                onDestinationChange = onDestinationChange,
                onDestinationSelected = onDestinationSelected,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )

        }
    }
}

@Composable
private fun RouteBottomSheetContent(
    uiState: RouteUiState,
    onDismissLocationMessage: () -> Unit,
    onDismissSearchMessage: () -> Unit,
    onDismissRouteMessage: () -> Unit,
    onSetPinAsDestination: () -> Unit,
    onNavigateToPin: () -> Unit,
    onCalculateRouteToPin: () -> Unit,
    onNavigateToDetails: (routeDetailsId: String) -> Unit,
    onArrivalAlertsEnabledChange: (Boolean) -> Unit,
    onAlertDistanceThresholdChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Route Planner",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        ArrivalAlertSettingsCard(
            enabled = uiState.arrivalAlertsEnabled,
            thresholdMeters = uiState.alertDistanceThresholdMeters,
            onEnabledChange = onArrivalAlertsEnabledChange,
            onThresholdChange = onAlertDistanceThresholdChange,
        )

        uiState.locationMessage?.let { message ->
            LocationMessageCard(
                message = message,
                onDismiss = onDismissLocationMessage,
            )
        }

        uiState.searchMessage?.let { message ->
            MessageCard(
                message = message,
                onDismiss = onDismissSearchMessage,
            )
        }

        uiState.routeMessage?.let { message ->
            MessageCard(
                message = message,
                onDismiss = onDismissRouteMessage,
                isError = false,
            )
        }

        uiState.droppedPin?.let { pin ->
            DroppedPinRouteCard(
                pin = pin,
                isCalculatingRoute = uiState.isCalculatingRoute,
                onSetAsDestination = onSetPinAsDestination,
                onNavigateHere = onNavigateToPin,
                onCalculateRoute = onCalculateRouteToPin,
            )
        }

        if (uiState.isCalculatingRoute || uiState.route != null) {
            RouteSummaryCard(
                uiState = uiState,
                isCalculatingRoute = uiState.isCalculatingRoute,
            )
        }

        uiState.currentNavigationInstruction?.let { instruction ->
            ActiveNavigationCard(
                instruction = instruction,
                isOffRoute = uiState.isOffRoute,
            )
        }

        uiState.transitDetails?.let { details ->
            TransitRouteDetailsCard(details = details)
        }

        uiState.routeDetailsId?.let { routeDetailsId ->
            Button(
                onClick = { onNavigateToDetails(routeDetailsId) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                HomeIconGraphic(
                    icon = HomeIcon.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = "View route details",
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        RouteInfoSection(
            title = "Route Result",
            body = uiState.routeResult,
            icon = HomeIcon.Route,
        )

        if (uiState.travelRecommendations.isNotEmpty()) {
            TravelRecommendationsSection(
                recommendations = uiState.travelRecommendations,
                onRecommendationClick = { recommendation ->
                    uiState.routeDetailsId?.let(onNavigateToDetails)
                },
            )
        }

        if (uiState.navigationSteps.isNotEmpty()) {
            NavigationStepsSection(
                steps = uiState.navigationSteps,
                activeStepIndex = uiState.activeNavigationStepIndex,
            )
        }
    }
}

@Composable
private fun ArrivalAlertSettingsCard(
    enabled: Boolean,
    thresholdMeters: Int,
    onEnabledChange: (Boolean) -> Unit,
    onThresholdChange: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Arrival alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "BTS, MRT, and Airport Rail Link",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                )
            }

            Text(
                text = "Alert distance: $thresholdMeters m",
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.SemiBold,
            )
            Slider(
                value = thresholdMeters.toFloat(),
                onValueChange = { value ->
                    onThresholdChange((value / 50f).roundToInt() * 50)
                },
                valueRange = ArrivalAlertSettingsRepository.MIN_THRESHOLD_METERS.toFloat()..
                    ArrivalAlertSettingsRepository.MAX_THRESHOLD_METERS.toFloat(),
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun RouteInputSection(
    uiState: RouteUiState,
    onDestinationChange: (String) -> Unit,
    onDestinationSelected: (PlaceSuggestionUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = uiState.destination,
                onValueChange = onDestinationChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Destination Search") },
                placeholder = { Text("Search destination") },
                leadingIcon = {
                    HomeIconGraphic(
                        icon = HomeIcon.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                singleLine = true,
                supportingText = if (uiState.isSearchingDestination) {
                    { Text("Searching destinations...") }
                } else {
                    null
                },
                shape = RoundedCornerShape(16.dp),
            )

            if (uiState.isSearchingDestination) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.destinationSuggestions.isNotEmpty()) {
                DestinationSuggestions(
                    suggestions = uiState.destinationSuggestions,
                    onDestinationSelected = onDestinationSelected,
                )
            }
        }
    }
}

@Composable
private fun DestinationSuggestions(
    suggestions: List<PlaceSuggestionUiModel>,
    onDestinationSelected: (PlaceSuggestionUiModel) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
    ) {
        LazyColumn(
            modifier = Modifier.height(220.dp),
        ) {
            items(suggestions, key = { it.placeId }) { suggestion ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            role = Role.Button,
                            onClick = { onDestinationSelected(suggestion) },
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = suggestion.primaryText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    suggestion.secondaryText?.let { secondaryText ->
                        Text(
                            text = secondaryText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteMap(
    uiState: RouteUiState,
    onMapLoaded: () -> Unit,
    onRecenterMap: () -> Unit,
    onMapClick: (MapLatLng) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraTarget = LatLng(uiState.cameraTargetLatitude, uiState.cameraTargetLongitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(cameraTarget, uiState.cameraZoom)
    }

    LaunchedEffect(uiState.cameraTargetLatitude, uiState.cameraTargetLongitude, uiState.cameraZoom) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(cameraTarget, uiState.cameraZoom),
            durationMs = 500,
        )
    }

    Box(
        modifier = modifier,
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = uiState.hasLocationPermission),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                zoomGesturesEnabled = true,
                scrollGesturesEnabled = true,
                rotationGesturesEnabled = true,
                tiltGesturesEnabled = true,
                compassEnabled = true,
                mapToolbarEnabled = true,
            ),
            onMapLoaded = onMapLoaded,
            onMapClick = { latLng ->
                onMapClick(
                    MapLatLng(
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                    ),
                )
            },
        ) {
            uiState.markers.forEach { marker ->
                Marker(
                    state = MarkerState(
                        position = LatLng(marker.latitude, marker.longitude),
                    ),
                    title = marker.title,
                    snippet = marker.snippet,
                )
            }

            if (uiState.currentLocationLatitude != null && uiState.currentLocationLongitude != null) {
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            uiState.currentLocationLatitude,
                            uiState.currentLocationLongitude,
                        ),
                    ),
                    title = "Current Location",
                    snippet = "You are here",
                )
            }

            uiState.selectedDestination?.let { destination ->
                if (uiState.droppedPin == null) {
                    Marker(
                        state = MarkerState(
                            position = LatLng(destination.latitude, destination.longitude),
                        ),
                        title = destination.name,
                        snippet = destination.address,
                    )
                }
            }

            uiState.droppedPin?.let { pin ->
                Marker(
                    state = MarkerState(position = LatLng(pin.latitude, pin.longitude)),
                    title = pin.placeName,
                    snippet = pin.address,
                )
            }

            // Render route polylines
            if (uiState.routeSegments.isNotEmpty()) {
                uiState.routeSegments.forEach { segment ->
                    Polyline(
                        points = segment.points.map { LatLng(it.latitude, it.longitude) },
                        color = segment.color,
                        width = if (segment.index == uiState.activeNavigationStepIndex) 14f else 9f,
                    )
                }
            } else if (uiState.overviewPolyline.isNotEmpty()) {
                Polyline(
                    points = uiState.overviewPolyline.map { LatLng(it.latitude, it.longitude) },
                    color = Color.Blue,
                    width = 10f,
                )
            }
        }

        FloatingActionButton(
            onClick = onRecenterMap,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 196.dp),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            if (uiState.isLocationLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                HomeIconGraphic(
                    icon = HomeIcon.Station,
                    contentDescription = "Current location",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun RouteSummaryCard(
    uiState: RouteUiState,
    isCalculatingRoute: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Route Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (isCalculatingRoute) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
            } else {
                Text(
                    text = "${uiState.route?.distanceText} · ${uiState.route?.durationText}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                uiState.route?.arrivalTimeText?.let { arrivalTime ->
                    Text(
                        text = "Arrive around $arrivalTime",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveNavigationCard(
    instruction: String,
    isOffRoute: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (isOffRoute) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = if (isOffRoute) "Recalculating route" else "Current step",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isOffRoute) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
            )
            Text(
                text = instruction,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isOffRoute) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
            )
        }
    }
}

@Composable
private fun TravelRecommendationsSection(
    recommendations: List<TravelRecommendationUiModel>,
    onRecommendationClick: (TravelRecommendationUiModel) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Travel Recommendations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        recommendations.forEach { recommendation ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRecommendationClick(recommendation) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "${recommendation.mode.iconLabel} ${recommendation.mode.label}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (recommendation.isFastest) {
                            RecommendationBadge("Fastest")
                        }
                        if (recommendation.isCheapest) {
                            RecommendationBadge("Cheapest")
                        }
                    }
                    Text(
                        text = "${recommendation.durationMinutes} min - ${recommendation.estimatedCostBaht} baht",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = recommendation.routeSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    recommendation.instructions.forEachIndexed { index, instruction ->
                        Text(
                            text = "${index + 1}. $instruction",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun NavigationStepsSection(
    steps: List<NavigationStepUiModel>,
    activeStepIndex: Int?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Navigation Steps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            steps.forEachIndexed { index, step ->
                val isActive = step.index == activeStepIndex
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isActive) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                    Text(
                        text = "${index + 1}. ${step.transportType.routeLabel()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = step.instruction,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Text(
                        text = "${step.distanceText} - ${step.durationText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    step.transitDetails?.let { transit ->
                        Text(
                            text = listOf(
                                transit.departureStop.takeIf { it.isNotBlank() },
                                transit.arrivalStop.takeIf { it.isNotBlank() },
                            ).joinToString(" to "),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isActive) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    }
                }
            }
        }
    }
}

private fun RouteTransportType.routeLabel(): String {
    return when (this) {
        RouteTransportType.WALKING -> "Walking"
        RouteTransportType.DRIVING -> "Driving"
        RouteTransportType.BUS -> "Bus"
        RouteTransportType.BTS_SUKHUMVIT -> "BTS Sukhumvit Line"
        RouteTransportType.BTS_SILOM -> "BTS Silom Line"
        RouteTransportType.MRT_BLUE -> "MRT Blue Line"
        RouteTransportType.MRT_PURPLE -> "MRT Purple Line"
        RouteTransportType.AIRPORT_RAIL_LINK -> "Airport Rail Link"
        RouteTransportType.UNKNOWN_TRANSIT -> "Transit"
    }
}

@Composable
private fun LocationMessageCard(
    message: String,
    onDismiss: () -> Unit,
) {
    MessageCard(
        message = message,
        onDismiss = onDismiss,
        isError = true,
    )
}

@Composable
private fun MessageCard(
    message: String,
    onDismiss: () -> Unit,
    isError: Boolean = true,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
    ) {
        val contentColor = if (isError) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = "Dismiss",
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun RouteInfoSection(
    title: String,
    body: String,
    icon: HomeIcon,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HomeIconGraphic(
                icon = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TransitRouteDetailsCard(
    details: TransitRouteDetailsUiModel,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HomeIconGraphic(
                    icon = HomeIcon.Train,
                    contentDescription = "Transit route",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(8.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Transit Route",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = details.lineName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RouteMetricRow(label = "Departure station", value = details.departureStation)
                RouteMetricRow(label = "Arrival station", value = details.arrivalStation)
                RouteMetricRow(label = "Number of stations", value = details.stationCount.toString())
                RouteMetricRow(label = "Travel duration", value = details.durationText)
                RouteMetricRow(label = "Distance", value = details.distanceText)
                details.btsOriginStation?.let {
                    RouteMetricRow(label = "BTS origin", value = it)
                }
                details.btsDestinationStation?.let {
                    RouteMetricRow(label = "BTS destination", value = it)
                }
                details.mrtOriginStation?.let {
                    RouteMetricRow(label = "MRT origin", value = it)
                }
                details.mrtDestinationStation?.let {
                    RouteMetricRow(label = "MRT destination", value = it)
                }
                RouteMetricRow(label = "BTS fare", value = details.btsFareText)
                RouteMetricRow(label = "MRT fare", value = details.mrtFareText)
                RouteMetricRow(label = "Total transit fare", value = details.totalTransitFareText)
            }
        }
    }
}

@Composable
private fun RouteMetricRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.72f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DroppedPinRouteCard(
    pin: DroppedPinUiModel,
    isCalculatingRoute: Boolean,
    onSetAsDestination: () -> Unit,
    onNavigateHere: () -> Unit,
    onCalculateRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = pin.placeName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "%.6f, %.6f".format(pin.latitude, pin.longitude),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            if (pin.isLoadingDetails) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 4.dp))
            } else {
                pin.address?.let { address ->
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (isCalculatingRoute) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onSetAsDestination,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Set as Destination")
                }
                Button(
                    onClick = onNavigateHere,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Navigate Here")
                }
            }

            OutlinedButton(
                onClick = onCalculateRoute,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Calculate Route")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun RouteScreenPreview() {
    CityFlowBKKTheme(dynamicColor = false) {
        RouteContent(
            uiState = RouteUiState(),
            onDestinationChange = {},
            onMapLoaded = {},
            onRecenterMap = {},
            onDismissLocationMessage = {},
            onDestinationSelected = {},
            onMapClick = {},
            onSetPinAsDestination = {},
            onNavigateToPin = {},
            onCalculateRouteToPin = {},
            onDismissSearchMessage = {},
            onDismissRouteMessage = {},
            onArrivalAlertsEnabledChange = {},
            onAlertDistanceThresholdChange = {},
            onNavigateToDetails = {},
        )
    }
}
