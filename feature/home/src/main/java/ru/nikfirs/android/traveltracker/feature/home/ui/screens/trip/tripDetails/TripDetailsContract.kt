package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.tripDetails

import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState
import ru.nikfirs.android.traveltracker.feature.home.ui.model.TripSegmentUi
import java.time.format.DateTimeFormatter

sealed class TripDetailsContract {
    data class State(
        val isLoading: Boolean = false,
        val trip: Trip? = null,
        val visa: Visa? = null,
        val segmentsForView: List<TripSegmentUi> = emptyList(),
        val expandSegments: Boolean = false,
        val error: CustomString? = null,
        val dateFormatter: DateTimeFormatter = AppDateFormatModel.getDefault().getFormatter(),
        val dialogText: CustomString? = null,
    ) : MviState

    sealed class Action : MviAction {
        data class LoadData(val tripId: Long) : Action()
        data object ChangeExpandSegment : Action()
        data object Delete : Action()
        data object ShowDeleteDialog : Action()
        data object HideDialog : Action()
        data class SetError(val error: CustomString? = null) : Action()
    }

    sealed class Effect : MviEffect {
        data object NavigateBack : Effect()
    }
}