package ru.nikfirs.android.traveltracker.feature.calendar.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomCalendar
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomSwitch
import ru.nikfirs.android.traveltracker.core.ui.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.ui.component.FullScreenLoadingIndicator
import ru.nikfirs.android.traveltracker.core.ui.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.ui.model.DateType
import ru.nikfirs.android.traveltracker.core.ui.ui.model.DayCalculation
import ru.nikfirs.android.traveltracker.core.ui.ui.model.ExistingRange
import ru.nikfirs.android.traveltracker.core.ui.ui.model.IconType
import ru.nikfirs.android.traveltracker.core.ui.ui.model.TopBarActionModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.calendar.R
import ru.nikfirs.android.traveltracker.feature.calendar.ui.components.DayInformationCard
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.*
import java.time.LocalDate
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@Composable
fun CalendarScreen(
    navigateRoute: (Any) -> Unit,
    navigateToTripDetails: (tripId: Long) -> Unit,
    navigateToVisaDetails: (visaId: Long) -> Unit,
    viewModel: CalendarViewmodel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffectResolver(flow = viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateToTripDetails -> navigateToTripDetails(effect.tripId)
            is Effect.NavigateToVisaDetails -> navigateToVisaDetails(effect.visaId)
        }
    }
    Screen(
        bottomNavRouteRoute = BottomNavBarRoute.Calendar,
        navigateRoute = navigateRoute,
        actions = listOf(
            TopBarActionModel(
                icon = IconType.DrawableRes(uiR.drawable.ic_tune),
                onClick = {
                    viewModel.setAction(Action.ShowFilters(!state.showFilters))
                },
            )
        )
    ) {
        CalendarContent(
            state = state,
            onAction = viewModel::setAction,
        )
    }

    state.dateInformation?.let { dateInfo ->
        DayInformationCard(
            date = dateInfo.date,
            dateInfo = dateInfo,
            onClose = { viewModel.setAction(Action.ClearDateInfo) },
            onTripClick = { viewModel.setAction(Action.NavigateToTripDetails) },
            onVisaClick = { viewModel.setAction(Action.NavigateToVisaDetails) }
        )
    }

    FullScreenLoadingIndicator(state.isLoading)

    ErrorDialog(
        message = state.error,
        onDismiss = { viewModel.setAction(Action.SetError()) },
    )
}

@Composable
private fun CalendarContent(
    state: State,
    onAction: (Action) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (state.showFilters) {
                    Modifier.clickableOnce {
                        onAction(Action.ShowFilters(false))
                    }
                } else Modifier
            )
    ) {
        CustomCalendar(
            existingRangeList =
            when {
                state.filters.showVisaRange && state.filters.showTripRange -> {
                    state.visaRanges + state.tripRanges
                }

                state.filters.showVisaRange -> state.visaRanges
                state.filters.showTripRange -> state.tripRanges
                else -> emptyList()
            },
            availableDateRange = state.availableDateRange,
            dateList = state.dateList,
            showDots = state.filters.showDayChangeDot,
            showRemainingDays = state.filters.showRemainingDays,
            onDateClick = { onAction(Action.GetDateInfo(it)) },
        )

        AnimatedVisibility(
            visible = state.showFilters,
            enter = expandVertically(),
            exit = shrinkVertically(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            FilterMenu(
                filters = state.filters,
                onFiltersChange = { newFilters ->
                    onAction(Action.UpdateFilters(newFilters))
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun FilterMenu(
    filters: Filters,
    onFiltersChange: (Filters) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.calendar_filters_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            CustomSwitch(
                checked = filters.showRemainingDays,
                onCheckedChange = { isChecked ->
                    onFiltersChange(filters.copy(showRemainingDays = isChecked))
                },
                title = stringResource(R.string.calendar_filter_remaining_days),
                description = stringResource(R.string.calendar_filter_remaining_days_description)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            CustomSwitch(
                checked = filters.showDayChangeDot,
                onCheckedChange = { isChecked ->
                    onFiltersChange(filters.copy(showDayChangeDot = isChecked))
                },
                title = stringResource(R.string.calendar_filter_day_change_indicators),
                description = stringResource(R.string.calendar_filter_day_change_indicators_description)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            CustomSwitch(
                checked = filters.showVisaRange,
                onCheckedChange = { isChecked ->
                    onFiltersChange(filters.copy(showVisaRange = isChecked))
                },
                title = stringResource(R.string.calendar_filter_visa_ranges),
                description = stringResource(R.string.calendar_filter_visa_ranges_description)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            CustomSwitch(
                checked = filters.showTripRange,
                onCheckedChange = { isChecked ->
                    onFiltersChange(filters.copy(showTripRange = isChecked))
                },
                title = stringResource(R.string.calendar_filter_trip_ranges),
                description = stringResource(R.string.calendar_filter_trip_ranges_description)
            )
        }
    }
}

@LightRUScreenPreview
@Composable
private fun CalendarContentPreview() {
    val now = LocalDate.now()
    AppTheme {
        CalendarContent(
            state = State(
                filters = Filters(
                    showRemainingDays = true,
                    showDayChangeDot = false
                ),
                showFilters = false,
                dateList = listOf(
                    DayCalculation(
                        date = now.minusDays(1),
                        remaining = 90,
                        isIncreased = true,
                    ),
                    DayCalculation(
                        date = now.plusDays(1),
                        remaining = 1,
                        isUsed = true,
                    ),
                    DayCalculation(
                        date = now.plusDays(2),
                        remaining = 88,
                        isUsed = true,
                    ),
                    DayCalculation(
                        date = now.plusDays(5),
                        remaining = 87,
                        isUsed = true,
                    ),
                    DayCalculation(
                        date = now.plusDays(6),
                        remaining = 88,
                        isIncreased = true,
                        isUsed = true,
                    ),
                    DayCalculation(
                        date = now.plusDays(7),
                        remaining = 88,
                        isIncreased = true,
                        isUsed = true,
                    )
                ),
                tripRanges = listOf(
                    ExistingRange(
                        startDate = now.plusDays(5),
                        endDate = now.plusDays(8),
                        color = Color.Green,
                        type = DateType.Trip(1),
                    ),
                    ExistingRange(
                        startDate = now.plusDays(10),
                        endDate = now.plusDays(14),
                        color = Color.Magenta,
                        type = DateType.Trip(2),
                    )
                ),
            ),
            onAction = {},
        )
    }
}