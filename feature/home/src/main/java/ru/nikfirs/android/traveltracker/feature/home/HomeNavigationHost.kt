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
import ru.nikfirs.android.traveltracker.core.ui.navigation.navigateOnce
import ru.nikfirs.android.traveltracker.core.ui.navigation.popBackStackOnce
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
            navigateToAddVisa = { navController.navigateOnce(HomeRoute.SaveOrEditVisa()) },
            navigateToAddTrip = { navController.navigateOnce(HomeRoute.AddTripGraph()) },
            navigateToEditVisa = { navController.navigateOnce(HomeRoute.SaveOrEditVisa(it)) },
            navigateToEditTrip = { navController.navigateOnce(HomeRoute.AddTripGraph(it)) },
            navigateToVisaDetails = { navController.navigateOnce(HomeRoute.VisaDetails(it, true)) },
            navigateToTripDetails = { navController.navigateOnce(HomeRoute.TripDetails(it, true)) },
            navigateRoute = { navController.navigateBottomNavBarRoute(it) },
        )
    }
    // Visa
    composable<HomeRoute.SaveOrEditVisa> { backStack ->
        val route = backStack.toRoute<HomeRoute.SaveOrEditVisa>()
        AddOrEditVisaScreen(
            visaId = route.visaId,
            navigateBack = { navController.popBackStackOnce(backStack) },
        )
    }
    composable<HomeRoute.VisaDetails> { backStack ->
        val route = backStack.toRoute<HomeRoute.VisaDetails>()
        VisaDetailsScreen(
            visaId = route.visaId,
            isEditable = route.isEditable,
            navigateBack = { navController.popBackStackOnce(backStack) },
            navigateToEdit = { navController.navigateOnce(HomeRoute.SaveOrEditVisa(route.visaId)) }
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
                navigateToTripSegment = { navController.navigateOnce(HomeRoute.AddTripSegment) },
                navigateBack = { navController.popBackStackOnce(backStack) },
            )
        }
        composable<HomeRoute.AddTripSegment> { backStack ->
            AddTripSegmentScreen(
                commonViewModel = backStack.addTripCommonViewModel(navController),
                navigateBack = { navController.popBackStackOnce(backStack) },
            )
        }
    }
    composable<HomeRoute.TripDetails> { backStack ->
        val route = backStack.toRoute<HomeRoute.TripDetails>()
        TripDetailsScreen(
            tripId = route.tripId,
            isEditable = route.isEditable,
            navigateBack = { navController.popBackStackOnce(backStack) },
            navigateToVisaDetails = { navController.navigateOnce(HomeRoute.VisaDetails(it)) },
            navigateToEdit = { navController.navigateOnce(HomeRoute.AddTripGraph(route.tripId)) },
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