package ru.nikfirs.android.traveltracker.feature.home.ui.main

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.nikfirs.android.traveltracker.core.domain.model.*
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.core.ui.theme.tab
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeItem
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeTab
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.main.components.DaysCounterCard
import ru.nikfirs.android.traveltracker.feature.home.ui.main.components.TripCard
import ru.nikfirs.android.traveltracker.feature.home.ui.main.components.VisaCard
import java.time.LocalDate

@Composable
fun HomeScreen(
    navigateToAddVisa: () -> Unit,
    navigateToAddTrip: () -> Unit,
    navigateToEditVisa: (Long) -> Unit,
    navigateToEditTrip: (Long) -> Unit,
    navigateToVisaDetails: (visaId: Long) -> Unit,
    navigateRoute: (Any) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffectResolver(flow = viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateToAddVisa -> navigateToAddVisa()
            is Effect.NavigateToAddTrip -> navigateToAddTrip()
            is Effect.NavigateToVisaDetails -> navigateToVisaDetails(effect.visaId)
            is Effect.NavigateToEditVisa -> navigateToEditVisa(effect.visaId)
            is Effect.NavigateToEditTrip -> navigateToEditTrip(effect.tripId)
            is Effect.ShowMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = effect.message.asString(context)
                    )
                }
            }
        }
    }

    Screen(
        bottomNavRouteRoute = BottomNavBarRoute.Home,
        navigateRoute = navigateRoute,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.filteredItems.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        when (state.selectedTab) {
                            HomeTab.VISAS -> viewModel.setAction(Action.NavigateToAddVisa)
                            HomeTab.TRIPS -> viewModel.setAction(Action.NavigateToAddTrip)
                            HomeTab.ALL -> viewModel.setAction(Action.NavigateToAddTrip)
                        }
                    },
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (state.selectedTab) {
                            HomeTab.VISAS -> stringResource(R.string.action_add_visa)
                            HomeTab.TRIPS -> stringResource(R.string.action_add_trip)
                            HomeTab.ALL -> stringResource(R.string.action_add_trip)
                        }
                    )
                }
            }
        }
    ) {
        HomeContent(
            state = state,
            onAction = viewModel::setAction,
        )
    }

    ErrorDialog(
        message = state.error,
        onDismiss = { viewModel.setAction(Action.DismissError) },
        onRetry = { viewModel.setAction(Action.RetryLoadData) }
    )
}

@Composable
private fun HomeContent(
    state: State,
    onAction: (Action) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Days counter section
        state.daysCalculation?.let { calculation ->
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                DaysCounterCard(
                    daysCalculation = calculation,
                    currentVisa = state.currentSchengenVisa,
                    exemptCountries = state.exemptCountries
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = state.selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            HomeTab.entries.forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { onAction(Action.SelectTab(tab)) },
                    text = {
                        Text(
                            text = when (tab) {
                                HomeTab.ALL -> stringResource(R.string.home_tab_all)
                                HomeTab.VISAS -> stringResource(R.string.home_tab_visas)
                                HomeTab.TRIPS -> stringResource(R.string.home_tab_trips)
                            }
                        )
                    },
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.tab)
                        .background(Color.Transparent, MaterialTheme.shapes.tab)
                )
            }
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.filteredItems.isEmpty() -> {
                    EmptyState(
                        tab = state.selectedTab,
                        onAddClick = {
                            when (state.selectedTab) {
                                HomeTab.VISAS -> onAction(Action.NavigateToAddVisa)
                                HomeTab.TRIPS -> onAction(Action.NavigateToAddTrip)
                                HomeTab.ALL -> onAction(Action.NavigateToAddTrip)
                            }
                        }
                    )
                }

                else -> {
                    when (state.selectedTab) {
                        HomeTab.ALL -> AllTabContent(state, onAction)
                        HomeTab.VISAS -> VisasTabContent(state, onAction)
                        HomeTab.TRIPS -> TripsTabContent(state, onAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun AllTabContent(
    state: State,
    onAction: (Action) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = state.filteredItems,
            key = { item ->
                when (item) {
                    is HomeItem.VisaItem -> "visa_${item.visa.id}"
                    is HomeItem.TripItem -> "trip_${item.trip.id}"
                }
            }
        ) { item ->
            when (item) {
                is HomeItem.VisaItem -> VisaCard(
                    visa = item.visa,
                    onClick = { onAction(Action.NavigateToVisaDetails(item.visa.id)) }
                )

                is HomeItem.TripItem -> TripCard(
                    trip = item.trip,
                    isExempt = item.isExempt,
                    onClick = { onAction(Action.NavigateToEditTrip(item.trip)) }
                )
            }
        }
    }
}

@Composable
private fun VisasTabContent(
    state: State,
    onAction: (Action) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = state.visas,
            key = { "visa_${it.id}" }
        ) { visa ->
            VisaCard(
                visa = visa,
                onClick = { onAction(Action.NavigateToVisaDetails(visa.id)) }
            )
        }
    }
}

