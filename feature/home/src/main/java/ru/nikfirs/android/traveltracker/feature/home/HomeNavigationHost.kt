package ru.nikfirs.android.traveltracker.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.DeepRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.navigateBottomNavBarRoute
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.visa.addVisa.AddVisaScreen

fun NavGraphBuilder.homeNavigationGraph(
    navController: NavHostController,
    navigateDeepRoute: (DeepRoute) -> Unit,
) {
    composable<BottomNavBarRoute.Home> {
        HomeScreen(
            navigateToAddVisa = { navController.navigate(HomeRoute.AddVisa) },
            navigateToAddTrip = {},
            navigateToEditVisa = {},
            navigateToEditTrip = {},
            navigateRoute = { navController.navigateBottomNavBarRoute(it) },
        )
    }
    composable<HomeRoute.AddVisa> {
        AddVisaScreen(
            onNavigateBack = { navController.popBackStack() },
        )
    }
}