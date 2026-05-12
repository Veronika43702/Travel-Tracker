package ru.nikfirs.android.traveltracker.core.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import ru.nikfirs.android.traveltracker.core.ui.ui.model.IconType

@Immutable
@Serializable
sealed class BottomNavBarRoute {

    @Serializable
    data object Home : BottomNavBarRoute()

    @Serializable
    data object Calendar : BottomNavBarRoute()

    @Serializable
    data object Settings : BottomNavBarRoute()

}

fun BottomNavBarRoute.getSelectedIcon(): IconType {
    return when (this) {
        BottomNavBarRoute.Home -> IconType.VectorIcon(Icons.Filled.Home)
        BottomNavBarRoute.Calendar -> IconType.VectorIcon(Icons.Filled.DateRange)
        BottomNavBarRoute.Settings -> IconType.VectorIcon(Icons.Filled.Settings)
    }
}

fun BottomNavBarRoute.getUnselectedIcon(): IconType {
    return when (this) {
        BottomNavBarRoute.Home -> IconType.VectorIcon(Icons.Outlined.Home)
        BottomNavBarRoute.Calendar -> IconType.VectorIcon(Icons.Outlined.DateRange)
        BottomNavBarRoute.Settings -> IconType.VectorIcon(Icons.Outlined.Settings)
    }
}

fun getBottomNavBarItems(): List<BottomNavBarRoute> {
    return listOf(
        BottomNavBarRoute.Home,
        BottomNavBarRoute.Calendar,
        BottomNavBarRoute.Settings,
    )
}