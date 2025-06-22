package ru.nikfirs.android.traveltracker.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.DeepRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.navigateBottomNavBarRoute
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.tripDetails.TripDetailsScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.editVisa.AddOrEditVisaScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails.VisaDetailsScreen
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.HomeScreen

fun NavGraphBuilder.homeNavigationGraph(
    navController: NavHostController,
    navigateDeepRoute: (DeepRoute) -> Unit,
) {
    composable<BottomNavBarRoute.Home> {
        HomeScreen(
            navigateToAddVisa = { navController.navigate(HomeRoute.SaveOrEditVisa()) },
            navigateToAddTrip = { navController.navigate(HomeRoute.AddTrip()) },
            navigateToEditVisa = { navController.navigate(HomeRoute.SaveOrEditVisa(it)) },
            navigateToEditTrip = { navController.navigate(HomeRoute.AddTrip(it)) },
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
    composable<HomeRoute.AddTrip> { backStack ->
        val route = backStack.toRoute<HomeRoute.AddTripSegment>()
        // TODO + edit
        AddTripScreen(
            navigateToTripSegment = { navController.navigate(HomeRoute.AddTripSegment) },
            navigateBack = { navController.popBackStack() },
        )
    }
    composable<HomeRoute.AddTripSegment> {
        AddTripSegmentScreen(
            navigateBack = { navController.popBackStack() },
        )
    }
    composable<HomeRoute.TripDetails> { backStack ->
        val route = backStack.toRoute<HomeRoute.TripDetails>()
        TripDetailsScreen(
            tripId = route.tripId,
            isEditable = route.isEditable,
            navigateBack = { navController.popBackStack() },
            navigateToVisaDetails = { navController.navigate(HomeRoute.VisaDetails(it)) },
            navigateToEdit = { navController.navigate(HomeRoute.AddTrip(route.tripId)) },
        )
    }
}