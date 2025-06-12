package ru.nikfirs.android.traveltracker.feature.home

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
sealed class HomeRoute {

    @Serializable
    data object AddVisa

    @Serializable
    data class VisaDetails(val visaId: Long)

    @Serializable
    data class EditVisa(val visaId: Long)

}