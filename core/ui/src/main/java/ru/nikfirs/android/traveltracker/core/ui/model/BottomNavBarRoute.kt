package ru.nikfirs.android.traveltracker.core.ui.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed class BottomNavBarRoute {

    @Serializable
    data object Home : BottomNavBarRoute()

}

fun BottomNavBarRoute.getSelectedIcon(): Int {
    return when (this) {
        BottomNavBarRoute.Home -> 0 // TODO
    }
}

fun BottomNavBarRoute.getUnselectedIcon(): Int {
    return when (this) {
        BottomNavBarRoute.Home -> 0 // TODO
    }
}

fun getBottomNavBarItems(): List<BottomNavBarRoute> {
    return listOf(
        BottomNavBarRoute.Home,
    )
}