package ru.nikfirs.android.traveltracker.feature.home.ui.screens.main

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.nikfirs.android.traveltracker.core.domain.model.DaysCalculation
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.domain.model.VisaEntries
import ru.nikfirs.android.traveltracker.core.domain.model.asString
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.component.DialogTwoRowButton
import ru.nikfirs.android.traveltracker.core.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.core.ui.theme.button
import ru.nikfirs.android.traveltracker.core.ui.theme.tab
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeTab
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.HomeContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.HomeContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.HomeContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.components.DaysCounterCard
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.components.SwipeableTripCard
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.components.SwipeableVisaCard
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.utils.HomeAction
import java.time.LocalDate

@Composable
fun HomeScreen(
    navigateToAddVisa: () -> Unit,
    navigateToAddTrip: () -> Unit,
    navigateToEditVisa: (Long) -> Unit,
    navigateToEditTrip: (Long) -> Unit,
    navigateToVisaDetails: (visaId: Long) -> Unit,
    navigateToTripDetails: (tripId: Long) -> Unit,
    navigateRoute: (Any) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.setAction(Action.UpdateDaysCalculation)
    }
    LaunchedEffectResolver(flow = viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateToAddVisa -> navigateToAddVisa()
            is Effect.NavigateToAddTrip -> navigateToAddTrip()
            is Effect.NavigateToVisaDetails -> navigateToVisaDetails(effect.visaId)
            is Effect.NavigateToTripDetails -> navigateToTripDetails(effect.tripId)
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
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.button,
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

    DialogTwoRowButton(
        message = state.dialogText,
        onRightBtn = {
            viewModel.setAction(Action.HideDialog)
            when (state.action?.action) {
                HomeAction.DELETE_VISA -> {
                    state.action?.visa?.let {
                        viewModel.setAction(Action.DeleteVisa(it))
                    }
                }

                HomeAction.DELETE_TRIP -> {
                    state.action?.trip?.let {
                        viewModel.setAction(Action.DeleteTrip(it))
                    }
                }

                null -> {}
            }
        },
        onDismiss = { viewModel.setAction(Action.HideDialog) }
    )


    ErrorDialog(
        message = state.error,
        onDismiss = { viewModel.setAction(Action.SetError()) },
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
                        isVisaListEmpty = state.visas.isEmpty(),
                        onAddClick = {
                            when (state.selectedTab) {
                                HomeTab.VISAS -> onAction(Action.NavigateToAddVisa)
                                HomeTab.TRIPS -> if (state.visas.isEmpty()) {
                                    onAction(Action.NavigateToAddVisa)
                                } else onAction(Action.NavigateToAddTrip)
                            }
                        }
                    )
                }

                else -> {
                    when (state.selectedTab) {
                        HomeTab.VISAS -> VisasTabContent(state, onAction)
                        HomeTab.TRIPS -> TripsTabContent(state, onAction)
                    }
                }
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
            SwipeableVisaCard(
                visa = visa,
                onAction = onAction,
            )
        }
        item {
            Spacer(Modifier.height(70.dp))
        }
    }
}

@Composable
private fun TripsTabContent(
    state: State,
    onAction: (Action) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.trips) {
        val firstOverLimitTripIndex = findFirstOverLimitTripIndex(state)
        if (firstOverLimitTripIndex != -1) {
            delay(100)
            listState.animateScrollToItem(firstOverLimitTripIndex)
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                SwipeableTripCard(
                    trip = trip,
                    isExempt = trip.hasExemptSegment,
                    countableDuration = trip.countableDays,
                    onAction = onAction,
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
                SwipeableTripCard(
                    trip = trip,
                    isExempt = trip.hasExemptSegment,
                    countableDuration = trip.countableDays,
                    onAction = onAction,
                )
            }
        }

        // Past trips (within past 180 days)
        if (state.pastTrips.isNotEmpty()) {
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
                items = state.pastTrips,
                key = { "trip_${it.id}" }
            ) { trip ->
                SwipeableTripCard(
                    trip = trip,
                    isExempt = trip.hasExemptSegment,
                    countableDuration = trip.countableDays,
                    onAction = onAction,
                )
            }
        }

        item {
            Spacer(Modifier.height(70.dp))
        }
    }
}

