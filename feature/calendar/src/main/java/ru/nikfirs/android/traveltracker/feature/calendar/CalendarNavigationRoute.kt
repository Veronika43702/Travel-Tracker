package ru.nikfirs.android.traveltracker.feature.calendar

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
sealed class CalendarRoute {

    @Serializable
    data class Example(val visaId: Long? = null)

}