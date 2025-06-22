package ru.nikfirs.android.traveltracker.feature.calendar.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.nikfirs.android.traveltracker.core.ui.component.CustomCalendar
import ru.nikfirs.android.traveltracker.core.ui.component.DayCalculation
import ru.nikfirs.android.traveltracker.core.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.component.ExistingRange
import ru.nikfirs.android.traveltracker.core.ui.component.FullScreenLoadingIndicator
import ru.nikfirs.android.traveltracker.core.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.Action
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.State
import java.time.LocalDate

@Composable
fun CalendarScreen(
    navigateRoute: (Any) -> Unit,
    viewModel: CalendarViewmodel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Screen(
        bottomNavRouteRoute = BottomNavBarRoute.Home,
        navigateRoute = navigateRoute,
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
    CustomCalendar(
        existingRangeList = state.tripRanges,
        dateList = state.dateList,

    )
}

@LightRUScreenPreview
@Composable
private fun CalendarContentPreview() {
    val now = LocalDate.now()
    AppTheme {
        CalendarContent(
            state = State(
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
            onAction = {}
        )
    }
}