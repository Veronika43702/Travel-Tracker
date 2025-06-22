package ru.nikfirs.android.traveltracker.core.ui.navigation

import android.util.Log
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

fun NavHostController.navigateBottomNavBarRoute(route: Any) {
    try {
        navigate(route) {
            popUpTo(graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    } catch (e: Exception) {
        Log.e("NavController", "navigateBottomNavBarRoute failed", e)
    }
}