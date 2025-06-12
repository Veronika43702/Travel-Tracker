package ru.nikfirs.android.traveltracker.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.DeepRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.navigateBottomNavBarRoute
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.visa.addVisa.AddVisaScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.visa.editVisa.EditVisaScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.visa.visaDetails.VisaDetailsScreen

fun NavGraphBuilder.homeNavigationGraph(
    navController: NavHostController,
    navigateDeepRoute: (DeepRoute) -> Unit,
) {
    composable<BottomNavBarRoute.Home> {
        HomeScreen(
            navigateToAddVisa = { navController.navigate(HomeRoute.AddVisa) },
            navigateToAddTrip = {},
            navigateToEditVisa = { navController.navigate(HomeRoute.EditVisa(it)) },
            navigateToEditTrip = {},
            navigateToVisaDetails = { navController.navigate(HomeRoute.VisaDetails(it)) },
            navigateRoute = { navController.navigateBottomNavBarRoute(it) },
        )
    }
    composable<HomeRoute.AddVisa> {
        AddVisaScreen(
            onNavigateBack = { navController.popBackStack() },
        )
    }
    composable<HomeRoute.VisaDetails> { backStack ->
        val route = backStack.toRoute<HomeRoute.VisaDetails>()
        VisaDetailsScreen(
            visaId = route.visaId,
            navigateBack = { navController.popBackStack() },
            navigateToEdit = { navController.navigate(HomeRoute.EditVisa(route.visaId)) }
        )
    }
    composable<HomeRoute.EditVisa> { backStack ->
        val route = backStack.toRoute<HomeRoute.EditVisa>()
        EditVisaScreen(
            visaId = route.visaId,
            navigateBack = { navController.popBackStack() },
        )
    }
}