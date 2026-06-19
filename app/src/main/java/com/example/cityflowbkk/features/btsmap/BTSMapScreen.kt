package com.example.cityflowbkk.features.btsmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cityflowbkk.R
import com.example.cityflowbkk.features.btsmap.data.FareRepository
import com.example.cityflowbkk.features.btsmap.model.RouteResult
import com.example.cityflowbkk.features.stationmapping.model.StationCoordinate
import com.example.cityflowbkk.features.stationmapping.model.StationLine
import kotlin.math.min
import kotlin.math.sqrt

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 8f

private fun lineColor(line: StationLine): Color = when (line) {
    StationLine.SUKHUMVIT         -> Color(0xFF009D63)
    StationLine.SILOM             -> Color(0xFF003F87)
    StationLine.GOLD              -> Color(0xFFC9A84C)
    StationLine.AIRPORT_RAIL_LINK -> Color(0xFFCA3832)
    StationLine.MRT_BLUE          -> Color(0xFF1A3A8F)
    StationLine.MRT_PURPLE        -> Color(0xFF7B2D8B)
    StationLine.MRT_YELLOW        -> Color(0xFFF5C400)
    StationLine.MRT_PINK          -> Color(0xFFE3007F)
    StationLine.BTS_EXTENSION     -> Color(0xFF009D63)
}

// ─────────────────────────────────────────────────────────────────────────────
// Coordinate helpers
// ─────────────────────────────────────────────────────────────────────────────

data class ImageTransform(
    val renderedW: Float,
    val renderedH: Float,
    val imgLeft: Float,
    val imgTop: Float,
    val origW: Float,
    val origH: Float,
)

private fun buildTransform(
    containerW: Float,
    containerH: Float,
    origW: Float,
    origH: Float,
): ImageTransform {
    val fitScale  = min(containerW / origW, containerH / origH)
    val renderedW = origW * fitScale
    val renderedH = origH * fitScale
    val imgLeft   = (containerW - renderedW) / 2f
    val imgTop    = (containerH - renderedH) / 2f
    return ImageTransform(renderedW, renderedH, imgLeft, imgTop, origW, origH)
}

