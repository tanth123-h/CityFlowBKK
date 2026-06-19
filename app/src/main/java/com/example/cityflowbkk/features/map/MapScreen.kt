package com.example.cityflowbkk.features.map

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
 Myheero
import androidx.compose.foundation.layout.Spacer
======= master
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
 Myheero
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
=======
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
 master
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
 Myheero
=======
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
 master
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cityflowbkk.R
import kotlin.math.min

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = Modifier.fillMaxSize()) {
        // BTS Map Image + Tap Detection + Markers
        BtsMapWithTapDetection(
            markers = uiState.tappedMarkers,
            zoom = uiState.mapZoom,
            onMapTapped = { x, y -> viewModel.onMapTapped(x, y) },
            onImageSizeChanged = { size -> imageSize = size },
            modifier = Modifier.fillMaxSize(),
        )

        // Debug Panel (Top Right)
        DebugPanel(
            lastTapX = uiState.lastTapX,
            lastTapY = uiState.lastTapY,
            markerCount = uiState.tappedMarkers.size,
            onClearAll = { viewModel.clearAllMarkers() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp),
        )

        // Zoom Controls (Center Right)
        ZoomControls(
            currentZoom = uiState.mapZoom,
            onZoomIn = { viewModel.onZoomIn() },
            onZoomOut = { viewModel.onZoomOut() },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
        )
    }
}

@Composable
private fun BtsMapWithTapDetection(
    markers: List<TappedMarker>,
    zoom: Float,
    onMapTapped: (Float, Float) -> Unit,
    onImageSizeChanged: (IntSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    // Smooth zoom animation
    val animatedZoom by animateFloatAsState(
        targetValue = zoom,
        label = "mapZoom"
    )

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val containerW = containerSize.width.toFloat()
                    val containerH = containerSize.height.toFloat()
                    val imgW = imageSize.width.toFloat().takeIf { it > 0 } ?: containerW
                    val imgH = imageSize.height.toFloat().takeIf { it > 0 } ?: containerH

                    val scaleX = containerW / imgW
                    val scaleY = containerH / imgH
                    val scale = min(scaleX, scaleY) * animatedZoom

                    val renderedW = imgW * scale
                    val renderedH = imgH * scale
                    val imgLeft = (containerW - renderedW) / 2f
                    val imgTop = (containerH - renderedH) / 2f

                    val relX = (tapOffset.x - imgLeft) / renderedW
                    val relY = (tapOffset.y - imgTop) / renderedH

                    if (relX in 0f..1f && relY in 0f..1f) {
                        onMapTapped(relX, relY)
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // BTS Map Image with zoom
        Image(
            painter = painterResource(R.drawable.btsmap),
            contentDescription = "BTS Map",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = animatedZoom,
                    scaleY = animatedZoom,
                )
                .onSizeChanged {
                    imageSize = it
                    onImageSizeChanged(it)
                },
        )

 Myheero
        // Marker Layer
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = animatedZoom,
                    scaleY = animatedZoom,
=======
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
 master
                )
        ) {
            val containerW = size.width
            val containerH = size.height
            val imgW = imageSize.width.toFloat().takeIf { it > 0 } ?: containerW
            val imgH = imageSize.height.toFloat().takeIf { it > 0 } ?: containerH

            val scaleX = containerW / imgW
            val scaleY = containerH / imgH
            val scale = min(scaleX, scaleY)

            val renderedW = imgW * scale
            val renderedH = imgH * scale
            val imgLeft = (containerW - renderedW) / 2f
            val imgTop = (containerH - renderedH) / 2f

            val markerRadius = 12.dp.toPx() / animatedZoom
            val strokeWidth = 3.dp.toPx() / animatedZoom

            markers.forEach { marker ->
                val cx = imgLeft + marker.x * renderedW
                val cy = imgTop + marker.y * renderedH

                drawCircle(
                    color = Color(0xFF2196F3),
                    radius = markerRadius,
                    center = Offset(cx, cy),
                )
                drawCircle(
                    color = Color.White,
                    radius = markerRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeWidth),
                )
            }
        }
    }
}

@Composable
private fun ZoomControls(
    currentZoom: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Zoom In Button
        FloatingActionButton(
            onClick = onZoomIn,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Zoom In",
                modifier = Modifier.size(24.dp),
            )
        }

        // Zoom Out Button
        FloatingActionButton(
            onClick = onZoomOut,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Zoom Out",
                modifier = Modifier.size(24.dp),
            )
        }

 Myheero
        Spacer(Modifier.height(4.dp))

        // Zoom Level Display
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = 2.dp,
        ) {
            Text(
                text = "%.1fx".format(currentZoom),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
=======
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
 master
            )
        }
    }
}

@Composable
 Myheero
private fun DebugPanel(
    lastTapX: Float?,
    lastTapY: Float?,
    markerCount: Int,
    onClearAll: () -> Unit,
=======
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
master
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.width(180.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Debug Panel",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DebugRow(
                        label = "Last X:",
                        value = lastTapX?.let { "%.4f".format(it) } ?: "-",
                    )
                    DebugRow(
                        label = "Last Y:",
                        value = lastTapY?.let { "%.4f".format(it) } ?: "-",
                    )
                    DebugRow(
                        label = "Markers:",
                        value = markerCount.toString(),
                    )
                }
            }

            Button(
                onClick = onClearAll,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
                enabled = markerCount > 0,
            ) {
                Text(
                    text = "Clear All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun DebugRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
