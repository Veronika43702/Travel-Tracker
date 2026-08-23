package ru.nikfirs.android.traveltracker.feature.calendar

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.DeepRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.navigateBottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.popBackStackOnce
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarScreen

fun NavGraphBuilder.calendarNavigationGraph(
    navController: NavHostController,
    navigateDeepRoute: (DeepRoute) -> Unit,
) {
    composable<BottomNavBarRoute.Calendar> { backStack ->
        CalendarScreen(
            navigateRoute = { navController.navigateBottomNavBarRoute(it) },
            navigateBack = { navController.popBackStackOnce(backStack) },
            navigateToTripDetails = { navigateDeepRoute(DeepRoute.TripDetails(it)) },
            navigateToVisaDetails = { navigateDeepRoute(DeepRoute.VisaDetails(it)) },
        )
    }
}