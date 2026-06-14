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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cityflowbkk.R
import com.example.cityflowbkk.features.place.PlaceDetailBottomSheet
import com.example.cityflowbkk.features.place.PlaceDetailViewModel
import com.example.cityflowbkk.ui.theme.CityFlowBKKTheme
import com.example.cityflowbkk.ui.theme.CityFlowBlue
import com.example.cityflowbkk.ui.theme.CityFlowGreen
import com.example.cityflowbkk.ui.theme.CityFlowOrange
@Immutable
data class HomeUiState(
    val searchQuery: String = "",
    val quickActions: List<QuickActionUiModel> = sampleQuickActions,
    val popularPlaces: List<PopularPlaceUiModel> = samplePopularPlaces,
    val recentSearches: List<RecentSearchUiModel> = sampleRecentSearches,
    val selectedBottomItem: BottomNavItem = BottomNavItem.Home,
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

@Immutable
data class RecentSearchUiModel(
    val origin: String,
    val destination: String,
)

enum class HomeIcon {
    Route,
    Train,
    Subway,
    Ticket,
    School,
    Search,
    Notification,
    Home,
    Map,
    Station,
    Profile,
}

enum class BottomNavItem(
    val label: String,
    val icon: HomeIcon,
) {
    Home("Home", HomeIcon.Home),
    Route("Route", HomeIcon.Route),
    Station("Station", HomeIcon.Station),
    Profile("Profile", HomeIcon.Profile),
}
@Composable
fun HomeScreen(
    uiState: HomeUiState = HomeUiState(),
    onSearchQueryChange: (String) -> Unit = {},
    onPlanRouteClick: () -> Unit = {},
    onQuickActionClick: (QuickActionUiModel) -> Unit = {},
    onRecentSearchClick: (RecentSearchUiModel) -> Unit = {},
    onBottomNavItemClick: (BottomNavItem) -> Unit = {},
    placeDetailViewModel: PlaceDetailViewModel = viewModel(),
) {
    var localSearchQuery by remember(uiState.searchQuery) { mutableStateOf(uiState.searchQuery) }
    var selectedPopularPlace by remember { mutableStateOf<PopularPlaceUiModel?>(null) }
    val placeDetailUiState by placeDetailViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CityFlowTopAppBar()
        },
        bottomBar = {
            CityFlowBottomBar(
                selectedItem = uiState.selectedBottomItem,
                onItemClick = onBottomNavItemClick,
            )
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
            WelcomeSection(
                searchQuery = localSearchQuery,
                onSearchQueryChange = {
                    localSearchQuery = it
                    onSearchQueryChange(it)
                },
            )
            HeroCard(onPlanRouteClick = onPlanRouteClick)
            QuickActionsGrid(
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
            RecentSearchesSection(
                searches = uiState.recentSearches,
                onRecentSearchClick = onRecentSearchClick,
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
private fun CityFlowTopAppBar() {
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
private fun WelcomeSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Hello \uD83D\uDC4B",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Where would you like to go today?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Search station, landmark, destination...")
            },
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
                if (place.name == "Siam Paragon") {
                    Image(
                        painter = painterResource(place.imageResId ?: R.drawable.siam_paragon),
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
private fun RecentSearchesSection(
    searches: List<RecentSearchUiModel>,
    onRecentSearchClick: (RecentSearchUiModel) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Recent Searches")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            searches.forEach { search ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) { onRecentSearchClick(search) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 1.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HomeIconGraphic(
                            icon = HomeIcon.Route,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "${search.origin} -> ${search.destination}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
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

@Composable
private fun CityFlowBottomBar(
    selectedItem: BottomNavItem,
    onItemClick: (BottomNavItem) -> Unit,
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
    ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = item == selectedItem,
                onClick = { onItemClick(item) },
                icon = {
                    HomeIconGraphic(
                        icon = item.icon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(item.label)
                },
            )
        }
    }
}

@Composable
private fun HomeIconGraphic(
    icon: HomeIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val finalModifier = modifier.size(24.dp)
    val semanticModifier = if (contentDescription == null) {
        finalModifier
    } else {
        finalModifier.semantics { this.contentDescription = contentDescription }
    }
    Canvas(modifier = semanticModifier) {
        val stroke = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)
        fun point(x: Float, y: Float) = Offset(size.width * x, size.height * y)

        when (icon) {
            HomeIcon.Search -> {
                drawCircle(tint, radius = size.minDimension * 0.27f, center = point(0.43f, 0.43f), style = stroke)
                drawLine(tint, point(0.64f, 0.64f), point(0.86f, 0.86f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Route -> {
                val path = Path().apply {
                    moveTo(size.width * 0.22f, size.height * 0.78f)
                    quadraticTo(size.width * 0.46f, size.height * 0.3f, size.width * 0.78f, size.height * 0.22f)
                }
                drawPath(path, tint, style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.12f, center = point(0.22f, 0.78f))
                drawCircle(tint, radius = size.minDimension * 0.12f, center = point(0.78f, 0.22f))
            }

            HomeIcon.Train, HomeIcon.Subway -> {
                drawRoundRect(
                    color = tint,
                    topLeft = point(0.23f, 0.14f),
                    size = Size(size.width * 0.54f, size.height * 0.58f),
                    cornerRadius = CornerRadius(size.minDimension * 0.12f),
                    style = stroke,
                )
                drawLine(tint, point(0.34f, 0.32f), point(0.66f, 0.32f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.34f, 0.52f), point(0.66f, 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.38f, 0.86f), point(0.48f, 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.62f, 0.86f), point(0.52f, 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Ticket -> {
                val path = Path().apply {
                    moveTo(size.width * 0.14f, size.height * 0.32f)
                    lineTo(size.width * 0.84f, size.height * 0.2f)
                    lineTo(size.width * 0.9f, size.height * 0.46f)
                    lineTo(size.width * 0.8f, size.height * 0.54f)
                    lineTo(size.width * 0.86f, size.height * 0.78f)
                    lineTo(size.width * 0.16f, size.height * 0.66f)
                    lineTo(size.width * 0.24f, size.height * 0.52f)
                    close()
                }
                drawPath(path, tint, style = stroke)
                drawLine(tint, point(0.48f, 0.28f), point(0.56f, 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.School -> {
                val cap = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.16f)
                    lineTo(size.width * 0.88f, size.height * 0.36f)
                    lineTo(size.width * 0.5f, size.height * 0.56f)
                    lineTo(size.width * 0.12f, size.height * 0.36f)
                    close()
                }
                drawPath(cap, tint)
                drawLine(tint, point(0.26f, 0.5f), point(0.26f, 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.26f, 0.7f), point(0.74f, 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.74f, 0.7f), point(0.74f, 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Notification -> {
                val path = Path().apply {
                    moveTo(size.width * 0.28f, size.height * 0.68f)
                    quadraticTo(size.width * 0.38f, size.height * 0.52f, size.width * 0.34f, size.height * 0.34f)
                    quadraticTo(size.width * 0.5f, size.height * 0.12f, size.width * 0.66f, size.height * 0.34f)
                    quadraticTo(size.width * 0.62f, size.height * 0.52f, size.width * 0.72f, size.height * 0.68f)
                    close()
                }
                drawPath(path, tint, style = stroke)
                drawLine(tint, point(0.42f, 0.82f), point(0.58f, 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Home -> {
                drawLine(tint, point(0.14f, 0.46f), point(0.5f, 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.5f, 0.18f), point(0.86f, 0.46f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.25f, 0.42f), point(0.25f, 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.75f, 0.42f), point(0.75f, 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.25f, 0.84f), point(0.75f, 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Map -> {
                drawLine(tint, point(0.16f, 0.24f), point(0.36f, 0.16f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.36f, 0.16f), point(0.64f, 0.26f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.64f, 0.26f), point(0.84f, 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.16f, 0.24f), point(0.16f, 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.36f, 0.16f), point(0.36f, 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.64f, 0.26f), point(0.64f, 0.86f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.84f, 0.18f), point(0.84f, 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.16f, 0.84f), point(0.36f, 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.36f, 0.76f), point(0.64f, 0.86f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, point(0.64f, 0.86f), point(0.84f, 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Station -> {
                drawCircle(tint, radius = size.minDimension * 0.27f, center = point(0.5f, 0.38f), style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.1f, center = point(0.5f, 0.38f))
                drawLine(tint, point(0.5f, 0.65f), point(0.5f, 0.9f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            HomeIcon.Profile -> {
                drawCircle(tint, radius = size.minDimension * 0.17f, center = point(0.5f, 0.32f), style = stroke)
                val shoulders = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.86f)
                    quadraticTo(size.width * 0.5f, size.height * 0.56f, size.width * 0.82f, size.height * 0.86f)
                }
                drawPath(shoulders, tint, style = stroke)
            }
        }
    }
}

private val sampleQuickActions = listOf(
    QuickActionUiModel("Plan Route", HomeIcon.Route, CityFlowBlue),
    QuickActionUiModel("BTS Guide", HomeIcon.Train, CityFlowGreen),
    QuickActionUiModel("MRT Guide", HomeIcon.Subway, CityFlowOrange),
    QuickActionUiModel("Ticket Guide", HomeIcon.Ticket, Color(0xFF7E57C2)),
    QuickActionUiModel("Tutorials", HomeIcon.School, Color(0xFF00ACC1)),
    QuickActionUiModel("Smart Search", HomeIcon.Search, Color(0xFFEF6C00)),
)

private val samplePopularPlaces = listOf(
    PopularPlaceUiModel("ICONSIAM", "Charoen Nakhon BTS", "4.8", CityFlowBlue, latitude = 13.7266, longitude = 100.5108),
    PopularPlaceUiModel("Siam Paragon", "Siam BTS", "4.7", CityFlowGreen, R.drawable.siam_paragon, latitude = 13.7466, longitude = 100.5347),
    PopularPlaceUiModel("Chatuchak Market", "Mo Chit BTS", "4.6", CityFlowOrange, latitude = 13.7999, longitude = 100.5501),
    PopularPlaceUiModel("Asiatique", "Saphan Taksin BTS", "4.5", Color(0xFF7E57C2), latitude = 13.7042, longitude = 100.5036),
    PopularPlaceUiModel("Grand Palace", "Sanam Chai MRT", "4.8", Color(0xFF00ACC1), latitude = 13.7500, longitude = 100.4913),
)

private val sampleRecentSearches = listOf(
    RecentSearchUiModel("Siam", "Mo Chit"),
    RecentSearchUiModel("Asok", "Chatuchak Park"),
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
