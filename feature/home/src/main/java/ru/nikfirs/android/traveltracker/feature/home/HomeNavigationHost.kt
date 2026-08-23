package ru.nikfirs.android.traveltracker.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.DeepRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.navigateBottomNavBarRoute
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addOrEditTrip.AddOrEditTripScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.common.AddTripCommonViewModel
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.tripDetails.TripDetailsScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.addOrEditVisa.AddOrEditVisaScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails.VisaDetailsScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.HomeScreen

fun NavGraphBuilder.homeNavigationGraph(
    navController: NavHostController,
    navigateDeepRoute: (DeepRoute) -> Unit,
) {
    composable<BottomNavBarRoute.Home> {
        HomeScreen(
            navigateToAddVisa = { navController.navigate(HomeRoute.SaveOrEditVisa()) },
            navigateToAddTrip = { navController.navigate(HomeRoute.AddTripGraph()) },
            navigateToEditVisa = { navController.navigate(HomeRoute.SaveOrEditVisa(it)) },
            navigateToEditTrip = { navController.navigate(HomeRoute.AddTripGraph(it)) },
            navigateToVisaDetails = { navController.navigate(HomeRoute.VisaDetails(it, true)) },
            navigateToTripDetails = { navController.navigate(HomeRoute.TripDetails(it, true)) },
            navigateRoute = { navController.navigateBottomNavBarRoute(it) },
        )
    }
    // Visa
    composable<HomeRoute.SaveOrEditVisa> { backStack ->
        val route = backStack.toRoute<HomeRoute.SaveOrEditVisa>()
        AddOrEditVisaScreen(
            visaId = route.visaId,
            navigateBack = { navController.popBackStack() },
        )
    }
    composable<HomeRoute.VisaDetails> { backStack ->
        val route = backStack.toRoute<HomeRoute.VisaDetails>()
        VisaDetailsScreen(
            visaId = route.visaId,
            isEditable = route.isEditable,
            navigateBack = { navController.popBackStack() },
            navigateToEdit = { navController.navigate(HomeRoute.SaveOrEditVisa(route.visaId)) }
        )
    }
    // Trip
    navigation<HomeRoute.AddTripGraph>(startDestination = HomeRoute.AddTrip) {
        composable<HomeRoute.AddTrip> { backStack ->
            val graphRoute = remember(backStack) {
                navController.getBackStackEntry<HomeRoute.AddTripGraph>()
                    .toRoute<HomeRoute.AddTripGraph>()
            }
            AddOrEditTripScreen(
                commonViewModel = backStack.addTripCommonViewModel(navController),
                tripId = graphRoute.tripId,
                navigateToTripSegment = { navController.navigate(HomeRoute.AddTripSegment) },
                navigateBack = { navController.popBackStack() },
            )
        }
        composable<HomeRoute.AddTripSegment> { backStack ->
            AddTripSegmentScreen(
                commonViewModel = backStack.addTripCommonViewModel(navController),
                navigateBack = { navController.popBackStack() },
            )
        }
    }
    composable<HomeRoute.TripDetails> { backStack ->
        val route = backStack.toRoute<HomeRoute.TripDetails>()
        TripDetailsScreen(
            tripId = route.tripId,
            isEditable = route.isEditable,
            navigateBack = { navController.popBackStack() },
            navigateToVisaDetails = { navController.navigate(HomeRoute.VisaDetails(it)) },
            navigateToEdit = { navController.navigate(HomeRoute.AddTripGraph(route.tripId)) },
        )
    }
}

@Composable
private fun NavBackStackEntry.addTripCommonViewModel(
    navController: NavHostController,
): AddTripCommonViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry<HomeRoute.AddTripGraph>()
    }
    return hiltViewModel(parentEntry)
}