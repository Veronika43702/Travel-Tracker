package ru.nikfirs.android.traveltracker.feature.calendar.ui.main

import dagger.hilt.android.lifecycle.HiltViewModel
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import javax.inject.Inject
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.Action
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.Effect
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.State


@HiltViewModel
class CalendarViewmodel @Inject constructor(

) : ViewModel<Action, Effect, State>() {

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            Action.LoadData -> TODO()
            is Action.SetError -> setError(action.error)
        }
    }


    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error) }
    }
}