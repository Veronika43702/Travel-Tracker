package ru.nikfirs.android.traveltracker.feature.home

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
sealed class HomeRoute {

    // Visa
    @Serializable
    data class SaveOrEditVisa(val visaId: Long? = null)

    @Serializable
    data class VisaDetails(val visaId: Long, val isEditable: Boolean = false)

    // Trip
    @Serializable
    data class AddTripGraph(val tripId: Long? = null)

    @Serializable
    data object AddTrip

    @Serializable
    data object AddTripSegment

    @Serializable
    data class TripDetails(val tripId: Long, val isEditable: Boolean = false)

}