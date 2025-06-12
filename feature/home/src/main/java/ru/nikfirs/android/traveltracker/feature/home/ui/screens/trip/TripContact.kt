package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip

import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState

sealed class TripContract {
    data class State(
        val isLoading: Boolean = true,
        val trips: List<Trip> = emptyList(),
        val error: CustomString? = null
    ) : MviState

    sealed class Action : MviAction {
        data object LoadData : Action()
        data class DeleteTrip(val trip: Trip) : Action()
    }

    sealed class Effect : MviEffect {
        data object DataDeleted : Effect()
    }
}

