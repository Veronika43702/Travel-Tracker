package ru.nikfirs.android.traveltracker.feature.home

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
sealed class HomeRoute {

    // Visa
    @Serializable
    data class SaveOrEditVisa(val visaId: Long? = null)

    @Serializable
    data class VisaDetails(val visaId: Long)

    // Trip
    @Serializable
    data object AddTrip

    @Serializable
    data object AddTripSegment

}