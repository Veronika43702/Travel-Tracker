package ru.nikfirs.android.traveltracker.feature.home.ui.main

import android.content.res.Configuration
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.nikfirs.android.traveltracker.core.domain.model.DaysCalculation
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaType
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.component.DaysCounter
import ru.nikfirs.android.traveltracker.core.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.core.ui.theme.button
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeItem
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeTab
import ru.nikfirs.android.traveltracker.feature.home.ui.main.components.TripCard
import ru.nikfirs.android.traveltracker.feature.home.ui.main.components.VisaCard
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeContract.Effect
import java.time.LocalDate

@Composable
fun HomeScreen(
    navigateToAddVisa: () -> Unit,
    navigateToAddTrip: () -> Unit,
    navigateToEditVisa: (Long) -> Unit,
    navigateToEditTrip: (Long) -> Unit,
    navigateRoute: (Any) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffectResolver(flow = viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateToAddVisa -> navigateToAddVisa()
            is Effect.NavigateToAddTrip -> navigateToAddTrip()
            is Effect.NavigateToEditVisa -> navigateToEditVisa(effect.visaId)
            is Effect.NavigateToEditTrip -> navigateToEditTrip(effect.tripId)
            is Effect.ShowError -> {} // Обрабатывается через state
        }
    }

    Screen(
        bottomNavRouteRoute = BottomNavBarRoute.Home,
        navigateRoute = navigateRoute,
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
        state.daysCalculation?.let { calculation ->
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)

            ) {
                DaysCounter(
                    daysUsed = calculation.totalDaysUsed,
                    maxDays = DaysCalculation.MAX_STAY_DAYS,
                    showWarning = calculation.isNearLimit,
                    isOverLimit = calculation.isOverLimit
                )
            }
        }

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
                    }
                )
            }
        }

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
                                    onClick = { onAction(Action.NavigateToEditVisa(item.visa)) }
                                )

                                is HomeItem.TripItem -> TripCard(
                                    trip = item.trip,
                                    onClick = { onAction(Action.NavigateToEditTrip(item.trip)) }
                                )
                            }
                        }
                    }
                }
            }

            if (state.filteredItems.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        when (state.selectedTab) {
                            HomeTab.VISAS -> onAction(Action.NavigateToAddVisa)
                            HomeTab.TRIPS -> onAction(Action.NavigateToAddTrip)
                            HomeTab.ALL -> onAction(Action.NavigateToAddTrip)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
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
                HomeTab.VISAS -> stringResource(R.string.home_add_first_visa)
                HomeTab.TRIPS -> stringResource(R.string.home_add_first_trip)
                HomeTab.ALL -> stringResource(R.string.home_empty_state)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddClick,
            shape = MaterialTheme.shapes.button
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (tab) {
                    HomeTab.VISAS -> stringResource(R.string.action_add_visa)
                    HomeTab.TRIPS -> stringResource(R.string.action_add_trip)
                    HomeTab.ALL -> stringResource(R.string.action_add_trip)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    AppTheme {
        HomeContent(
            state = State(
                isLoading = false,
                selectedTab = HomeTab.ALL
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, locale = "EN", uiMode = Configuration.UI_MODE_NIGHT_YES)
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
                        visaType = "C",
                        issueDate = LocalDate.now().minusMonths(6),
                        expiryDate = LocalDate.now().plusMonths(6),
                        entries = VisaType.MULTI
                    ),
                    Visa(
                        id = 2,
                        visaNumber = "C987654321",
                        visaType = "C",
                        issueDate = LocalDate.now().minusYears(1),
                        expiryDate = LocalDate.now().minusDays(10),
                        entries = VisaType.SINGLE
                    )
                ),
                trips = listOf(
                    Trip(
                        id = 5,
                        startDate = LocalDate.now().plusDays(60),
                        endDate = LocalDate.now().plusDays(90),
                        country = "Slovenia",
                        city = "Ljubljana",
                        purpose = TripPurpose.EDUCATION,
                        isPlanned = true
                    ),
                    Trip(
                        id = 2,
                        startDate = LocalDate.now().plusDays(30),
                        endDate = LocalDate.now().plusDays(37),
                        country = "France",
                        city = "Paris",
                        purpose = TripPurpose.BUSINESS,
                        isPlanned = true
                    ),
                    Trip(
                        id = 1,
                        startDate = LocalDate.now().minusDays(5),
                        endDate = LocalDate.now().plusDays(5),
                        country = "Germany",
                        city = "Berlin",
                        purpose = TripPurpose.TOURISM,
                        isPlanned = false
                    ),
                    Trip(
                        id = 3,
                        startDate = LocalDate.now().minusDays(15),
                        endDate = LocalDate.now().minusDays(10),
                        country = "France",
                        city = "Paris",
                        purpose = TripPurpose.FAMILY,
                        isPlanned = false
                    ),
                    Trip(
                        id = 4,
                        startDate = LocalDate.now().minusDays(37),
                        endDate = LocalDate.now().minusDays(30),
                        country = "France",
                        city = "Paris",
                        purpose = TripPurpose.MEDICAL,
                        isPlanned = false
                    ),
                ),
                daysCalculation = DaysCalculation(
                    totalDaysUsed = 45,
                    remainingDays = 45,
                    periodStart = LocalDate.now().minusDays(180),
                    periodEnd = LocalDate.now(),
                    isNearLimit = false,
                    isOverLimit = false
                ),
                selectedTab = HomeTab.TRIPS
            ),
            onAction = {}
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
                        visaType = "C",
                        issueDate = LocalDate.now().minusMonths(6),
                        expiryDate = LocalDate.now().plusMonths(2),
                        entries = VisaType.MULTI
                    )
                ),
                trips = listOf(
                    Trip(
                        id = 1,
                        startDate = LocalDate.now().minusDays(10),
                        endDate = LocalDate.now().minusDays(3),
                        country = "Spain",
                        city = "Madrid",
                        purpose = TripPurpose.TOURISM,
                        isPlanned = false
                    )
                ),
                daysCalculation = DaysCalculation(
                    totalDaysUsed = 78,
                    remainingDays = 12,
                    periodStart = LocalDate.now().minusDays(180),
                    periodEnd = LocalDate.now(),
                    isNearLimit = true,
                    isOverLimit = false
                ),
                selectedTab = HomeTab.ALL
            ),
            onAction = {}
        )
    }
}