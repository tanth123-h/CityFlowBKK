package com.example.cityflowbkk.features.route

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.roundToInt

// ─── Navigation sheet expand states ──────────────────────────────────────────
private enum class NavSheetState { Collapsed, HalfExpanded, FullyExpanded }

@Composable
fun RouteScreen(
    viewModel: RouteViewModel = viewModel(),
    onNavigateToDetails: (routeDetailsId: String) -> Unit = {},
    initialDestination: String = "",
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var hasRequestedPermission by remember { mutableStateOf(false) }

    // Pre-fill destination when navigated from another screen (recommended or saved place)
    LaunchedEffect(initialDestination) {
        if (initialDestination.isNotBlank()) {
            viewModel.onDestinationChange(initialDestination)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.refreshLocationPermissionState()
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {}

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
        onDismissArrivalAlert = viewModel::dismissArrivalAlert,
        onArrivalAlertsEnabledChange = viewModel::onArrivalAlertsEnabledChange,
        onAlertDistanceThresholdChange = viewModel::onAlertDistanceThresholdChange,
        onNavigateToDetails = onNavigateToDetails,
        onStartNavigation = viewModel::startNavigation,
        onEndNavigation = viewModel::endNavigation,
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
    onDismissArrivalAlert: () -> Unit,
    onArrivalAlertsEnabledChange: (Boolean) -> Unit,
    onAlertDistanceThresholdChange: (Int) -> Unit,
    onNavigateToDetails: (routeDetailsId: String) -> Unit,
    onStartNavigation: () -> Unit,
    onEndNavigation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

    // Track nav sheet expand state independently so we can drive peekHeight
    var navSheetState by remember { mutableStateOf(NavSheetState.HalfExpanded) }

    // Reset to half-expanded whenever navigation starts fresh
    LaunchedEffect(uiState.isNavigating) {
        if (uiState.isNavigating) navSheetState = NavSheetState.HalfExpanded
    }

    uiState.arrivalAlertStationName?.let { stationName ->
        ArrivalStationAlertDialog(
            stationName = stationName,
            onDismiss = onDismissArrivalAlert,
        )
    }

    // Peek height drives the visible collapsed/half state.
    // FullyExpanded is achieved by the user dragging the sheet all the way up
    // (BottomSheetScaffold natively supports drag-to-full).
    val navSheetPeekHeight = when (navSheetState) {
        NavSheetState.Collapsed     -> 76.dp
        NavSheetState.HalfExpanded  -> 320.dp
        NavSheetState.FullyExpanded -> 320.dp // sheet is dragged open by scaffold
    }

    // Planner peek shows exactly: drag handle + summary row + Start Navigation button
    val plannerPeekHeight = 100.dp
    val currentPeekHeight = if (uiState.isNavigating) navSheetPeekHeight else plannerPeekHeight

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = currentPeekHeight,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            if (uiState.isNavigating) {
                NavigationBottomSheet(
                    uiState = uiState,
                    sheetState = navSheetState,
                    onSheetStateChange = { navSheetState = it },
                    onEndNavigation = onEndNavigation,
                    onNavigateToDetails = onNavigateToDetails,
                )
            } else {
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
                    onStartNavigation = onStartNavigation,
                )
            }
        },
        containerColor = Color.Transparent,
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            RouteMap(
                uiState = uiState,
                sheetPeekHeight = currentPeekHeight.value.toInt(),
                onMapLoaded = onMapLoaded,
                onRecenterMap = onRecenterMap,
                onMapClick = onMapClick,
                modifier = Modifier.fillMaxSize(),
            )

            if (!uiState.isNavigating) {
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
            } else {
                // Top banner always visible during navigation
                NavigationTopBanner(
                    uiState = uiState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                )
            }
        }
    }
}

// ─── Navigation Bottom Sheet ──────────────────────────────────────────────────

