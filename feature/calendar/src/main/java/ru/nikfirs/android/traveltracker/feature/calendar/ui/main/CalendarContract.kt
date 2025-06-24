package ru.nikfirs.android.traveltracker.feature.calendar.ui.main

import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.ui.model.DayCalculation
import ru.nikfirs.android.traveltracker.core.ui.model.ExistingRange
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState
import java.time.LocalDate

sealed class CalendarContract {
    data class State(
        val isLoading: Boolean = true,
        val error: CustomString? = null,
        val dateList: List<DayCalculation> = emptyList(),
        val trips: List<Trip> = emptyList(),
        val tripRanges: List<ExistingRange> = emptyList(),
        val visaRanges: List<ExistingRange> = emptyList(),
        val availableDateRange: ClosedRange<LocalDate>? = null,
        val showFilters: Boolean = false,
        val filters: Filters = Filters(),
    ) : MviState

    sealed class Action : MviAction {
        data object LoadData : Action()
        data class SetError(val error: CustomString? = null) : Action()
        data class ShowFilters(val value: Boolean) : Action()
        data class UpdateFilters(val filters: Filters) : Action()
    }

    sealed class Effect : MviEffect {

    }

    data class Filters(
        val showRemainingDays: Boolean = false,
        val showDayChangeDot: Boolean = false,
        val showVisaRange: Boolean = false,
        val showTripRange: Boolean = true,
    )
}