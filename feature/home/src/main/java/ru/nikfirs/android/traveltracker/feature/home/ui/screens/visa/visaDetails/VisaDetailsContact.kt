package ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails

import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.utils.VisaAction
import java.time.format.DateTimeFormatter

sealed class VisaDetailsContract {
    data class State(
        val isLoading: Boolean = false,
        val visa: Visa? = null,
        val error: CustomString? = null,
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        val dialogText: CustomString? = null,
        val action: VisaAction? = null,
    ) : MviState

    sealed class Action : MviAction {
        data class LoadData(val visaId: Long) : Action()
        data object Annul : Action()
        data object Delete : Action()
        data object ShowDeleteDialog : Action()
        data object ShowAnnulDialog : Action()
        data object HideDialog : Action()
        data class SetError(val error: CustomString? = null) : Action()
    }

    sealed class Effect : MviEffect {
        data object NavigateBack : Effect()
    }
}