@Composable
private fun TripsTabContent(
    state: State,
    onAction: (Action) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ongoing trips
        if (state.ongoingTrips.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.trips_section_ongoing),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(
                items = state.ongoingTrips,
                key = { "trip_${it.id}" }
            ) { trip ->
                TripCard(
                    trip = trip,
                    isExempt = state.exemptCountries.any { country ->
                        trip.segments.any { it.country == country }
                    },
                    onClick = { onAction(Action.NavigateToEditTrip(trip)) }
                )
            }
        }

        // Planned trips
        if (state.plannedTrips.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.trips_section_planned),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(
                items = state.plannedTrips,
                key = { "trip_${it.id}" }
            ) { trip ->
                TripCard(
                    trip = trip,
                    isExempt = state.exemptCountries.any { country ->
                        trip.segments.any { it.country == country }
                    },
                    onClick = { onAction(Action.NavigateToEditTrip(trip)) }
                )
            }
        }

        // Past trips (show only last 5)
        val recentPastTrips = state.pastTrips.take(5)
        if (recentPastTrips.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.trips_section_past),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(
                items = recentPastTrips,
                key = { "trip_${it.id}" }
            ) { trip ->
                TripCard(
                    trip = trip,
                    isExempt = state.exemptCountries.any { country ->
                        trip.segments.any { it.country == country }
                    },
                    onClick = { onAction(Action.NavigateToEditTrip(trip)) }
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    tab: HomeTab,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = when (tab) {
                HomeTab.VISAS -> painterResource(R.drawable.ic_badge)
                HomeTab.TRIPS -> painterResource(R.drawable.ic_luggage)
                HomeTab.ALL -> painterResource(R.drawable.ic_travel_explore)
            },
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (tab) {
                HomeTab.VISAS -> stringResource(R.string.home_empty_visas)
                HomeTab.TRIPS -> stringResource(R.string.home_empty_trips)
                HomeTab.ALL -> stringResource(R.string.home_empty_all)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        CustomButton(
            text = when (tab) {
                HomeTab.VISAS -> stringResource(R.string.action_add_visa)
                HomeTab.TRIPS -> stringResource(R.string.action_add_trip)
                HomeTab.ALL -> stringResource(R.string.action_add_trip)
            },
            iconImage = Icons.Default.Add,
            onClick = onAddClick,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenEmptyPreview() {
    AppTheme {
        HomeContent(
            state = State(
                isLoading = false,
                selectedTab = HomeTab.ALL
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, locale = "EN")
@Composable
private fun HomeScreenWithDataPreview() {
    AppTheme {
        HomeContent(
            state = State(
                isLoading = false,
                visas = listOf(
                    Visa(
                        id = 1,
                        visaNumber = "C123456789",
                        visaType = VisaCategory.TYPE_C,
                        startDate = LocalDate.now().minusMonths(6),
                        expiryDate = LocalDate.now().plusMonths(6),
                        entries = VisaEntries.MULTI,
                        durationOfStay = 1,
                        isActive = false,
                    ),
                    Visa(
                        id = 2,
                        visaNumber = "D987654321",
                        visaType = VisaCategory.TYPE_D,
                        country = "Germany",
                        startDate = LocalDate.now().minusMonths(3),
                        expiryDate = LocalDate.now().plusMonths(9),
                        entries = VisaEntries.MULTI,
                        durationOfStay = 1,
                    )
                ),
                trips = listOf(
                    Trip(
                        id = 1,
                        startDate = LocalDate.now().minusDays(5),
                        endDate = LocalDate.now().plusDays(5),
                        segments = listOf(
                            TripSegment(
                                country = "Germany",
                                startDate = LocalDate.now().minusDays(5),
                                endDate = LocalDate.now(),
                                type = SegmentType.STAY
                            ),
                            TripSegment(
                                country = "Poland",
                                startDate = LocalDate.now(),
                                endDate = LocalDate.now().plusDays(5),
                                type = SegmentType.STAY
                            )
                        ),
                        purpose = TripPurpose.TOURISM,
                        isPlanned = false
                    ),
                    Trip(
                        id = 2,
                        startDate = LocalDate.now().plusDays(30),
                        endDate = LocalDate.now().plusDays(37),
                        segments = listOf(
                            TripSegment(
                                country = "France",
                                startDate = LocalDate.now().plusDays(30),
                                endDate = LocalDate.now().plusDays(33),
                                type = SegmentType.STAY,
                                cities = listOf("Paris")
                            ),
                            TripSegment(
                                country = "Spain",
                                startDate = LocalDate.now().plusDays(33),
                                endDate = LocalDate.now().plusDays(37),
                                type = SegmentType.STAY,
                                cities = listOf("Madrid", "Barcelona")
                            )
                        ),
                        purpose = TripPurpose.BUSINESS,
                        isPlanned = true
                    )
                ),
                daysCalculation = DaysCalculation(
                    totalDaysUsed = 45,
                    remainingDays = 45,
                    periodStart = LocalDate.now().minusDays(179),
                    periodEnd = LocalDate.now(),
                    isNearLimit = false,
                    isOverLimit = false,
                    exemptCountries = setOf("Germany")
                ),
                exemptCountries = setOf("Germany"),
                selectedTab = HomeTab.ALL
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenNearLimitPreview() {
    AppTheme {
        HomeContent(
            state = State(
                isLoading = false,
                visas = listOf(
                    Visa(
                        id = 1,
                        visaNumber = "C123456789",
                        visaType = VisaCategory.TYPE_C,
                        startDate = LocalDate.now().minusMonths(6),
                        expiryDate = LocalDate.now().plusDays(25),
                        entries = VisaEntries.MULTI,
                        durationOfStay = 1,
                    )
                ),
                trips = listOf(
                    Trip(
                        id = 1,
                        startDate = LocalDate.now().minusDays(10),
                        endDate = LocalDate.now().minusDays(3),
                        segments = listOf(
                            TripSegment(
                                country = "Spain",
                                startDate = LocalDate.now().minusDays(10),
                                endDate = LocalDate.now().minusDays(3),
                                type = SegmentType.STAY,
                                cities = listOf("Madrid")
                            )
                        ),
                        purpose = TripPurpose.TOURISM,
                        isPlanned = false
                    )
                ),
                daysCalculation = DaysCalculation(
                    totalDaysUsed = 78,
                    remainingDays = 12,
                    periodStart = LocalDate.now().minusDays(179),
                    periodEnd = LocalDate.now(),
                    isNearLimit = true,
                    isOverLimit = false
                ),
                selectedTab = HomeTab.ALL
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenTripsTabPreview() {
    AppTheme {
        HomeContent(
            state = State(
                isLoading = false,
                visas = listOf(
                    Visa(
                        id = 1,
                        visaNumber = "RP123456",
                        visaType = VisaCategory.RESIDENCE_PERMIT,
                        country = "Poland",
                        startDate = LocalDate.now().minusYears(1),
                        expiryDate = LocalDate.now().plusYears(1),
                        entries = VisaEntries.MULTI,
                        durationOfStay = 1,
                    )
                ),
                trips = listOf(
                    Trip(
                        id = 1,
                        startDate = LocalDate.now().minusDays(90),
                        endDate = LocalDate.now().minusDays(80),
                        segments = listOf(
                            TripSegment(
                                country = "Italy",
                                startDate = LocalDate.now().minusDays(90),
                                endDate = LocalDate.now().minusDays(85),
                                type = SegmentType.STAY
                            ),
                            TripSegment(
                                country = "France",
                                startDate = LocalDate.now().minusDays(85),
                                endDate = LocalDate.now().minusDays(80),
                                type = SegmentType.STAY
                            )
                        ),
                        purpose = TripPurpose.TOURISM,
                        isPlanned = false
                    ),
                    Trip(
                        id = 2,
                        startDate = LocalDate.now().minusDays(60),
                        endDate = LocalDate.now().minusDays(45),
                        segments = listOf(
                            TripSegment(
                                country = "Poland",
                                startDate = LocalDate.now().minusDays(60),
                                endDate = LocalDate.now().minusDays(45),
                                type = SegmentType.STAY
                            )
                        ),
                        purpose = TripPurpose.FAMILY,
                        isPlanned = false
                    ),
                    Trip(
                        id = 3,
                        startDate = LocalDate.now().plusDays(10),
                        endDate = LocalDate.now().plusDays(20),
                        segments = listOf(
                            TripSegment(
                                country = "Germany",
                                startDate = LocalDate.now().plusDays(10),
                                endDate = LocalDate.now().plusDays(15),
                                type = SegmentType.STAY
                            ),
                            TripSegment(
                                country = "Czech Republic",
                                startDate = LocalDate.now().plusDays(15),
                                endDate = LocalDate.now().plusDays(20),
                                type = SegmentType.STAY
                            )
                        ),
                        purpose = TripPurpose.EDUCATION,
                        isPlanned = true
                    )
                ),
                daysCalculation = DaysCalculation(
                    totalDaysUsed = 22,
                    remainingDays = 74,
                    periodStart = LocalDate.now().minusDays(179),
                    periodEnd = LocalDate.now(),
                    isNearLimit = false,
                    isOverLimit = false,
                    exemptCountries = setOf("Poland"),
                    daysPerCountry = mapOf(
                        "Italy" to 6,
                        "France" to 6,
                        "Poland" to 16,
                        "Germany" to 0,
                        "Czech Republic" to 0
                    )
                ),
                exemptCountries = setOf("Poland"),
                selectedTab = HomeTab.ALL
            ),
            onAction = {},
        )
    }
}