/**
 * 3-state navigation sheet:
 *  • Collapsed      – pill strip showing time • distance • fare
 *  • Half-expanded  – ETA, fares, current step, next step, End Navigation
 *  • Fully-expanded – full step list, fare breakdown, route summary
 *
 * Collapsed ↔ HalfExpanded is toggled via the chevron button.
 * HalfExpanded ↔ FullyExpanded is handled by the native BottomSheetScaffold drag gesture.
 */
@Composable
private fun NavigationBottomSheet(
    uiState: RouteUiState,
    sheetState: NavSheetState,
    onSheetStateChange: (NavSheetState) -> Unit,
    onEndNavigation: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring())
            .navigationBarsPadding(),
    ) {
        // ── Drag handle + collapse toggle ──────────────────────────────────
        NavSheetHandle(
            isCollapsed = sheetState == NavSheetState.Collapsed,
            onToggleCollapse = {
                onSheetStateChange(
                    if (sheetState == NavSheetState.Collapsed) NavSheetState.HalfExpanded
                    else NavSheetState.Collapsed,
                )
            },
        )

        // ── Sheet body – switches between states ───────────────────────────
        AnimatedContent(
            targetState = sheetState,
            transitionSpec = {
                (fadeIn() + slideInVertically { it / 4 })
                    .togetherWith(fadeOut() + slideOutVertically { it / 4 })
            },
            label = "nav_sheet_content",
        ) { state ->
            when (state) {
                NavSheetState.Collapsed -> NavCollapsedPill(uiState = uiState)
                NavSheetState.HalfExpanded -> NavHalfExpandedContent(
                    uiState = uiState,
                    onEndNavigation = onEndNavigation,
                )
                NavSheetState.FullyExpanded -> NavFullExpandedContent(
                    uiState = uiState,
                    onEndNavigation = onEndNavigation,
                    onNavigateToDetails = onNavigateToDetails,
                )
            }
        }
    }
}

// ── Drag handle with collapse chevron ────────────────────────────────────────

