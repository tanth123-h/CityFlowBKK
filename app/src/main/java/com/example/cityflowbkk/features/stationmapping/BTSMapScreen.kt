package com.example.cityflowbkk.features.stationmapping

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cityflowbkk.R
import com.example.cityflowbkk.features.stationmapping.model.BtsStation
import com.example.cityflowbkk.features.stationmapping.model.StationCoordinate
import com.example.cityflowbkk.features.stationmapping.model.StationLine

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 8f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BTSMapScreen(
    onBack: () -> Unit = {},
    viewModel: BTSMapViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.exportPath) {
        uiState.exportPath?.let {
            snackbarHostState.showSnackbar("Exported to: $it")
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Station Mapping Tool") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.export() }) {
                        Text("Export JSON", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            StationControls(
                stations = uiState.allStations,
                selectedStation = uiState.selectedStation,
                onStationSelected = viewModel::onStationSelected
            )
            
            CoordinateDisplay(uiState.mappedCoordinates.lastOrNull())

            Box(modifier = Modifier.weight(1f)) {
                ZoomableMap(
                    coordinates = uiState.mappedCoordinates,
                    onMapTapped = viewModel::onMapTapped,
                    onMarkerTapped = viewModel::onMarkerTapped
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationControls(
    stations: List<BtsStation>,
    selectedStation: BtsStation?,
    onStationSelected: (BtsStation?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedStation?.name ?: "Select a station...",
                onValueChange = {},
                readOnly = true,
                label = { Text("Target Station") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                stations.forEach { station ->
                    DropdownMenuItem(
                        text = { Text(station.name) },
                        onClick = {
                            onStationSelected(station)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CoordinateDisplay(lastCoord: StationCoordinate?) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Last Tap: ", style = MaterialTheme.typography.labelMedium)
            if (lastCoord != null) {
                Text(
                    "X: ${lastCoord.absX}, Y: ${lastCoord.absY} (Pixel)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            } else {
                Text("Tap map to start", color = Color.Gray)
            }
        }
    }
}

@Composable
private fun ZoomableMap(
    coordinates: List<StationCoordinate>,
    onMapTapped: (Float, Float) -> Unit,
    onMarkerTapped: (StationCoordinate) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                    offset += pan
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val containerW = size.width.toFloat()
                    val containerH = size.height.toFloat()
                    val imgW = imageSize.width.toFloat().takeIf { it > 0 } ?: containerW
                    val imgH = imageSize.height.toFloat().takeIf { it > 0 } ?: containerH
                    
                    val scaleX = containerW / imgW
                    val scaleY = containerH / imgH
                    val fitScale = minOf(scaleX, scaleY)
                    
                    val renderedW = imgW * fitScale * scale
                    val renderedH = imgH * fitScale * scale
                    
                    val imgLeft = (containerW - renderedW) / 2f + offset.x
                    val imgTop = (containerH - renderedH) / 2f + offset.y

                    val relX = (tapOffset.x - imgLeft) / renderedW
                    val relY = (tapOffset.y - imgTop) / renderedH

                    if (relX in 0f..1f && relY in 0f..1f) {
                        onMapTapped(relX, relY)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.btsmap),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { imageSize = it }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            val containerW = size.width
            val containerH = size.height
            val imgW = imageSize.width.toFloat().takeIf { it > 0 } ?: containerW
            val imgH = imageSize.height.toFloat().takeIf { it > 0 } ?: containerH
            
            val scaleX = containerW / imgW
            val scaleY = containerH / imgH
            val fitScale = minOf(scaleX, scaleY)
            val renderedW = imgW * fitScale
            val renderedH = imgH * fitScale
            val imgLeft = (containerW - renderedW) / 2f
            val imgTop = (containerH - renderedH) / 2f

            coordinates.forEach { coord ->
                val cx = imgLeft + coord.x * renderedW
                val cy = imgTop + coord.y * renderedH
                
                drawCircle(
                    color = Color.Red,
                    radius = 8.dp.toPx() / scale,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx() / scale,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.dp.toPx() / scale)
                )
            }
        }
    }
}