@Composable
private fun EmptyState(
    tab: HomeTab,
    isVisaListEmpty: Boolean,
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
            },
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (tab) {
                HomeTab.VISAS -> stringResource(R.string.home_empty_visas)
                HomeTab.TRIPS -> if (isVisaListEmpty) {
                    stringResource(R.string.home_empty_all)
                } else {
                    stringResource(R.string.home_empty_trips)
                }
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        CustomButton(
            text = when (tab) {
                HomeTab.VISAS -> stringResource(R.string.action_add_visa)
                HomeTab.TRIPS -> if (isVisaListEmpty) {
                    stringResource(R.string.action_add_visa)
                } else {
                    stringResource(R.string.action_add_trip)
                }
            },
            iconImage = Icons.Default.Add,
            onClick = onAddClick,
        )
    }
}

private fun findFirstOverLimitTripIndex(state: State): Int {
    var currentIndex = 0

    // search in ngoing trips
    if (state.ongoingTrips.isNotEmpty()) {
        currentIndex++ // header

        for (trip in state.ongoingTrips) {
            if (trip.hasOverLimitDay) {
                return currentIndex
            }
            currentIndex++
        }
    }

    // search in planned trips
    if (state.plannedTrips.isNotEmpty()) {
        currentIndex++ // spacer + header

        for (trip in state.plannedTrips) {
            if (trip.hasOverLimitDay) {
                return currentIndex
            }
            currentIndex++
        }
    }

    // search in past trips
    if (state.pastTrips.isNotEmpty()) {
        currentIndex++ // spacer + header

        for (trip in state.pastTrips) {
            if (trip.hasOverLimitDay) {
                return currentIndex
            }
            currentIndex++
        }
    }

    return -1 // not found
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenEmptyPreview() {
    AppTheme {
        HomeContent(
            state = State(
                isLoading = false,
                selectedTab = HomeTab.TRIPS
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
                                isExempt = false
                            ),
                            TripSegment(
                                country = "Poland",
                                startDate = LocalDate.now(),
                                endDate = LocalDate.now().plusDays(5),
                                isExempt = false
                            )
                        ),
                        purpose = TripPurpose.TOURISM,
                    ),
                    Trip(
                        id = 3,
                        startDate = LocalDate.now().minusDays(5),
                        endDate = LocalDate.now().plusDays(5),
                        segments = listOf(
                            TripSegment(
                                country = "Germany",
                                startDate = LocalDate.now().minusDays(5),
                                endDate = LocalDate.now(),
                                isExempt = false
                            ),
                            TripSegment(
                                country = "Poland",
                                startDate = LocalDate.now(),
                                endDate = LocalDate.now().plusDays(5),
                                isExempt = false
                            )
                        ),
                        purpose = TripPurpose.TOURISM,
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
                                cities = listOf("Paris"),
                                isExempt = true
                            ),
                            TripSegment(
                                country = "Spain",
                                startDate = LocalDate.now().plusDays(33),
                                endDate = LocalDate.now().plusDays(37),
                                cities = listOf("Madrid", "Barcelona"),
                                isExempt = false
                            )
                        ),
                        purpose = TripPurpose.BUSINESS,
                    )
                ),
                daysCalculation = DaysCalculation(
                    totalDaysUsed = 45,
                    remainingDays = 45,
                    periodStart = LocalDate.now().minusDays(179),
                    periodEnd = LocalDate.now(),
                ),
                selectedTab = HomeTab.TRIPS
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
                                cities = listOf("Madrid"),
                                isExempt = true
                            )
                        ),
                        purpose = TripPurpose.TOURISM,
                    )
                ),
                daysCalculation = DaysCalculation(
                    totalDaysUsed = 78,
                    remainingDays = 12,
                    periodStart = LocalDate.now().minusDays(179),
                    periodEnd = LocalDate.now(),
                ),
                selectedTab = HomeTab.VISAS
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
                                isExempt = true,
                            ),
                            TripSegment(
                                country = "France",
                                startDate = LocalDate.now().minusDays(85),
                                endDate = LocalDate.now().minusDays(80),
                                isExempt = false,
                            )
                        ),
                        purpose = TripPurpose.TOURISM,
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
                                isExempt = false
                            )
                        ),
                        purpose = TripPurpose.FAMILY,
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
                                isExempt = false
                            ),
                            TripSegment(
                                country = "Czech Republic",
                                startDate = LocalDate.now().plusDays(15),
                                endDate = LocalDate.now().plusDays(20),
                                isExempt = false
                            )
                        ),
                        purpose = TripPurpose.EDUCATION,
                    )
                ),
                daysCalculation = DaysCalculation(
                    totalDaysUsed = 22,
                    remainingDays = 74,
                    periodStart = LocalDate.now().minusDays(179),
                    periodEnd = LocalDate.now(),
                ),
                selectedTab = HomeTab.TRIPS
            ),
            onAction = {},
        )
    }
}