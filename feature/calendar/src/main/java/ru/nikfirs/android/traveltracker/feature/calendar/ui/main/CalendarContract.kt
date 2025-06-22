package ru.nikfirs.android.traveltracker.feature.calendar.ui.main

import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.ui.component.DayCalculation
import ru.nikfirs.android.traveltracker.core.ui.component.ExistingRange
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
        val availableDateRange: ClosedRange<LocalDate>? = null
    ) : MviState

    sealed class Action : MviAction {
        data object LoadData : Action()
        data class SetError(val error: CustomString? = null) : Action()

    }

    sealed class Effect : MviEffect {

    }
}