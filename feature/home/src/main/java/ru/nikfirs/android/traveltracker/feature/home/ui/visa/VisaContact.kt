package ru.nikfirs.android.traveltracker.feature.home.ui.visa

import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState

sealed class VisaContract {
    data class State(
        val isLoading: Boolean = true,
        val visas: List<Visa> = emptyList(),
        val error: CustomString? = null
    ) : MviState

    sealed class Action : MviAction {
        data object LoadData : Action()
        data class DeleteVisa(val visa: Visa) : Action()
    }

    sealed class Effect : MviEffect {
        data object DataDeleted : Effect()
    }
}

