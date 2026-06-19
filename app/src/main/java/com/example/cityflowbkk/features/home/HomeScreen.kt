package com.example.cityflowbkk.features.home

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.TextButton
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

import java.util.Calendar

@Immutable
data class HomeUiState(
    val quickActions: List<QuickActionUiModel> = sampleQuickActions,
    val places: List<BangkokPlace> = BangkokData.places,
    val popularPlaces: List<PopularPlaceUiModel> = samplePopularPlaces,
    val preferredCategory: Category = Category.FOODIE,
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
    onNavigateToMap: () -> Unit = {},
    onNavigateToBtsMap: () -> Unit = {},
    onNavigateToStationMapping: () -> Unit = {},
    onQuickActionClick: (QuickActionUiModel) -> Unit = {},
    onTourClick: () -> Unit = {},
    placeDetailViewModel: PlaceDetailViewModel = viewModel(),
) {
    val recommendations = remember(uiState.places, uiState.preferredCategory) {
        RecommendationEngine.recommend(
            places = uiState.places,
            preferredCategory = uiState.preferredCategory,
        )
    }
    var selectedPlace by remember(recommendations) { mutableStateOf(recommendations.firstOrNull()) }
    var detailPlace by remember { mutableStateOf<BangkokPlace?>(null) }
    val placeDetailUiState by placeDetailViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CityFlowTopAppBar(onMappingToolClick = onNavigateToStationMapping)
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

                GreetingSection(modifier = Modifier.padding(horizontal = 24.dp))

                TravelNoticeCard(
                    notice = selectedPlace?.travelNotice
                        ?: "Choose a destination to see practical BTS/MRT and neighborhood travel tips.",
                    modifier = Modifier.padding(horizontal = 24.dp),
                )

                HeroCard(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    onPlanRouteClick = onPlanRouteClick
                )

                QuickActionsGrid(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    quickActions = uiState.quickActions,
                    onQuickActionClick = { action ->
                        if (action.title == "Tour") {
                            onTourClick()
                        } else {
                            onQuickActionClick(action)
                        }
                    },
                )

                RecommendedDestinationsSection(
                    places = recommendations,
                    selectedPlace = selectedPlace,
                    onPlaceClick = { place ->
                        selectedPlace = place
                        detailPlace = place
                        placeDetailViewModel.loadPlace(place.toPopularPlaceUiModel())
                    },
                )
            }
        }
    }

    if (detailPlace != null) {
        PlaceDetailBottomSheet(
            uiState = placeDetailUiState,
            onDismiss = {
                detailPlace = null
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
private fun CityFlowTopAppBar(onMappingToolClick: () -> Unit = {}) {
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
            TextButton(onClick = onMappingToolClick) {
                Text("Mapping Tool", style = MaterialTheme.typography.labelMedium)
            }
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
private fun GreetingSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "${currentGreeting()} 👋",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "Explore Bangkok smarter today.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TravelNoticeCard(
    notice: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                HomeIconGraphic(
                    icon = HomeIcon.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Travel Notice",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Bold,
                )
                Crossfade(
                    targetState = notice,
                    label = "TravelNoticeText",
                ) { currentNotice ->
                    Text(
                        text = currentNotice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.86f),
                        lineHeight = 20.sp,
                    )
                }
            }
        }
    }
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
private fun RecommendedDestinationsSection(
    places: List<BangkokPlace>,
    selectedPlace: BangkokPlace?,
    onPlaceClick: (BangkokPlace) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(
            title = "Recommended Destinations",
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            items(
                items = places,
                key = { place -> place.name },
            ) { place ->
                RecommendedDestinationCard(
                    place = place,
                    isSelected = place == selectedPlace,
                    onClick = { onPlaceClick(place) },
                )
            }
        }
    }
}

@Composable
private fun RecommendedDestinationCard(
    place: BangkokPlace,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 0.dp,
            color = borderColor
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            ) {
                Image(
                    painter = painterResource(place.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)),
                                startY = 100f
                            )
                        )
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HomeIconGraphic(
                            icon = place.primaryCategory.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = place.primaryCategory.displayName,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeIconGraphic(
                        icon = HomeIcon.Station,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = place.nearestStation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// Extension function to convert BangkokPlace to PopularPlaceUiModel
private fun BangkokPlace.toPopularPlaceUiModel(): PopularPlaceUiModel {
    return PopularPlaceUiModel(
        name = this.name,
        nearestStation = this.nearestStation,
        rating = "4.5", // Default rating since it's not in BangkokPlace
        imageColor = CityFlowBlue, // Default color
        imageResId = this.imageRes,
        placeId = null,
        latitude = null,
        longitude = null,
    )
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
    )
}

// Helper functions
private fun currentGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"
        else -> "Good Night"
    }
}

// Sample data
private val sampleQuickActions = listOf(
    QuickActionUiModel("Tour", HomeIcon.Map, CityFlowBlue),
    QuickActionUiModel("Plan Route", HomeIcon.Route, CityFlowGreen),
    QuickActionUiModel("Smart Search", HomeIcon.Search, CityFlowOrange),
)

private val samplePopularPlaces = listOf(
    PopularPlaceUiModel(
        name = "Chatuchak Market",
        nearestStation = "Mo Chit",
        rating = "4.5",
        imageColor = CityFlowGreen,
    ),
    PopularPlaceUiModel(
        name = "Grand Palace",
        nearestStation = "Saphan Taksin",
        rating = "4.7",
        imageColor = CityFlowOrange,
    ),
)

// Category extension properties
val Category.icon: HomeIcon
    get() = when (this) {
        Category.FOODIE -> HomeIcon.Search // Using Search as a general discovery/food icon
        Category.CULTURE -> HomeIcon.School // Using School as a culture/education icon  
        Category.SHOPPING -> HomeIcon.Ticket // Using Ticket as a shopping/activity icon
        Category.NIGHTLIFE -> HomeIcon.Notification // Using Notification as a nightlife/alert icon
        Category.CAFE -> HomeIcon.Home // Using Home as a cozy cafe icon
        Category.FAMILY -> HomeIcon.Profile // Using Profile as a family/people icon
    }

val Category.displayName: String
    get() = when (this) {
        Category.FOODIE -> "Foodie"
        Category.CULTURE -> "Culture"
        Category.SHOPPING -> "Shopping"
        Category.NIGHTLIFE -> "Nightlife"
        Category.CAFE -> "Cafe"
        Category.FAMILY -> "Family"
    }

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    CityFlowBKKTheme {
        HomeScreen()
    }
}