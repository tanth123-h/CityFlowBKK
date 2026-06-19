package com.example.cityflowbkk.features.stationmapping

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cityflowbkk.R
import com.example.cityflowbkk.features.stationmapping.model.BtsStation
import com.example.cityflowbkk.features.stationmapping.model.StationCoordinate
import com.example.cityflowbkk.features.stationmapping.model.StationLine
import kotlinx.coroutines.launch

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 8f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationMappingScreen(
    onBack: () -> Unit = {},
    viewModel: StationMappingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showStationPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<StationCoordinate?>(null) }

    // Show export / error snackbar
    LaunchedEffect(uiState.exportPath, uiState.errorMessage) {
        when {
            uiState.exportPath != null -> {
                snackbarHostState.showSnackbar("Exported to: ${uiState.exportPath}")
                viewModel.clearExportMessage()
            }
            uiState.errorMessage != null -> {
                snackbarHostState.showSnackbar(uiState.errorMessage!!)
                viewModel.clearExportMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Column {
                        Text(
                            "Station Mapping Tool",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "${uiState.mappedCoordinates.size} / ${uiState.allStations.size} mapped",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.exportCoordinates() }) {
                        Text("Export", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Station selector bar
            StationSelectorBar(
                selectedStation = uiState.selectedStation,
                editingCoordinate = uiState.editingCoordinate,
                onSelectClick = { showStationPicker = true },
                onCancelEdit = { viewModel.dismissEditing() },
            )

            // Zoomable map with markers
            ZoomableMapWithMarkers(
                coordinates = uiState.mappedCoordinates,
                selectedStation = uiState.selectedStation,
                editingCoordinate = uiState.editingCoordinate,
                onMapTapped = { x, y ->
                    if (uiState.editingCoordinate != null) {
                        viewModel.onEditingMarkerMoved(x, y)
                    } else {
                        viewModel.onMapTapped(x, y)
                    }
                },
                onMarkerTapped = { coord ->
                    showDeleteConfirm = coord
                    viewModel.onMarkerTapped(coord)
                },
                modifier = Modifier.weight(1f),
            )
        }
    }

    // Station picker bottom sheet
    if (showStationPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showStationPicker = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            StationPickerSheet(
                stations = uiState.filteredStations,
                mappedIds = uiState.mappedStationIds,
                searchQuery = uiState.searchQuery,
                onSearchChanged = viewModel::onSearchQueryChanged,
                onStationSelected = { station ->
                    viewModel.onStationSelected(station)
                    scope.launch { sheetState.hide() }
                    showStationPicker = false
                },
            )
        }
    }

    // Delete confirmation dialog
    showDeleteConfirm?.let { coord ->
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = null
                viewModel.dismissEditing()
            },
            title = { Text("Delete Marker") },
            text = { Text("Remove ${coord.stationName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMarker(coord.stationId)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirm = null
                    viewModel.dismissEditing()
                }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun StationSelectorBar(
    selectedStation: BtsStation?,
    editingCoordinate: StationCoordinate?,
    onSelectClick: () -> Unit,
    onCancelEdit: () -> Unit,
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when {
                editingCoordinate != null -> {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(lineColor(editingCoordinate.line)),
                    )
                    Text(
                        text = "Tap map to move: ${editingCoordinate.stationName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TextButton(onClick = onCancelEdit) { Text("Cancel") }
                }
                selectedStation != null -> {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(lineColor(selectedStation.line)),
                    )
                    Text(
                        text = "Tap map to place: ${selectedStation.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TextButton(onClick = onSelectClick) { Text("Change") }
                }
                else -> {
                    Text(
                        text = "Select a station to place on map",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Button(onClick = onSelectClick, modifier = Modifier.height(36.dp)) {
                        Text("Select Station", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomableMapWithMarkers(
    coordinates: List<StationCoordinate>,
    selectedStation: BtsStation?,
    editingCoordinate: StationCoordinate?,
    onMapTapped: (Float, Float) -> Unit,
    onMarkerTapped: (StationCoordinate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                    val maxX = (size.width * (newScale - 1f)) / 2f
                    val maxY = (size.height * (newScale - 1f)) / 2f
                    offset = Offset(
                        x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                        y = (offset.y + pan.y).coerceIn(-maxY, maxY),
                    )
                    scale = newScale
                }
            }
            .pointerInput(selectedStation, editingCoordinate) {
                detectTapGestures { tapOffset ->
                    // Convert screen tap → normalised image coord
                    val containerW = size.width.toFloat()
                    val containerH = size.height.toFloat()
                    // Centre of scaled image
                    val imgW = imageSize.width.toFloat().takeIf { it > 0 } ?: containerW
                    val imgH = imageSize.height.toFloat().takeIf { it > 0 } ?: containerH
                    val renderedW = imgW * scale
                    val renderedH = imgH * scale
                    val imgLeft = (containerW - renderedW) / 2f + offset.x
                    val imgTop = (containerH - renderedH) / 2f + offset.y

                    val relX = (tapOffset.x - imgLeft) / renderedW
                    val relY = (tapOffset.y - imgTop) / renderedH

                    if (relX !in 0f..1f || relY !in 0f..1f) return@detectTapGestures

                    // Check if tapping an existing marker (within 24dp radius)
                    val tapThreshold = 24.dp.toPx() / scale
                    val hit = coordinates.firstOrNull { coord ->
                        val markerX = imgLeft + coord.x * renderedW
                        val markerY = imgTop + coord.y * renderedH
                        val dx = tapOffset.x - markerX
                        val dy = tapOffset.y - markerY
                        kotlin.math.sqrt(dx * dx + dy * dy) < tapThreshold
                    }
                    if (hit != null) {
                        onMarkerTapped(hit)
                    } else if (selectedStation != null || editingCoordinate != null) {
                        onMapTapped(relX, relY)
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // Map image
        Image(
            painter = painterResource(R.drawable.btsmap),
            contentDescription = "BTS Map",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { imageSize = it }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                ),
        )

        // Markers overlay
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                ),
        ) {
            val imgW = imageSize.width.toFloat().takeIf { it > 0 } ?: size.width
            val imgH = imageSize.height.toFloat().takeIf { it > 0 } ?: size.height
            val renderedW: Float
            val renderedH: Float
            val imgLeft: Float
            val imgTop: Float

            // Match ContentScale.Fit logic
            val scaleX = size.width / imgW
            val scaleY = size.height / imgH
            val fitScale = minOf(scaleX, scaleY)
            renderedW = imgW * fitScale
            renderedH = imgH * fitScale
            imgLeft = (size.width - renderedW) / 2f
            imgTop = (size.height - renderedH) / 2f

            val markerRadius = 10.dp.toPx() / scale
            val strokeWidth = 2.dp.toPx() / scale

            coordinates.forEach { coord ->
                val cx = imgLeft + coord.x * renderedW
                val cy = imgTop + coord.y * renderedH
                val color = lineColor(coord.line)
                val isEditing = editingCoordinate?.stationId == coord.stationId

                // Filled circle
                drawCircle(
                    color = color,
                    radius = if (isEditing) markerRadius * 1.5f else markerRadius,
                    center = Offset(cx, cy),
                )
                // White border
                drawCircle(
                    color = Color.White,
                    radius = if (isEditing) markerRadius * 1.5f else markerRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeWidth),
                )
            }
        }
    }
}

@Composable
private fun StationPickerSheet(
    stations: List<BtsStation>,
    mappedIds: Set<String>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    onStationSelected: (BtsStation) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            "Select Station",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            placeholder = { Text("Search station...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(stations, key = { it.id }) { station ->
                StationPickerItem(
                    station = station,
                    isMapped = station.id in mappedIds,
                    onSelected = { onStationSelected(station) },
                )
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun StationPickerItem(
    station: BtsStation,
    isMapped: Boolean,
    onSelected: () -> Unit,
) {
    Card(
        onClick = onSelected,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMapped)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(lineColor(station.line)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = station.line.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isMapped) {
                Text(
                    "✓",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun lineColor(line: StationLine): Color = when (line) {
    StationLine.SUKHUMVIT -> Color(0xFF009D63)
    StationLine.SILOM -> Color(0xFF00338D)
    StationLine.GOLD -> Color(0xFFC9A84C)
    StationLine.AIRPORT_RAIL_LINK -> Color(0xFFCA3832)
    StationLine.MRT_BLUE -> Color(0xFF1A3A8F)
    StationLine.MRT_PURPLE -> Color(0xFF7B2D8B)
    StationLine.MRT_YELLOW -> Color(0xFFF5C400)
    StationLine.MRT_PINK -> Color(0xFFE3007F)
    StationLine.BTS_EXTENSION -> Color(0xFF009D63)
}
