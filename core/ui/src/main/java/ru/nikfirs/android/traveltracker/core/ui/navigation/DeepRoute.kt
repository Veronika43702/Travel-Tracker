package ru.nikfirs.android.traveltracker.core.ui.navigation

import androidx.compose.runtime.Immutable

@Immutable
sealed class DeepRoute {
    data object Home : DeepRoute()
    data class TripDetails(val tripId: Long) : DeepRoute()
    data class VisaDetails(val visaId: Long) : DeepRoute()
}