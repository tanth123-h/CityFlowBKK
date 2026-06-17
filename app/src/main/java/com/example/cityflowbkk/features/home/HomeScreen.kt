package com.example.cityflowbkk.features.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cityflowbkk.R
import com.example.cityflowbkk.features.place.PlaceDetailBottomSheet
import com.example.cityflowbkk.features.place.PlaceDetailViewModel
import com.example.cityflowbkk.ui.icons.HomeIcon
import com.example.cityflowbkk.ui.icons.HomeIconGraphic
import com.example.cityflowbkk.ui.theme.CityFlowBKKTheme
import com.example.cityflowbkk.ui.theme.CityFlowBlue
import com.example.cityflowbkk.ui.theme.CityFlowGreen
import com.example.cityflowbkk.ui.theme.CityFlowOrange

@Immutable
data class HomeUiState(
    val quickActions: List<QuickActionUiModel> = sampleQuickActions,
    val popularPlaces: List<PopularPlaceUiModel> = samplePopularPlaces,
)

@Immutable
data class QuickActionUiModel(
    val title: String,
    val icon: HomeIcon,
    val accentColor: Color,
)

@Immutable
data class PopularPlaceUiModel(
    val name: String,
    val nearestStation: String,
    val rating: String,
    val imageColor: Color,
    val imageResId: Int? = null,
    val placeId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Composable
fun HomeScreen(
    uiState: HomeUiState = HomeUiState(),
    onPlanRouteClick: () -> Unit = {},
    onQuickActionClick: (QuickActionUiModel) -> Unit = {},
    placeDetailViewModel: PlaceDetailViewModel = viewModel(),
) {
    var selectedPopularPlace by remember { mutableStateOf<PopularPlaceUiModel?>(null) }
    val placeDetailUiState by placeDetailViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CityFlowTopAppBar()
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundDecorations()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                HeroCard(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    onPlanRouteClick = onPlanRouteClick
                )

                QuickActionsGrid(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    quickActions = uiState.quickActions,
                    onQuickActionClick = onQuickActionClick,
                )

                PopularPlacesSection(
                    places = uiState.popularPlaces,
                    onPlaceClick = { place ->
                        selectedPopularPlace = place
                        placeDetailViewModel.loadPlace(place)
                    },
                )
            }
        }
    }

    if (selectedPopularPlace != null) {
        PlaceDetailBottomSheet(
            uiState = placeDetailUiState,
            onDismiss = {
                selectedPopularPlace = null
                placeDetailViewModel.clear()
            },
        )
    }
}

@Composable
private fun BackgroundDecorations() {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CityFlowBlue.copy(alpha = 0.05f), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.1f),
                    radius = size.width * 0.6f
                ),
                center = Offset(size.width * 0.9f, size.height * 0.1f),
                radius = size.width * 0.6f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CityFlowGreen.copy(alpha = 0.04f), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.4f),
                    radius = size.width * 0.5f
                ),
                center = Offset(size.width * 0.1f, size.height * 0.4f),
                radius = size.width * 0.5f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityFlowTopAppBar() {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CityFlowLogo()
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Bangkok Transit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        },
        actions = {
            Box(modifier = Modifier.padding(end = 8.dp)) {
                IconButton(onClick = { }) {
                    HomeIconGraphic(
                        icon = HomeIcon.Notification,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp),
                    containerColor = CityFlowOrange,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

@Composable
private fun CityFlowLogo() {
    Image(
        painter = painterResource(R.drawable.chatgpt_image_jun_15__2026__11_17_32_pm),
        contentDescription = null,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun HeroCard(
    modifier: Modifier = Modifier,
    onPlanRouteClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.banner_home),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 100f
                        )
                    ),
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawPath(
                            path = Path().apply {
                                moveTo(size.width, 0f)
                                quadraticTo(size.width * 0.7f, size.height * 0.2f, size.width * 0.8f, size.height * 0.5f)
                                quadraticTo(size.width * 0.9f, size.height * 0.8f, size.width, size.height)
                            },
                            color = Color.White.copy(alpha = 0.1f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Explore Bangkok",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Smart & Easy Navigation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
                Button(
                    onClick = onPlanRouteClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    HomeIconGraphic(
                        icon = HomeIcon.Route,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Plan Route", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    modifier: Modifier = Modifier,
    quickActions: List<QuickActionUiModel>,
    onQuickActionClick: (QuickActionUiModel) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(title = "Quick Actions")
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            quickActions.chunked(3).forEach { rowActions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowActions.forEach { action ->
                        QuickActionCard(
                            action = action,
                            onClick = { onQuickActionClick(action) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(3 - rowActions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    action: QuickActionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(110.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(action.accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                HomeIconGraphic(
                    icon = action.icon,
                    contentDescription = null,
                    tint = action.accentColor,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun PopularPlacesSection(
    places: List<PopularPlaceUiModel>,
    onPlaceClick: (PopularPlaceUiModel) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(
            title = "Popular Destinations",
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            places.forEach { place ->
                PopularPlaceCard(
                    place = place,
                    onClick = { onPlaceClick(place) },
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Composable
private fun PopularPlaceCard(
    place: PopularPlaceUiModel,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            ) {
                if (place.imageResId != null) {
                    Image(
                        painter = painterResource(place.imageResId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        place.imageColor.copy(alpha = 0.9f),
                                        place.imageColor.copy(alpha = 0.48f),
                                    ),
                                ),
                            ),
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.2f)),
                                startY = 100f
                            )
                        )
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "★",
                            color = CityFlowOrange,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = place.rating,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HomeIconGraphic(
                        icon = HomeIcon.Station,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = place.nearestStation,
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

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "See All",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { }
        )
    }
}

private val sampleQuickActions = listOf(
    QuickActionUiModel("BTS Guide", HomeIcon.Train, CityFlowBlue),
    QuickActionUiModel("Plan Route", HomeIcon.Route, CityFlowGreen),
    QuickActionUiModel("Ticket Guide", HomeIcon.Ticket, CityFlowOrange),
)

private val samplePopularPlaces = listOf(
    PopularPlaceUiModel("ICONSIAM", "Charoen Nakhon BTS", "4.8", CityFlowBlue, imageResId = R.drawable.download, latitude = 13.7266, longitude = 100.5108),
    PopularPlaceUiModel("Siam Paragon", "Siam BTS", "4.7", CityFlowGreen, imageResId = R.drawable.siam_paragon, latitude = 13.7466, longitude = 100.5347),
    PopularPlaceUiModel("Chatuchak Market", "Mo Chit BTS", "4.6", CityFlowOrange, imageResId = R.drawable.images, latitude = 13.7999, longitude = 100.5501),
    PopularPlaceUiModel("Asiatique", "Saphan Taksin BTS", "4.5", Color(0xFF7E57C2), imageResId = R.drawable.asiatique, latitude = 13.7042, longitude = 100.5036),
    PopularPlaceUiModel("Grand Palace", "Sanam Chai MRT", "4.8", Color(0xFF00ACC1), imageResId = R.drawable.grandplace, latitude = 13.7500, longitude = 100.4913),
)

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun HomeScreenPreview() {
    CityFlowBKKTheme(dynamicColor = false) {
        HomeScreen()
    }
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 900,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HomeScreenDarkPreview() {
    CityFlowBKKTheme(dynamicColor = false) {
        HomeScreen()
    }
}
