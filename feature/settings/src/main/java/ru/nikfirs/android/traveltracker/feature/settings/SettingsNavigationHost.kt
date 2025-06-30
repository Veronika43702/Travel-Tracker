package ru.nikfirs.android.traveltracker.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.DeepRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.navigateBottomNavBarRoute
import ru.nikfirs.android.traveltracker.feature.settings.ui.settings.SettingsScreen

fun NavGraphBuilder.settingsNavigationGraph(
    navController: NavHostController,
    navigateDeepRoute: (DeepRoute) -> Unit,
) {
    composable<BottomNavBarRoute.Settings> {
        SettingsScreen(
            navigateRoute = { navController.navigateBottomNavBarRoute(it) },
        )
    }
}