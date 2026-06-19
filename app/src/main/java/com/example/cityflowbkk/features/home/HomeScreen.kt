package com.example.cityflowbkk.features.home

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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    onNavigateToMap: () -> Unit = {},
    onNavigateToBtsMap: () -> Unit = {},
    onNavigateToStationMapping: () -> Unit = {},
    onQuickActionClick: (QuickActionUiModel) -> Unit = {},
    onPopularPlaceClick: (PopularPlaceUiModel) -> Unit = {},
    placeDetailViewModel: PlaceDetailViewModel = viewModel(),
) {
    var selectedPopularPlace by remember { mutableStateOf<PopularPlaceUiModel?>(null) }
    val placeDetailUiState by placeDetailViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CityFlowTopAppBar(onMappingToolClick = onNavigateToStationMapping)
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            HeroCard(onPlanRouteClick = onPlanRouteClick)
            BtsMapBannerButton(onClick = onNavigateToBtsMap)
            QuickActionsGrid(
                quickActions = uiState.quickActions,
                onQuickActionClick = { action ->
                    when (action.title) {
                        "Plan Route", "Smart Search" -> onNavigateToMap()
                        else -> onQuickActionClick(action)
                    }
                },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityFlowTopAppBar(onMappingToolClick: () -> Unit = {}) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CityFlowLogo()
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        actions = {
            TextButton(onClick = onMappingToolClick) {
                Text("Mapping Tool", style = MaterialTheme.typography.labelMedium)
            }
            Box {
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
                        .padding(top = 10.dp, end = 10.dp),
                    containerColor = CityFlowOrange,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

@Composable
private fun CityFlowLogo() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(CityFlowBlue, CityFlowGreen),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "CF",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun HeroCard(onPlanRouteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                    .background(Color.Black.copy(alpha = 0.35f)),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = "Explore Bangkok\nSmart & Easy",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Button(
                    onClick = onPlanRouteClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                ) {
                    HomeIconGraphic(
                        icon = HomeIcon.Route,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Plan Route", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun BtsMapBannerButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(CityFlowGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                HomeIconGraphic(
                    icon = HomeIcon.Train,
                    contentDescription = null,
                    tint = CityFlowGreen,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BTS Skytrain Map",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "Interactive network map",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                )
            }
            HomeIconGraphic(
                icon = HomeIcon.Map,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun QuickActionsGrid(
    quickActions: List<QuickActionUiModel>,
    onQuickActionClick: (QuickActionUiModel) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Quick Actions")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
    Card(
        modifier = modifier
            .aspectRatio(0.95f)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(action.accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                HomeIconGraphic(
                    icon = action.icon,
                    contentDescription = null,
                    tint = action.accentColor,
                    modifier = Modifier.size(23.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PopularPlacesSection(
    places: List<PopularPlaceUiModel>,
    onPlaceClick: (PopularPlaceUiModel) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Popular Places")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            places.forEach { place ->
                PopularPlaceCard(
                    place = place,
                    onClick = { onPlaceClick(place) },
                )
            }
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
            .width(178.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp),
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
                HomeIconGraphic(
                    icon = HomeIcon.Station,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp),
                )
            }
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = place.nearestStation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "* ${place.rating}",
                    style = MaterialTheme.typography.labelMedium,
                    color = CityFlowOrange,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

private val sampleQuickActions = listOf(
    QuickActionUiModel("Plan Route", HomeIcon.Route, CityFlowBlue),
    QuickActionUiModel("BTS Guide", HomeIcon.Train, CityFlowGreen),
    QuickActionUiModel("Ticket Guide", HomeIcon.Ticket, Color(0xFF7E57C2)),
    QuickActionUiModel("Tutorials", HomeIcon.School, Color(0xFF00ACC1)),
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
