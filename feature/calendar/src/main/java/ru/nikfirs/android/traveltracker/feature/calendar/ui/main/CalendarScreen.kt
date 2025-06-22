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
import ru.nikfirs.android.traveltracker.core.ui.component.CustomCalendar
import ru.nikfirs.android.traveltracker.core.ui.component.CustomSwitch
import ru.nikfirs.android.traveltracker.core.ui.component.DayCalculation
import ru.nikfirs.android.traveltracker.core.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.component.ExistingRange
import ru.nikfirs.android.traveltracker.core.ui.component.FullScreenLoadingIndicator
import ru.nikfirs.android.traveltracker.core.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.model.IconType
import ru.nikfirs.android.traveltracker.core.ui.model.TopBarActionModel
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.calendar.R
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.*
import java.time.LocalDate
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@Composable
fun CalendarScreen(
    navigateRoute: (Any) -> Unit,
    viewModel: CalendarViewmodel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                title = stringResource(R.string.filter_remaining_days),
                description = stringResource(R.string.filter_remaining_days_desc)
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
                title = stringResource(R.string.filter_day_change_indicators),
                description = stringResource(R.string.filter_day_change_indicators_desc)
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
                title = stringResource(R.string.filter_visa_ranges),
                description = stringResource(R.string.filter_visa_ranges_desc)
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
                title = stringResource(R.string.filter_trip_ranges),
                description = stringResource(R.string.filter_trip_ranges_desc)
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
                showFilters = true,
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
                        id = 1
                    ),
                    ExistingRange(
                        startDate = now.plusDays(10),
                        endDate = now.plusDays(14),
                        color = Color.Magenta,
                        id = 2,
                    )
                ),
            ),
            onAction = {},
        )
    }
}