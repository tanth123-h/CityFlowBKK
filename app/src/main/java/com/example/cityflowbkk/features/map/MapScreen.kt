package com.example.cityflowbkk.features.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cityflowbkk.ui.icons.HomeIcon
import com.example.cityflowbkk.ui.icons.HomeIconGraphic
import com.example.cityflowbkk.ui.theme.CityFlowBlue
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var hasRequestedPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.refreshLocationPermissionState()
    }

    LaunchedEffect(uiState.hasLocationPermission) {
        if (!uiState.hasLocationPermission && !hasRequestedPermission) {
            hasRequestedPermission = true
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            LatLng(uiState.cameraTarget.latitude, uiState.cameraTarget.longitude),
            uiState.cameraZoom,
        )
    }

    LaunchedEffect(uiState.cameraTarget, uiState.cameraZoom) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(
                LatLng(uiState.cameraTarget.latitude, uiState.cameraTarget.longitude),
                uiState.cameraZoom,
            ),
            durationMs = 500,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val mapProperties = remember(uiState.hasLocationPermission) {
            MapProperties(isMyLocationEnabled = uiState.hasLocationPermission)
        }
        val mapUiSettings = remember {
            MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = false,
                compassEnabled = true,
            )
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            onMapLoaded = { viewModel.onMapReady() },
            onMapClick = { latLng ->
                viewModel.onMapClick(MapLatLng(latLng.latitude, latLng.longitude))
            },
        ) {
 tindersuper
            uiState.selectedPlace?.let { place ->
                val markerState = remember(place.latitude, place.longitude) {
                    MarkerState(position = LatLng(place.latitude, place.longitude))
                }
                Marker(
                    state = markerState,
                    title = place.name,
                    snippet = place.address,

            if (uiState.droppedPin == null) {
                uiState.selectedPlace?.let { place ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(place.latitude, place.longitude),
                        ),
                        title = place.name,
                        snippet = place.address,
                    )
                }
            }

            uiState.droppedPin?.let { pin ->
                Marker(
                    state = MarkerState(
                        position = LatLng(pin.latitude, pin.longitude),
                    ),
                    title = pin.placeName,
                    snippet = pin.address,
 master
                )
            }

            if (uiState.routePoints.isNotEmpty()) {
                Polyline(
                    points = uiState.routePoints.map { LatLng(it.latitude, it.longitude) },
                    color = CityFlowBlue,
                    width = 10f,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search places, stations, landmarks...") },
                leadingIcon = {
                    HomeIconGraphic(
                        icon = HomeIcon.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                ),
            )

            if (uiState.isSearching) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.suggestions.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp,
                ) {
                    LazyColumn {
                        items(uiState.suggestions, key = { it.placeId }) { suggestion ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onSuggestionSelected(suggestion) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                Text(
                                    text = suggestion.primaryText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                suggestion.secondaryText?.let { secondary ->
                                    Text(
                                        text = secondary,
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

            uiState.errorMessage?.let { message ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (uiState.hasLocationPermission) {
                    viewModel.onMyLocationClick()
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 120.dp),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            HomeIconGraphic(
                icon = HomeIcon.Station,
                contentDescription = "My location",
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }

        uiState.droppedPin?.let { pin ->
            DroppedPinCard(
                pin = pin,
                route = uiState.route,
                isLoadingRoute = uiState.isLoadingRoute,
                onSetAsDestination = viewModel::setDroppedPinAsDestination,
                onNavigateHere = viewModel::navigateToDroppedPin,
                onCalculateRoute = viewModel::calculateRouteToDroppedPin,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            )
        } ?: uiState.selectedPlace?.let { place ->
            RouteInfoCard(
                place = place,
                route = uiState.route,
                isLoadingRoute = uiState.isLoadingRoute,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun DroppedPinCard(
    pin: DroppedPinUiModel,
    route: RouteUiModel?,
    isLoadingRoute: Boolean,
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

            when {
                isLoadingRoute -> CircularProgressIndicator(modifier = Modifier.padding(top = 4.dp))
                route != null -> {
                    Text(
                        text = "${route.distanceText} · ${route.durationText}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Arrive around ${route.arrivalTimeText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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

@Composable
private fun RouteInfoCard(
    place: MapPlaceUiModel,
    route: RouteUiModel?,
    isLoadingRoute: Boolean,
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
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            place.address?.let { address ->
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            when {
                isLoadingRoute -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 4.dp))
                }
                route != null -> {
                    Text(
                        text = "${route.distanceText} · ${route.durationText}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Arrive around ${route.arrivalTimeText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
