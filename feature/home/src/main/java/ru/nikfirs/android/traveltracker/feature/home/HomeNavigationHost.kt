package ru.nikfirs.android.traveltracker.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import ru.nikfirs.android.traveltracker.core.ui.model.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.model.DeepRoute

fun NavGraphBuilder.homeNavigationGraph(
    navController: NavHostController,
    navigateDeepRoute: (DeepRoute) -> Unit,
) {
    composable<BottomNavBarRoute.Home> {

    }
    composable<HomeRoute.Example> {

    }
}