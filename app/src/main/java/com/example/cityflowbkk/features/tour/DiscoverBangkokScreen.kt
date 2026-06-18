package com.example.cityflowbkk.features.tour

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.cityflowbkk.features.tour.data.AttractionUiModel
import com.example.cityflowbkk.ui.theme.CityFlowBlue
import com.example.cityflowbkk.ui.theme.CityFlowGreen
import com.example.cityflowbkk.ui.theme.CityFlowOrange
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverBangkokScreen(
    viewModel: DiscoverViewModel,
    onBackClick: () -> Unit,
    onSavedPlacesClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover Bangkok", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = onSavedPlacesClick) {
                            Text("❤️", fontSize = 24.sp)
                        }
                        if (uiState.savedCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp),
                                containerColor = CityFlowOrange
                            ) {
                                Text(uiState.savedCount.toString())
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.errorMessage != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.isAllExplored -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "All Caught Up!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You've seen all popular attractions.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onSavedPlacesClick) {
                            Text("View Saved Places")
                        }
                    }
                }
                uiState.currentAttraction != null -> {
                    val attraction = uiState.currentAttraction!!
                    key(attraction.id) {
                        SwipeableCard(
                            attraction = attraction,
                            onSwipeLeft = { viewModel.onSwipeLeft() },
                            onSwipeRight = { viewModel.onSwipeRight() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableCard(
    attraction: AttractionUiModel,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val swipeThreshold = 300f

    val rotation = offsetX.value / 20f
    var isSwiped by remember { mutableStateOf(false) }

    val handleSwipe: (Boolean) -> Unit = { isRight ->
        if (!isSwiped) {
            isSwiped = true
            coroutineScope.launch {
                offsetX.animateTo(
                    targetValue = if (isRight) 1000f else -1000f,
                    animationSpec = tween(durationMillis = 300)
                )
                if (isRight) onSwipeRight() else onSwipeLeft()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .graphicsLayer(
                rotationZ = rotation,
                alpha = 1f - (abs(offsetX.value) / 1000f).coerceIn(0f, 0.5f)
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (!isSwiped) {
                            if (offsetX.value > swipeThreshold) {
                                handleSwipe(true)
                            } else if (offsetX.value < -swipeThreshold) {
                                handleSwipe(false)
                            } else {
                                coroutineScope.launch {
                                    offsetX.animateTo(0f)
                                    offsetY.animateTo(0f)
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        if (!isSwiped) {
                            change.consume()
                            coroutineScope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                offsetY.snapTo(offsetY.value + dragAmount.y)
                            }
                        }
                    }
                )
            }
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // ── Image layer ──────────────────────────────────────────────
                val context = LocalContext.current
                var imageLoadState by remember(attraction.photoUrl) {
                    mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Loading(null))
                }

                if (attraction.photoUrl != null) {
                    Log.d("DiscoverScreen", "Loading image for '${attraction.name}' → ${attraction.photoUrl}")

                    val imageRequest = remember(attraction.photoUrl) {
                        ImageRequest.Builder(context)
                            .data(attraction.photoUrl)
                            .crossfade(true)
                            .build()
                    }

                    AsyncImage(
                        model = imageRequest,
                        contentDescription = attraction.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onLoading = { state ->
                            imageLoadState = state
                        },
                        onSuccess = { state ->
                            imageLoadState = state
                            Log.d("DiscoverScreen", "Image loaded successfully for '${attraction.name}'")
                        },
                        onError = { state ->
                            imageLoadState = state
                            Log.e(
                                "DiscoverScreen",
                                "Image load failed for '${attraction.name}' URL='${attraction.photoUrl}'",
                                state.result.throwable
                            )
                        }
                    )

                    // Show spinner while loading
                    if (imageLoadState is AsyncImagePainter.State.Loading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    // Show error placeholder if load failed
                    if (imageLoadState is AsyncImagePainter.State.Error) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CityFlowBlue.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "📷 Image unavailable",
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // No photo URL → placeholder
                    Log.w("DiscoverScreen", "No photo URL for '${attraction.name}' — showing placeholder")
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CityFlowBlue.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📷 No photo available", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                    }
                }

                // ── Gradient overlay ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                startY = 300f
                            )
                        )
                )

                // ── Content overlay ──────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                        .padding(bottom = 72.dp) // space for buttons
                ) {
                    // Category chip
                    Surface(
                        shape = CircleShape,
                        color = CityFlowOrange.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = attraction.category,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Name
                    Text(
                        text = attraction.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Rating — always shown (N/A fallback)
                    val ratingText = if (attraction.rating != null) {
                        "⭐ ${"%.1f".format(attraction.rating)}"
                    } else {
                        "⭐ N/A"
                    }
                    Text(
                        text = ratingText,
                        color = Color(0xFFFFD700), // gold
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Review count — always shown
                    val reviewText = "(${attraction.userRatingsTotal ?: 0} Reviews)"
                    Text(
                        text = reviewText,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text(
                        text = attraction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Address
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📍 ${attraction.address ?: "Bangkok, Thailand"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Opening hours — show today's entry or open/closed status
                    Spacer(modifier = Modifier.height(4.dp))
                    val hoursText = when {
                        attraction.openingHours.isNotEmpty() -> {
                            // weekdayDescriptions index 0 = Monday … 6 = Sunday
                            // java.util.Calendar.DAY_OF_WEEK: Sunday=1 … Saturday=7
                            val dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
                            // convert to Mon=0 … Sun=6
                            val idx = (dayOfWeek + 5) % 7
                            val entry = attraction.openingHours.getOrNull(idx) ?: attraction.openingHours.first()
                            // Strip the day name prefix (e.g. "Monday: 08:00 – 18:00" → "08:00 – 18:00")
                            val timePart = entry.substringAfter(": ", entry)
                            "🕒 $timePart"
                        }
                        attraction.isOpenNow != null -> {
                            if (attraction.isOpenNow) "🕒 Open Now" else "🕒 Closed"
                        }
                        else -> "🕒 Hours Unavailable"
                    }
                    Text(
                        text = hoursText,
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            attraction.isOpenNow == true -> CityFlowGreen
                            attraction.isOpenNow == false -> Color.Red.copy(alpha = 0.8f)
                            else -> Color.White.copy(alpha = 0.7f)
                        },
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { handleSwipe(false) },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Text("❌", fontSize = 28.sp)
                    }
                    IconButton(
                        onClick = { handleSwipe(true) },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Text("❤️", fontSize = 28.sp)
                    }
                }
                
                // Swipe overlays
                if (offsetX.value > 50f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CityFlowGreen.copy(alpha = (offsetX.value / 500f).coerceIn(0f, 0.4f))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("❤️", fontSize = 120.sp)
                    }
                } else if (offsetX.value < -50f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red.copy(alpha = (abs(offsetX.value) / 500f).coerceIn(0f, 0.4f))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("❌", fontSize = 120.sp)
                    }
                }
            }
        }
    }
}
