package com.meteo.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meteo.app.R
import com.meteo.app.domain.DayForecast
import com.meteo.app.domain.WeatherCondition
import com.meteo.app.domain.WeatherData
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherRoute(
    viewModel: WeatherViewModel,
    onRequestLocation: () -> Unit,
    onRefresh: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val dayHourlyState by viewModel.dayHourlyState.collectAsState()
    var currentScreen by remember { mutableStateOf("weather") }
    var selectedDay by remember { mutableStateOf<DayForecast?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (currentScreen == "options") {
        OptionsScreen(onBack = { currentScreen = "weather" })
        return
    }

    val backgrounds = remember {
        WeatherCondition.entries.map { it.bgRes }.distinct()
    }
    var currentBgIndex by remember { 
        mutableIntStateOf(backgrounds.indexOf(R.drawable.bg_clear).takeIf { it != -1 } ?: 0) 
    }

    LaunchedEffect(state) {
        val s = state
        if (s is WeatherUiState.Success) {
            val currentHourLabel = s.data.hourly.firstOrNull()?.label
            val condition = WeatherCondition.entries.find {
                it.description == (currentHourLabel ?: s.data.overview.today.label)
            }
                ?: WeatherCondition.UNKNOWN
            val index = backgrounds.indexOf(condition.bgRes)
            if (index != -1) {
                currentBgIndex = index
            }
        }
    }

    val locationTitle = (state as? WeatherUiState.Success)?.currentLocation?.name
        ?: stringResource(R.string.app_name)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgrounds[currentBgIndex]),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    WeatherTopBar(
                        locationTitle = locationTitle,
                        viewModel = viewModel,
                        onRefresh = onRefresh,
                        onSettingsClick = { currentScreen = "options" },
                        onCurrentLocationRequest = onRequestLocation
                    )
                    if (state is WeatherUiState.Success) {
                        val successState = state as WeatherUiState.Success
                        if (successState.isRefreshing) {
                            BackgroundLoadingBanner()
                        }
                        else if (successState.isOffline) {
                            OfflineWarningBanner()
                        }
                    }
                }
            },
        ) { padding ->
            when (val s = state) {
                is WeatherUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.loading), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                is WeatherUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(s.message, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onRefresh) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
                is WeatherUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        WeatherContent(
                            modifier = Modifier
                                .weight(1f)
                                .padding(padding),
                            data = s.data,
                            onDayClick = { selectedDay = it },
                        )
                    }
                }
            }
        }

        if ((selectedDay != null) && (state is WeatherUiState.Success)) {
            val data = (state as WeatherUiState.Success).data
            val initialPage = remember(selectedDay) {
                data.daily5.indexOfFirst { it.date == selectedDay?.date }.coerceAtLeast(0)
            }
            val pagerState = rememberPagerState(initialPage = initialPage) { data.daily5.size }

            LaunchedEffect(pagerState.currentPage) {
                val day = data.daily5[pagerState.currentPage]
                viewModel.loadHourlyForDay(
                    (state as WeatherUiState.Success).currentLocation,
                    day.date,
                )
            }

            ModalBottomSheet(
                onDismissRequest = { selectedDay = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                ) { page ->
                    val day = data.daily5[page]
                    DayDetailContent(
                        day = day,
                        dayHourlyState = dayHourlyState
                    )
                }
            }
        }
    }
}

@Composable
private fun DayDetailContent(
    day: DayForecast,
    dayHourlyState: DayHourlyState,
) {
    val condition = remember(day.label) {
        WeatherCondition.entries.find { it.description == (day.label ?: "") } ?: WeatherCondition.UNKNOWN
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Image de fond plus visible
        Image(
            painter = painterResource(id = condition.bgRes),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )

        // Overlay sombre pour faire ressortir le texte blanc/clair
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.5f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // En-tête Date
            Column(
                modifier = Modifier.padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = day.weekdayLabel ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${day.dayOfMonth} ${monthName(day.date)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            // Carte Résumé translucide
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        WeatherIcon(day.label ?: "", modifier = Modifier.size(64.dp))
                        Text(
                            text = day.label ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            softWrap = true,
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${day.maxC}°",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                            )
                            Text(
                                text = "/ ${day.minC}°",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
                            )
                        }

                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            DetailBadge(icon = "💨", value = "${day.maxWindSpeed} km/h")
                            if (day.precipPct != null || (day.precipAmount ?: 0f) > 0f) {
                                val pct = day.precipPct?.let { "$it%" } ?: ""
                                val amount = day.precipAmount?.let { 
                                    if (it > 0) " (${String.format(java.util.Locale.getDefault(), "%.1f", it)} mm)" else "" 
                                } ?: ""
                                DetailBadge(icon = "💧", value = "$pct$amount")
                            }
                        }
                    }
                }
            }

            // Section Graphique
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Détails horaires",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Box(modifier = Modifier.padding(vertical = 12.dp)) {
                        when (dayHourlyState) {
                            is DayHourlyState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(168.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        strokeWidth = 3.dp,
                                        color = Color.White
                                    )
                                }
                            }

                            is DayHourlyState.Success -> {
                                if (dayHourlyState.hours.isNotEmpty()) {
                                    HourlyChart(hours = dayHourlyState.hours)
                                } else {
                                    EmptyHourlyState()
                                }
                            }

                            is DayHourlyState.Error -> {
                                Text(
                                    text = dayHourlyState.message,
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .align(Alignment.Center),
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBadge(icon: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(Color.Transparent, RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(icon, fontSize = 12.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun EmptyHourlyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pas de données détaillées",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

private fun monthName(date: LocalDate?): String {
    if (date == null) return ""
    return date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH)
        .replaceFirstChar { it.uppercase() }
}

@Composable
private fun BackgroundLoadingBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = 4.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "Actualisation en cours...",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun OfflineWarningBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(vertical = 4.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "Mode hors connexion - Données locales",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun WeatherContent(
    modifier: Modifier = Modifier,
    data: WeatherData,
    onDayClick: (DayForecast) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard(title = stringResource(R.string.forecast_title)) {
                OverviewPanel(data)
            }
        }
        item {
            SectionCard(title = stringResource(R.string.section_next_hours)) {
                HourlyPanel(data.hourly)
            }
        }
        item {
            SectionCard(title = "Tendance 7 jours") {
                DailyChart(data.daily5)
            }
        }
        item {
            SectionCard(title = stringResource(R.string.section_15_days)) {
                DailyPanel(data.daily5, onDayClick = onDayClick)
            }
        }
    }
}
