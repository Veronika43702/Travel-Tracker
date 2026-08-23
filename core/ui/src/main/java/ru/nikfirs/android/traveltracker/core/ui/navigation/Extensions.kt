package ru.nikfirs.android.traveltracker.core.ui.navigation

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
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

/**
 * Pops the back stack only when [from] (the calling screen's entry) is RESUMED.
 * Guards against duplicated back navigation.
 * After the first pop the entry leaves RESUMED and repeated calls are ignored.
 */
fun NavHostController.popBackStackOnce(from: NavBackStackEntry): Boolean {
    return if (from.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        popBackStack()
    } else {
        false
    }
}

/**
 * Navigates to [route] only when the current back stack entry is RESUMED.
 * Guards forward navigation against duplicated taps: the first navigate() moves
 * the stack top out of RESUMED for the whole transition, so a second tap racing
 * the animation (the same card twice, or two different cards) is ignored.
 */
fun NavHostController.navigateOnce(route: Any) {
    val resumed = currentBackStackEntry?.lifecycle?.currentState
        ?.isAtLeast(Lifecycle.State.RESUMED) != false
    if (resumed) {
        navigate(route)
    }
}
