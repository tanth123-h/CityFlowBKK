package com.example.cityflowbkk.features.tour

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cityflowbkk.features.tour.data.AttractionUiModel
import com.example.cityflowbkk.ui.theme.CityFlowBlue
import com.example.cityflowbkk.ui.theme.CityFlowGreen
import com.example.cityflowbkk.ui.theme.CityFlowOrange
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPlaceDetailScreen(
    attraction: AttractionUiModel,
    onBackClick: () -> Unit,
    viewModel: SavedPlaceDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load on first composition only
    LaunchedEffect(attraction.id) {
        viewModel.load(attraction)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Scrollable content ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero Image ──────────────────────────────────────────────────
            HeroSection(uiState = uiState)

            // ── Info cards ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Loading enrichment indicator
                if (uiState.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                        color = CityFlowBlue
                    )
                }

                // Partial-data warning
                if (uiState.errorMessage != null && !uiState.isLoading) {
                    DetailCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚠️", fontSize = 18.sp)
                            Text(
                                text = "Showing cached data — full details unavailable",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ── Basic info ────────────────────────────────────────────
                DetailCard {
                    DetailRow(icon = "📍", label = "Address", value = uiState.address ?: "Bangkok, Thailand")

                    val phone = uiState.phoneNumber
                    if (phone != null) {
                        Spacer(Modifier.height(2.dp))
                        ClickableDetailRow(
                            icon = "📞",
                            label = "Phone",
                            value = phone,
                            uri = "tel:$phone"
                        )
                    }

                    val website = uiState.website
                    if (website != null) {
                        Spacer(Modifier.height(2.dp))
                        ClickableDetailRow(
                            icon = "🌐",
                            label = "Website",
                            value = website.removePrefix("https://").removePrefix("http://").trimEnd('/'),
                            uri = website
                        )
                    }

                    val placeId = uiState.placeId
                    if (placeId != null) {
                        Spacer(Modifier.height(2.dp))
                        DetailRow(icon = "🆔", label = "Place ID", value = placeId)
                    }
                }

                // ── Opening status + today's hours ───────────────────────
                DetailCard {
                    OpeningStatusSection(uiState = uiState)
                }

                // ── Description ───────────────────────────────────────────
                DetailCard {
                    SectionLabel("📝 Description")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = uiState.description.ifBlank { "No description available." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }

                // ── Category ──────────────────────────────────────────────
                DetailCard {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = CityFlowOrange.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "🏷 ${uiState.category}",
                                style = MaterialTheme.typography.labelLarge,
                                color = CityFlowOrange,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // ── Full opening hours ────────────────────────────────────
                if (uiState.openingHours.isNotEmpty()) {
                    DetailCard {
                        SectionLabel("🕒 Opening Hours")
                        Spacer(Modifier.height(8.dp))
                        val todayIdx = todayWeekdayIndex()
                        uiState.openingHours.forEachIndexed { index, entry ->
                            val isToday = index == todayIdx
                            Text(
                                text = entry,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isToday) CityFlowBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                // ── Map section ───────────────────────────────────────────
                val lat = uiState.latitude
                val lng = uiState.longitude
                if (lat != null && lng != null) {
                    AnimatedVisibility(visible = true, enter = fadeIn()) {
                        MapPreviewCard(
                            name = uiState.name,
                            address = uiState.address,
                            lat = lat,
                            lng = lng
                        )
                    }
                }
            }
        }

        // ── Back button pinned over the hero ───────────────────────────────
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(12.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
        ) {
            Text("←", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Hero section ──────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(uiState: SavedPlaceDetailUiState) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        // Image
        val photoUrl = uiState.photoUrl
        if (photoUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = uiState.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CityFlowBlue, CityFlowBlue.copy(alpha = 0.5f))
                        )
                    )
            )
        }

        // Gradient scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.75f)),
                        startY = 150f
                    )
                )
        )

        // Text overlay at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = uiState.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rating badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.92f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("⭐", fontSize = 14.sp)
                        Text(
                            text = uiState.rating?.let { "%.1f".format(it) } ?: "N/A",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1B1B1F)
                        )
                    }
                }
                Text(
                    text = "${uiState.userRatingsTotal?.let { formatReviewCount(it) } ?: "0"} Reviews",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Opening status ────────────────────────────────────────────────────────────

@Composable
private fun OpeningStatusSection(uiState: SavedPlaceDetailUiState) {
    SectionLabel("🕒 Opening Status")
    Spacer(Modifier.height(8.dp))

    // Status badge
    val (statusText, statusColor) = when (uiState.isOpenNow) {
        true -> "Open Now" to CityFlowGreen
        false -> "Closed" to Color(0xFFE53935)
        null -> "Status Unavailable" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = statusColor.copy(alpha = 0.12f)
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelLarge,
            color = statusColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }

    // Today's hours
    val todayIdx = todayWeekdayIndex()
    val todayEntry = uiState.openingHours.getOrNull(todayIdx)
    if (todayEntry != null) {
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Today",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = todayEntry.substringAfter(": ", todayEntry),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    } else if (uiState.openingHours.isEmpty()) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Hours Unavailable",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Map preview card ──────────────────────────────────────────────────────────

@Composable
private fun MapPreviewCard(
    name: String,
    address: String?,
    lat: Double,
    lng: Double
) {
    val position = LatLng(lat, lng)
    val cameraState = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, 15f)
    }

    DetailCard {
        SectionLabel("📍 Location")
        Spacer(Modifier.height(10.dp))

        // Embedded map — no gestures, no controls, location preview only
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                uiSettings = MapUiSettings(
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    zoomControlsEnabled = false,
                    compassEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false
                ),
                properties = MapProperties(isMyLocationEnabled = false)
            ) {
                Marker(
                    state = MarkerState(position = position),
                    title = name,
                    snippet = address
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = address ?: "Bangkok, Thailand",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Reusable composables ──────────────────────────────────────────────────────

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun DetailRow(icon: String, label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$icon $label",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ClickableDetailRow(icon: String, label: String, value: String, uri: String) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .clickable {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
                }
            }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$icon $label",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = CityFlowBlue,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Returns index into weekdayDescriptions (Mon=0 … Sun=6). */
private fun todayWeekdayIndex(): Int {
    val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) // Sun=1…Sat=7
    return (dayOfWeek + 5) % 7 // Mon=0…Sun=6
}

private fun formatReviewCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0)
        count >= 1_000 -> "%.1fK".format(count / 1_000.0)
        else -> count.toString()
    }
}
