package ru.nikfirs.android.traveltracker

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.DeepRoute
import ru.nikfirs.android.traveltracker.feature.home.homeNavigationGraph

@Composable
fun NavigationHost(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavBarRoute.Home,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) }) {
        homeNavigationGraph(
            navController = navController,
            navigateDeepRoute = { deepRoute -> deepRoute.resolve(navController) }
        )
    }
}

internal fun DeepRoute.resolve(
    navController: NavHostController,
) {
    when (this) {
        DeepRoute.Home -> navController.navigate(BottomNavBarRoute.Home) {
            popUpTo(BottomNavBarRoute.Home) { inclusive = true }
            launchSingleTop = true
        }
    }
}