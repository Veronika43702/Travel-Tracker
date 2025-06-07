package ru.nikfirs.android.traveltracker.core.ui.navigation

import androidx.compose.runtime.Immutable

@Immutable
sealed class DeepRoute {
    data object Home : DeepRoute() // example
}