/** image pixel → final screen coordinates (Canvas has no graphicsLayer) */
private fun ImageTransform.toScreenCoords(
    sourceX: Float,
    sourceY: Float,
    zoom: Float,
    panOffset: Offset,
    containerW: Float,
    containerH: Float,
): Offset {
    val baseX = imgLeft + (sourceX / origW) * renderedW
    val baseY = imgTop  + (sourceY / origH) * renderedH
    val cx = containerW / 2f
    val cy = containerH / 2f
    return Offset(
        x = (baseX - cx) * zoom + cx + panOffset.x,
        y = (baseY - cy) * zoom + cy + panOffset.y,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// BTSMapScreen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BTSMapScreen(
    onBack: () -> Unit = {},
    viewModel: BTSMapViewModel = viewModel(),
) {
    val uiState    by viewModel.uiState.collectAsState()
    val sheetState  = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("BTS Skytrain Map", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    TextButton(onClick = { viewModel.toggleDebugMode() }) {
                        Text(
                            text  = if (uiState.debugMode) "Debug ON" else "Debug",
                            color = if (uiState.debugMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            ZoomableBtsMap(
                stations           = uiState.stations,
                originStation      = uiState.originStation,
                destinationStation = uiState.destinationStation,
                routeResult        = uiState.routeResult,
                originalImgW       = uiState.originalImgWidth,
                originalImgH       = uiState.originalImgHeight,
                debugMode          = uiState.debugMode,
                onStationClick     = viewModel::onStationClicked,
            )

            // Route info card — shown when both origin + destination selected
            if (uiState.routeResult != null) {
                RouteInfoCard(
                    routeResult        = uiState.routeResult!!,
                    originStation      = uiState.originStation,
                    destinationStation = uiState.destinationStation,
                    onClearOrigin      = viewModel::clearOrigin,
                    onClearDestination = viewModel::clearDestination,
                    onSwap             = viewModel::swapStations,
                    modifier           = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                )
            } else if (uiState.originStation != null || uiState.destinationStation != null) {
                // Only one station selected — show simple selection card
                TripSelectionCard(
                    origin             = uiState.originStation,
                    destination        = uiState.destinationStation,
                    onClearOrigin      = viewModel::clearOrigin,
                    onClearDestination = viewModel::clearDestination,
                    onSwap             = viewModel::swapStations,
                    modifier           = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (uiState.debugMode) {
                DebugInfoPanel(
                    imgWidth     = uiState.imgWidth,
                    imgHeight    = uiState.imgHeight,
                    originalImgW = uiState.originalImgWidth,
                    originalImgH = uiState.originalImgHeight,
                    stationCount = uiState.stations.size,
                    modifier     = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(start = 8.dp, top = 64.dp),
                )
            }
        }
    }

    if (uiState.selectedStation != null) {
        ModalBottomSheet(onDismissRequest = { viewModel.clearSelection() }, sheetState = sheetState) {
            StationDetailContent(
                station       = uiState.selectedStation!!,
                isOrigin      = uiState.selectedStation!!.stationId == uiState.originStation?.stationId,
                isDestination = uiState.selectedStation!!.stationId == uiState.destinationStation?.stationId,
                onDismiss     = { viewModel.clearSelection() },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ZoomableBtsMap
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ZoomableBtsMap(
    stations: List<StationCoordinate>,
    originStation: StationCoordinate?,
    destinationStation: StationCoordinate?,
    routeResult: RouteResult?,
    originalImgW: Float,
    originalImgH: Float,
    debugMode: Boolean,
    onStationClick: (StationCoordinate) -> Unit,
) {
    var userZoom   by remember { mutableFloatStateOf(1f) }
    var panOffset  by remember { mutableStateOf(Offset.Zero) }
    var containerW by remember { mutableFloatStateOf(0f) }
    var containerH by remember { mutableFloatStateOf(0f) }
    val textMeasurer = rememberTextMeasurer()

    // Set of station IDs on the active route for quick lookup
    val routeStationIds: Set<String> = remember(routeResult) {
        routeResult?.path?.map { it.id }?.toSet() ?: emptySet()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                containerW = size.width.toFloat()
                containerH = size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    userZoom = (userZoom * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                    val maxX = containerW * (userZoom - 1f) / 2f
                    val maxY = containerH * (userZoom - 1f) / 2f
                    panOffset = Offset(
                        (panOffset.x + pan.x).coerceIn(-maxX, maxX),
                        (panOffset.y + pan.y).coerceIn(-maxY, maxY),
                    )
                }
            }
            .pointerInput(stations, originalImgW, originalImgH) {
                detectTapGestures { tapOffset ->
                    if (containerW == 0f || originalImgW == 0f) return@detectTapGestures
                    val t         = buildTransform(containerW, containerH, originalImgW, originalImgH)
                    val hitRadius = 28.dp.toPx()
                    val hit = stations.minByOrNull { coord ->
                        val pos = t.toScreenCoords(coord.absX.toFloat(), coord.absY.toFloat(), userZoom, panOffset, containerW, containerH)
                        (tapOffset - pos).getDistance()
                    }
                    hit?.let { coord ->
                        val pos = t.toScreenCoords(coord.absX.toFloat(), coord.absY.toFloat(), userZoom, panOffset, containerW, containerH)
                        if ((tapOffset - pos).getDistance() < hitRadius) onStationClick(coord)
                    }
                }
            },
    ) {
        // ── Image ─────────────────────────────────────────────────────────
        Image(
            painter            = painterResource(R.drawable.btsmap),
            contentDescription = "BTS Skytrain Map",
            contentScale       = ContentScale.Fit,
            modifier           = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = userZoom, scaleY = userZoom, translationX = panOffset.x, translationY = panOffset.y),
        )

        // ── Canvas — markers + route highlight ────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (containerW == 0f || originalImgW == 0f) return@Canvas
            val t = buildTransform(containerW, containerH, originalImgW, originalImgH)

            // 1. Draw route polyline first (below markers)
            if (routeResult != null && routeResult.path.size >= 2) {
                val routePoints = routeResult.path.map { node ->
                    t.toScreenCoords(node.absX.toFloat(), node.absY.toFloat(), userZoom, panOffset, containerW, containerH)
                }
                // Glowing shadow
                for (i in 0 until routePoints.size - 1) {
                    drawLine(
                        color       = Color.White.copy(alpha = 0.5f),
                        start       = routePoints[i],
                        end         = routePoints[i + 1],
                        strokeWidth = 10.dp.toPx(),
                    )
                }
                // Coloured route line
                for (i in 0 until routePoints.size - 1) {
                    val segColor = lineColor(routeResult.path[i].line)
                    drawLine(
                        color       = segColor,
                        start       = routePoints[i],
                        end         = routePoints[i + 1],
                        strokeWidth = 5.dp.toPx(),
                        pathEffect  = null,
                    )
                }
            }

            // 2. Draw all station markers
            stations.forEach { coord ->
                val pos = t.toScreenCoords(coord.absX.toFloat(), coord.absY.toFloat(), userZoom, panOffset, containerW, containerH)

                android.util.Log.d("BTS_MARKER",
                    "${coord.stationId} image=(${coord.absX},${coord.absY}) screen=(${pos.x.toInt()},${pos.y.toInt()})")

                val isOrigin    = coord.stationId == originStation?.stationId
                val isDest      = coord.stationId == destinationStation?.stationId
                val isOnRoute   = routeStationIds.contains(coord.stationId)
                val isEndpoint  = isOrigin || isDest

                val radius = when {
                    isEndpoint -> 11.dp.toPx()
                    isOnRoute  -> 8.dp.toPx()
                    else       -> 5.dp.toPx()
                }
                val color = when {
                    isOrigin  -> Color(0xFF009D63)
                    isDest    -> Color(0xFFCA3832)
                    isOnRoute -> lineColor(coord.line)
                    else      -> lineColor(coord.line).copy(alpha = 0.55f)
                }
                val strokeW = when {
                    isEndpoint -> 3.dp.toPx()
                    isOnRoute  -> 2.dp.toPx()
                    else       -> 1.2.dp.toPx()
                }

                drawCircle(color, radius, pos)
                drawCircle(Color.White, radius, pos, style = Stroke(strokeW))

                // Label for route stations when zoomed in enough, always for endpoints
                if (debugMode || isEndpoint) {
                    drawStationLabel(textMeasurer, coord.stationId, coord.absX, coord.absY, pos, radius)
                }
            }
        }

        // ── Zoom controls ─────────────────────────────────────────────────
        Column(
            modifier            = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FloatingActionButton(
                onClick        = { userZoom = (userZoom * 1.2f).coerceIn(MIN_ZOOM, MAX_ZOOM) },
                modifier       = Modifier.size(48.dp),
                shape          = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation      = FloatingActionButtonDefaults.elevation(4.dp),
            ) { Icon(Icons.Default.Add, "Zoom In", Modifier.size(22.dp)) }

            FloatingActionButton(
                onClick        = { userZoom = (userZoom / 1.2f).coerceIn(MIN_ZOOM, MAX_ZOOM) },
                modifier       = Modifier.size(48.dp),
                shape          = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation      = FloatingActionButtonDefaults.elevation(4.dp),
            ) { Icon(Icons.Default.Remove, "Zoom Out", Modifier.size(22.dp)) }

            Card(
                shape     = RoundedCornerShape(8.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Text(
                    text       = "Zoom: ${"%.1f".format(userZoom)}x",
                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Route info card  (shown when both origin + destination are set)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RouteInfoCard(
    routeResult: RouteResult,
    originStation: StationCoordinate?,
    destinationStation: StationCoordinate?,
    onClearOrigin: () -> Unit,
    onClearDestination: () -> Unit,
    onSwap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fare = FareRepository.getFare(routeResult.stationCount, 
        routeResult.path.firstOrNull()?.line ?: com.example.cityflowbkk.features.stationmapping.model.StationLine.SUKHUMVIT)
    val fareText = if (fare != null && fare > 0) "$fare THB" else "Fare unavailable"
    val timeText = "${FareRepository.estimateTime(routeResult.stationCount, routeResult.edges.count { it.isTransfer })} min"
    val stationNames = routeResult.path.map { it.nameEn }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // ── Origin / Destination row ──────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    StationRow("From", originStation, Color(0xFF009D63), onClearOrigin)
                    HorizontalDivider(
                        modifier  = Modifier.padding(vertical = 6.dp, horizontal = 32.dp),
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant,
                    )
                    StationRow("To", destinationStation, Color(0xFFCA3832), onClearDestination)
                }
                IconButton(
                    onClick  = onSwap,
                    modifier = Modifier.padding(start = 8.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                ) {
                    Icon(Icons.Default.SwapVert, "Swap", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Fare + stats row ──────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                RouteStatChip(
                    label = "Stations",
                    value = "${routeResult.stationCount}",
                )
                RouteStatChip(
                    label = "Fare",
                    value = fareText,
                    highlight = true,
                )
                RouteStatChip(
                    label = "Time",
                    value = timeText,
                )
            }

            // ── Scrollable station list ───────────────────────────────────
            if (stationNames.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    "Route",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    stationNames.forEachIndexed { index, name ->
                        Text(
                            name,
                            style     = MaterialTheme.typography.labelSmall,
                            color     = MaterialTheme.colorScheme.onSurface,
                            maxLines  = 1,
                            overflow  = TextOverflow.Ellipsis,
                            modifier  = Modifier.widthIn(max = 120.dp),
                        )
                        if (index < stationNames.size - 1) {
                            Text(
                                "→",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteStatChip(
    label: String,
    value: String,
    highlight: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            value,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = if (highlight) Color(0xFF009D63) else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Canvas draw helper
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawStationLabel(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    stationId: String,
    sourceX: Int,
    sourceY: Int,
    screenPos: Offset,
    radius: Float,
) {
    val text   = "$stationId\n($sourceX,$sourceY)"
    val result = textMeasurer.measure(
        text, TextStyle(color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    )
    val padX = 4f
    val padY = 3f
    val left = screenPos.x - result.size.width / 2f - padX
    val top  = screenPos.y - radius - result.size.height - padY * 2 - 8f
    drawRect(
        color   = Color.White.copy(alpha = 0.92f),
        topLeft = Offset(left, top - padY),
        size    = Size(result.size.width + padX * 2, result.size.height + padY * 2),
    )
    drawText(result, topLeft = Offset(left + padX, top))
}

// ─────────────────────────────────────────────────────────────────────────────
// Debug info panel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DebugInfoPanel(
    imgWidth: Int,
    imgHeight: Int,
    originalImgW: Float,
    originalImgH: Float,
    stationCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier  = modifier.widthIn(max = 240.dp),
        shape     = RoundedCornerShape(8.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.78f)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("DEBUG", color = Color.Yellow, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            DebugLine("Drawable",  "${imgWidth} × ${imgHeight} px")
            DebugLine("Orig size", "${originalImgW.toInt()} × ${originalImgH.toInt()} px")
            DebugLine("Stations",  "$stationCount loaded")
            DebugLine("Coords",    "BTS_CAL_RESULT source")
            DebugLine("Fares",     if (FareRepository.isReady()) "Loaded" else "Fallback")
        }
    }
}

@Composable
private fun DebugLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Color(0xFFAAAAAA), style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(76.dp))
        Text(value, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Trip selection card  (shown when only one station is selected)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TripSelectionCard(
    origin: StationCoordinate?,
    destination: StationCoordinate?,
    onClearOrigin: () -> Unit,
    onClearDestination: () -> Unit,
    onSwap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    StationRow("From", origin,      Color(0xFF009D63), onClearOrigin)
                    HorizontalDivider(
                        modifier  = Modifier.padding(vertical = 8.dp, horizontal = 32.dp),
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant,
                    )
                    StationRow("To", destination, Color(0xFFCA3832), onClearDestination)
                }
                IconButton(
                    onClick  = onSwap,
                    modifier = Modifier.padding(start = 8.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                ) {
                    Icon(Icons.Default.SwapVert, "Swap", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

@Composable
private fun StationRow(
    label: String,
    station: StationCoordinate?,
    dotColor: Color,
    onClear: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(dotColor))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                station?.stationName ?: "Select station...",
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = if (station != null) FontWeight.Bold else FontWeight.Normal,
                color      = if (station != null) MaterialTheme.colorScheme.onSurface
                             else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
            station?.let {
                Text(it.line.displayName, style = MaterialTheme.typography.labelSmall,
                    color = lineColor(it.line), fontWeight = FontWeight.Medium)
            }
        }
        if (station != null) {
            IconButton(onClick = onClear, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.Close, "Clear", modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Station detail bottom sheet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StationDetailContent(
    station: StationCoordinate,
    isOrigin: Boolean,
    isDestination: Boolean,
    onDismiss: () -> Unit,
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier         = Modifier.size(48.dp).clip(CircleShape).background(lineColor(station.line)),
            contentAlignment = Alignment.Center,
        ) {
            Text(station.stationId, color = Color.White,
                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        Text(station.stationName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            station.line.displayName,
            style      = MaterialTheme.typography.bodyMedium,
            color      = lineColor(station.line),
            fontWeight = FontWeight.Medium,
        )
        when {
            isOrigin      -> StatusChip("Origin",      Color(0xFF009D63))
            isDestination -> StatusChip("Destination", Color(0xFFCA3832))
        }
        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                "Image coords: (${station.absX}, ${station.absY})",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Text("Close")
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.12f)) {
        Text(
            label,
            modifier   = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style      = MaterialTheme.typography.labelLarge,
            color      = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
