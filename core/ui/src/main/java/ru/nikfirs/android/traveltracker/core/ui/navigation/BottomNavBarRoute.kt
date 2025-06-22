package ru.nikfirs.android.traveltracker.core.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import ru.nikfirs.android.traveltracker.core.ui.model.IconType

@Immutable
@Serializable
sealed class BottomNavBarRoute {

    @Serializable
    data object Home : BottomNavBarRoute()

    @Serializable
    data object Calendar : BottomNavBarRoute()

}

fun BottomNavBarRoute.getSelectedIcon(): IconType {
    return when (this) {
        BottomNavBarRoute.Home -> IconType.VectorIcon(Icons.Outlined.Home)
        BottomNavBarRoute.Calendar -> TODO()
    }
}

fun BottomNavBarRoute.getUnselectedIcon(): IconType {
    return when (this) {
        BottomNavBarRoute.Home -> IconType.VectorIcon(Icons.Filled.Home)
        BottomNavBarRoute.Calendar -> TODO()
    }
}

fun getBottomNavBarItems(): List<BottomNavBarRoute> {
    return listOf(
        BottomNavBarRoute.Home,
    )
}