@Composable
private fun NavSheetHandle(
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left spacer to balance the row
        Spacer(modifier = Modifier.width(40.dp))

        // Centred drag indicator pill
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(2.dp),
                ),
        )

        // Chevron toggle
        Surface(
            modifier = Modifier
                .size(40.dp)
                .clickable(
                    role = Role.Button,
                    onClick = onToggleCollapse,
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                HomeIconGraphic(
                    icon = if (isCollapsed) HomeIcon.ChevronUp else HomeIcon.ChevronDown,
                    contentDescription = if (isCollapsed) "Expand sheet" else "Collapse sheet",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ── Collapsed pill ────────────────────────────────────────────────────────────

@Composable
private fun NavCollapsedPill(uiState: RouteUiState) {
    val time     = uiState.remainingTimeText ?: uiState.route?.durationText ?: "--"
    val distance = uiState.remainingDistanceText ?: uiState.route?.distanceText ?: "--"
    val fare     = uiState.transitDetails?.totalTransitFareText ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = buildString {
                append(time)
                append("  •  ")
                append(distance)
                if (fare.isNotBlank()) {
                    append("  •  ")
                    append(fare)
                }
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── Half-expanded content ─────────────────────────────────────────────────────

@Composable
private fun NavHalfExpandedContent(
    uiState: RouteUiState,
    onEndNavigation: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── ETA row ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = uiState.remainingTimeText ?: uiState.route?.durationText ?: "--",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF34A853),
                )
                Text(
                    text = "ETA  ${uiState.estimatedArrivalTimeText ?: uiState.route?.arrivalTimeText ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = uiState.remainingDistanceText ?: uiState.route?.distanceText ?: "--",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // ── Fare badges ───────────────────────────────────────────────────
        uiState.transitDetails?.let { details ->
            NavFareBadgeRow(details = details)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Current step ──────────────────────────────────────────────────
        uiState.currentNavigationInstruction?.let { current ->
            NavStepRow(
                label = if (uiState.isOffRoute) "Recalculating…" else "Now",
                instruction = current,
                isActive = true,
                isOffRoute = uiState.isOffRoute,
            )
        }

        // ── Next step ─────────────────────────────────────────────────────
        uiState.nextNavigationInstruction?.let { next ->
            NavStepRow(
                label = "Next",
                instruction = next,
                isActive = false,
                isOffRoute = false,
            )
        }

        // ── End Navigation ────────────────────────────────────────────────
        Button(
            onClick = onEndNavigation,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(16.dp),
        ) {
            HomeIconGraphic(
                icon = HomeIcon.Back,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
            )
            Text(
                text = "End Navigation",
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onError,
            )
        }
    }
}

// ── Fully-expanded content ────────────────────────────────────────────────────

@Composable
private fun NavFullExpandedContent(
    uiState: RouteUiState,
    onEndNavigation: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Route summary strip
        NavRouteSummaryStrip(uiState = uiState)

        // Fare breakdown
        uiState.transitDetails?.let { details ->
            NavFareBreakdownCard(details = details)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Full steps list
        if (uiState.navigationSteps.isNotEmpty()) {
            Text(
                text = "Route Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            NavigationStepsSection(
                steps = uiState.navigationSteps,
                activeStepIndex = uiState.activeNavigationStepIndex,
            )
        }

        // View full details button
        uiState.routeDetailsId?.let { id ->
            OutlinedButton(
                onClick = { onNavigateToDetails(id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                HomeIconGraphic(
                    icon = HomeIcon.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "View full route details",
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // End Navigation
        Button(
            onClick = onEndNavigation,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(16.dp),
        ) {
            HomeIconGraphic(
                icon = HomeIcon.Back,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
            )
            Text(
                text = "End Navigation",
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onError,
            )
        }
    }
}

// ── Shared nav sheet sub-composables ─────────────────────────────────────────

@Composable
private fun NavRouteSummaryStrip(uiState: RouteUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NavSummaryChip(
            label = "Remaining",
            value = uiState.remainingTimeText ?: uiState.route?.durationText ?: "--",
            modifier = Modifier.weight(1f),
        )
        NavSummaryChip(
            label = "Distance",
            value = uiState.remainingDistanceText ?: uiState.route?.distanceText ?: "--",
            modifier = Modifier.weight(1f),
        )
        uiState.estimatedArrivalTimeText?.let { eta ->
            NavSummaryChip(
                label = "ETA",
                value = eta,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NavSummaryChip(
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
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
private fun NavFareBadgeRow(details: TransitRouteDetailsUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val btsFare   = details.btsFareText
        val mrtFare   = details.mrtFareText
        val totalFare = details.totalTransitFareText

        if (btsFare != "Unavailable" && btsFare != "฿0") {
            FareBadge(label = "BTS", amount = btsFare, modifier = Modifier.weight(1f))
        }
        if (mrtFare != "Unavailable" && mrtFare != "฿0") {
            FareBadge(label = "MRT", amount = mrtFare, modifier = Modifier.weight(1f))
        }
        FareBadge(
            label = "Total Fare",
            amount = totalFare,
            modifier = Modifier.weight(1f),
            isPrimary = true,
        )
    }
}

@Composable
private fun NavFareBreakdownCard(details: TransitRouteDetailsUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HomeIconGraphic(
                    icon = HomeIcon.Train,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "Fare Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            details.btsOriginStation?.let {
                RouteMetricRow(label = "BTS from", value = it)
            }
            details.btsDestinationStation?.let {
                RouteMetricRow(label = "BTS to", value = it)
            }
            if (details.btsFareText != "Unavailable" && details.btsFareText != "฿0") {
                RouteMetricRow(label = "BTS fare", value = details.btsFareText)
            }
            details.mrtOriginStation?.let {
                RouteMetricRow(label = "MRT from", value = it)
            }
            details.mrtDestinationStation?.let {
                RouteMetricRow(label = "MRT to", value = it)
            }
            if (details.mrtFareText != "Unavailable" && details.mrtFareText != "฿0") {
                RouteMetricRow(label = "MRT fare", value = details.mrtFareText)
            }
            RouteMetricRow(label = "Total fare", value = details.totalTransitFareText)
        }
    }
}

@Composable
private fun NavStepRow(
    label: String,
    instruction: String,
    isActive: Boolean,
    isOffRoute: Boolean,
) {
    val containerColor = when {
        isOffRoute -> MaterialTheme.colorScheme.errorContainer
        isActive   -> MaterialTheme.colorScheme.primaryContainer
        else       -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isOffRoute -> MaterialTheme.colorScheme.onErrorContainer
        isActive   -> MaterialTheme.colorScheme.onPrimaryContainer
        else       -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor.copy(alpha = 0.72f),
            )
            Text(
                text = instruction,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─── Navigation Top Banner (kept for at-a-glance current step) ───────────────

@Composable
private fun NavigationTopBanner(
    uiState: RouteUiState,
    modifier: Modifier = Modifier,
) {
    val (instructions, bannerColor, bannerIcon) = remember(
        uiState.activeNavigationStepIndex,
        uiState.navigationSteps,
    ) {
        val step = uiState.navigationSteps.firstOrNull { it.index == uiState.activeNavigationStepIndex }

        val instructionList: List<String> = if (step != null) {
            if (step.transitDetails != null) {
                val type    = step.transportType
                val transit = step.transitDetails
                val line = when (type) {
                    RouteTransportType.BTS_SUKHUMVIT   -> "BTS Sukhumvit Line"
                    RouteTransportType.BTS_SILOM        -> "BTS Silom Line"
                    RouteTransportType.MRT_BLUE         -> "MRT Blue Line"
                    RouteTransportType.MRT_PURPLE       -> "MRT Purple Line"
                    RouteTransportType.AIRPORT_RAIL_LINK -> "Airport Rail Link"
                    RouteTransportType.BUS              -> "Bus ${transit.lineShortName ?: transit.lineName}"
                    else                               -> transit.lineShortName ?: transit.lineName
                }
                val stationCount = transit.numStops
                val rideText = when {
                    stationCount > 1 -> "Ride $stationCount stations"
                    stationCount == 1 -> "Ride 1 station"
                    else -> "Ride"
                }
                listOfNotNull(
                    "Board $line",
                    rideText,
                    if (transit.arrivalStop.isNotBlank()) "Exit at ${transit.arrivalStop}" else null,
                )
            } else {
                listOf(step.instruction)
            }
        } else {
            listOf("Follow route on map")
        }

        val color = when (step?.transportType) {
            RouteTransportType.BTS_SUKHUMVIT    -> Color(0xFF2E7D32)
            RouteTransportType.BTS_SILOM         -> Color(0xFF1B5E20)
            RouteTransportType.MRT_BLUE          -> Color(0xFF0D47A1)
            RouteTransportType.MRT_PURPLE        -> Color(0xFF4A148C)
            RouteTransportType.AIRPORT_RAIL_LINK -> Color(0xFF880E4F)
            RouteTransportType.BUS               -> Color(0xFFE65100)
            RouteTransportType.WALKING           -> Color(0xFF1565C0)
            RouteTransportType.DRIVING           -> Color(0xFF37474F)
            else                                -> Color(0xFF1B5E20)
        }

        val icon = when (step?.transportType) {
            RouteTransportType.WALKING  -> HomeIcon.Walk
            RouteTransportType.DRIVING  -> HomeIcon.Car
            RouteTransportType.BUS      -> HomeIcon.Subway
            RouteTransportType.BTS_SUKHUMVIT,
            RouteTransportType.BTS_SILOM,
            RouteTransportType.MRT_BLUE,
            RouteTransportType.MRT_PURPLE,
            RouteTransportType.AIRPORT_RAIL_LINK,
            RouteTransportType.UNKNOWN_TRANSIT -> HomeIcon.Train
            else -> HomeIcon.Route
        }

        Triple(instructionList, color, icon)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bannerColor.copy(alpha = 0.93f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HomeIconGraphic(
                    icon = bannerIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    instructions.forEachIndexed { index, inst ->
                        Text(
                            text = inst,
                            style = if (index == 0) MaterialTheme.typography.titleMedium
                                    else MaterialTheme.typography.bodyMedium,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                            color = Color.White,
                        )
                    }
                }
            }

            uiState.nextNavigationInstruction?.let { next ->
                Text(
                    text = "Next: $next",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.80f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ─── Alert dialog ─────────────────────────────────────────────────────────────

@Composable
private fun ArrivalStationAlertDialog(
    stationName: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Next Station",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Your next station is $stationName.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "This is your drop-off station. Please prepare to leave the train.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        },
    )
}

// ─── Route Planner Bottom Sheet content (non-navigation mode) ─────────────────
//
// Layout:
//  ┌──────────────────────────────────────────┐  ← peek (always visible, ~100 dp)
//  │  drag handle                             │
//  │  [ETA/Distance/Fare chips] [Start Nav]   │
//  ├──────────────────────────────────────────┤  ← expanded (scroll)
//  │  transit details, steps, settings, etc.  │
//  └──────────────────────────────────────────┘

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
    onStartNavigation: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        // ── Drag handle ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(2.dp),
                    ),
            )
        }

        // ── Peek row — always visible above the fold ──────────────────────
        // Shows the essential route summary and the Start Navigation button.
        // Height is intentionally compact so ≥70% of the screen stays as map.
        PlannerPeekRow(
            uiState = uiState,
            onStartNavigation = onStartNavigation,
        )

        // ── Expanded content — only visible when sheet is dragged up ──────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            uiState.locationMessage?.let { message ->
                LocationMessageCard(message = message, onDismiss = onDismissLocationMessage)
            }
            uiState.searchMessage?.let { message ->
                MessageCard(message = message, onDismiss = onDismissSearchMessage)
            }
            uiState.routeMessage?.let { message ->
                MessageCard(message = message, onDismiss = onDismissRouteMessage, isError = false)
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

            uiState.transitDetails?.let { details ->
                TransitRouteDetailsCard(details = details)
            }

            uiState.routeDetailsId?.let { routeDetailsId ->
                OutlinedButton(
                    onClick = { onNavigateToDetails(routeDetailsId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    HomeIconGraphic(
                        icon = HomeIcon.Route,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "View full route details",
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (uiState.navigationSteps.isNotEmpty()) {
                NavigationStepsSection(
                    steps = uiState.navigationSteps,
                    activeStepIndex = uiState.activeNavigationStepIndex,
                )
            }

            ArrivalAlertSettingsCard(
                enabled = uiState.arrivalAlertsEnabled,
                thresholdMeters = uiState.alertDistanceThresholdMeters,
                onEnabledChange = onArrivalAlertsEnabledChange,
                onThresholdChange = onAlertDistanceThresholdChange,
            )
        }
    }
}

// ── Planner peek row ──────────────────────────────────────────────────────────

@Composable
private fun PlannerPeekRow(
    uiState: RouteUiState,
    onStartNavigation: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Summary chips — duration, distance, fare
        if (uiState.isCalculatingRoute) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Calculating…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        } else if (uiState.route != null) {
            // Duration chip
            PeekChip(
                value = uiState.route.durationText,
                label = uiState.route.arrivalTimeText,
                modifier = Modifier.weight(1f),
            )
            // Distance chip
            PeekChip(
                value = uiState.route.distanceText,
                label = "distance",
                modifier = Modifier.weight(1f),
            )
            // Fare chip — only shown when available
            uiState.transitDetails?.totalTransitFareText
                ?.takeIf { it.isNotBlank() && it != "Unavailable" }
                ?.let { fare ->
                    PeekChip(
                        value = fare,
                        label = "fare",
                        modifier = Modifier.weight(1f),
                    )
                }
        } else {
            Text(
                text = "Search a destination above",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }

        // Start Navigation button — only when route is ready
        if (uiState.route != null && !uiState.isCalculatingRoute) {
            Button(
                onClick = onStartNavigation,
                colors = ButtonDefaults.buttonColors(containerColor = CityFlowBlue),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            ) {
                HomeIconGraphic(
                    icon = HomeIcon.Route,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Go",
                    modifier = Modifier.padding(start = 6.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun PeekChip(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp),
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─── Arrival alert settings card ─────────────────────────────────────────────

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
                Switch(checked = enabled, onCheckedChange = onEnabledChange)
            }

            Text(
                text = "Alert distance: $thresholdMeters m",
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
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

// ─── Route input section ──────────────────────────────────────────────────────

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
                } else null,
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
        LazyColumn(modifier = Modifier.height(220.dp)) {
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

// ─── Map composable ───────────────────────────────────────────────────────────

@Composable
private fun RouteMap(
    uiState: RouteUiState,
    sheetPeekHeight: Int,
    onMapLoaded: () -> Unit,
    onRecenterMap: () -> Unit,
    onMapClick: (MapLatLng) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraTarget = LatLng(uiState.cameraTargetLatitude, uiState.cameraTargetLongitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(cameraTarget, uiState.cameraZoom)
    }

    // Fit the camera to the full route bounds when a route is loaded.
    // Falls back to simple lat/lng zoom when no bounds are available.
    LaunchedEffect(uiState.routeBounds) {
        val bounds = uiState.routeBounds
        if (bounds != null) {
            val latLngBounds = LatLngBounds(
                LatLng(bounds.swLat, bounds.swLng),
                LatLng(bounds.neLat, bounds.neLng),
            )
            // padding: 80px top (search bar) + sheetPeekHeight converted px equivalent handled
            // by the map's own contentPadding; use 64px here as extra visual buffer
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(latLngBounds, 64),
                durationMs = 700,
            )
        }
    }

    LaunchedEffect(uiState.cameraTargetLatitude, uiState.cameraTargetLongitude, uiState.cameraZoom) {
        // Only drive point zoom when there are no route bounds to show
        if (uiState.routeBounds == null) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(cameraTarget, uiState.cameraZoom),
                durationMs = 500,
            )
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            // Push map UI controls (compass, zoom buttons) above the bottom sheet
            // so destination markers are never hidden behind it
            contentPadding = PaddingValues(bottom = sheetPeekHeight.dp),
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
                onMapClick(MapLatLng(latitude = latLng.latitude, longitude = latLng.longitude))
            },
        ) {
            uiState.markers.forEach { marker ->
                val markerState = remember(marker.latitude, marker.longitude) {
                    MarkerState(position = LatLng(marker.latitude, marker.longitude))
                }
                Marker(state = markerState, title = marker.title, snippet = marker.snippet)
            }

            if (uiState.currentLocationLatitude != null && uiState.currentLocationLongitude != null) {
                val currentLocationState = remember(
                    uiState.currentLocationLatitude,
                    uiState.currentLocationLongitude,
                ) {
                    MarkerState(
                        position = LatLng(
                            uiState.currentLocationLatitude,
                            uiState.currentLocationLongitude,
                        ),
                    )
                }
                Marker(
                    state = currentLocationState,
                    title = "Current Location",
                    snippet = "You are here",
                )
            }

            uiState.selectedDestination?.let { destination ->
                if (uiState.droppedPin == null) {
                    val destinationState = remember(destination.latitude, destination.longitude) {
                        MarkerState(position = LatLng(destination.latitude, destination.longitude))
                    }
                    Marker(
                        state = destinationState,
                        title = destination.name,
                        snippet = destination.address,
                    )
                }
            }

            uiState.droppedPin?.let { pin ->
                val pinState = remember(pin.latitude, pin.longitude) {
                    MarkerState(position = LatLng(pin.latitude, pin.longitude))
                }
                Marker(state = pinState, title = pin.placeName, snippet = pin.address)
            }

            if (uiState.routeSegments.isNotEmpty()) {
                // Draw walking segments first (underneath), then transit segments (on top)
                val sortedSegments = uiState.routeSegments.sortedBy { segment ->
                    if (segment.segmentType == RouteSegmentType.Walking) 0 else 1
                }
                sortedSegments.forEach { segment ->
                    Polyline(
                        points = segment.points.map { LatLng(it.latitude, it.longitude) },
                        color = segment.color,
                        width = segment.width,
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

        // Recenter FAB — sits above the bottom sheet peek area
        FloatingActionButton(
            onClick = onRecenterMap,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = (sheetPeekHeight + 16).dp),
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

// ─── Shared planner-mode cards (unchanged) ────────────────────────────────────

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
        color = if (isOffRoute) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = if (isOffRoute) "Recalculating route" else "Current step",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isOffRoute) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = instruction,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isOffRoute) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun FareBadge(
    label: String,
    amount: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isPrimary) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
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
                        if (recommendation.isFastest)  RecommendationBadge("Fastest")
                        if (recommendation.isCheapest) RecommendationBadge("Cheapest")
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
                    color = if (isActive) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "${index + 1}. ${step.transportType.routeLabel()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = step.instruction,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${step.distanceText} - ${step.durationText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        step.transitDetails?.let { transit ->
                            Text(
                                text = listOf(
                                    transit.departureStop.takeIf { it.isNotBlank() },
                                    transit.arrivalStop.takeIf { it.isNotBlank() },
                                ).joinToString(" to "),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
private fun LocationMessageCard(message: String, onDismiss: () -> Unit) {
    MessageCard(message = message, onDismiss = onDismiss, isError = true)
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
        color = if (isError) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        val contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer
                           else MaterialTheme.colorScheme.onSurfaceVariant
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
                Text(text = "Dismiss", color = contentColor, fontWeight = FontWeight.SemiBold)
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
            HomeIconGraphic(icon = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TransitRouteDetailsCard(details: TransitRouteDetailsUiModel) {
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
                RouteMetricRow(label = "Arrival station",   value = details.arrivalStation)
                RouteMetricRow(label = "Number of stations", value = details.stationCount.toString())
                RouteMetricRow(label = "Travel duration",   value = details.durationText)
                RouteMetricRow(label = "Distance",          value = details.distanceText)
                details.btsOriginStation?.let      { RouteMetricRow("BTS origin",      it) }
                details.btsDestinationStation?.let { RouteMetricRow("BTS destination", it) }
                details.mrtOriginStation?.let      { RouteMetricRow("MRT origin",      it) }
                details.mrtDestinationStation?.let { RouteMetricRow("MRT destination", it) }
                RouteMetricRow(label = "BTS fare",          value = details.btsFareText)
                RouteMetricRow(label = "MRT fare",          value = details.mrtFareText)
                RouteMetricRow(label = "Total transit fare", value = details.totalTransitFareText)
            }
        }
    }
}

@Composable
private fun RouteMetricRow(label: String, value: String) {
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
                ) { Text("Set as Destination") }
                Button(
                    onClick = onNavigateHere,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                ) { Text("Navigate Here") }
            }
            OutlinedButton(
                onClick = onCalculateRoute,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) { Text("Calculate Route") }
        }
    }
}

// ─── Extension helpers ────────────────────────────────────────────────────────

private fun RouteTransportType.routeLabel(): String = when (this) {
    RouteTransportType.WALKING           -> "Walking"
    RouteTransportType.DRIVING           -> "Driving"
    RouteTransportType.BUS               -> "Bus"
    RouteTransportType.BTS_SUKHUMVIT     -> "BTS Sukhumvit Line"
    RouteTransportType.BTS_SILOM         -> "BTS Silom Line"
    RouteTransportType.MRT_BLUE          -> "MRT Blue Line"
    RouteTransportType.MRT_PURPLE        -> "MRT Purple Line"
    RouteTransportType.AIRPORT_RAIL_LINK -> "Airport Rail Link"
    RouteTransportType.UNKNOWN_TRANSIT   -> "Transit"
}

// ─── Preview ──────────────────────────────────────────────────────────────────

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
            onDismissArrivalAlert = {},
            onArrivalAlertsEnabledChange = {},
            onAlertDistanceThresholdChange = {},
            onNavigateToDetails = {},
            onStartNavigation = {},
            onEndNavigation = {},
        )
    }
}
