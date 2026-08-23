package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.common

import dagger.hilt.android.lifecycle.HiltViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.common.AddTripCommonContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.common.AddTripCommonContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.common.AddTripCommonContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.model.TripSegmentUi
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.utils.sortedAndRecolored
import javax.inject.Inject

/**
 * Shared ViewModel scoped to the AddTripGraph navigation graph (see HomeNavigationHost.kt).
 * Lives exactly as long as the graph stays in the back stack and is cleared automatically
 * once the flow is left — no manual clear() is needed, unlike the AddTripHolder singleton
 * it replaces.
 */
@HiltViewModel
class AddTripCommonViewModel @Inject constructor() : ViewModel<Action, Effect, State>() {

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.PrepareSegmentEditor -> prepareSegmentEditor(action)
            is Action.SaveSegment -> saveSegment(action.segment)
            Action.DeleteEditedSegment -> deleteEditedSegment()
        }
    }

    private fun prepareSegmentEditor(action: Action.PrepareSegmentEditor) {
        setState {
            it.copy(
                tripStartDate = action.tripStartDate,
                tripEndDate = action.tripEndDate,
                segments = action.segments.sortedAndRecolored(),
                visaExemptCountry = action.visaExemptCountry,
                editedSegment = action.editedSegment,
            )
        }
    }

    private fun saveSegment(segment: TripSegmentUi) {
        setState { state ->
            state.copy(
                segments = (state.segments.filter { it != state.editedSegment } + segment)
                    .sortedAndRecolored(),
                editedSegment = null,
            )
        }
    }

    private fun deleteEditedSegment() {
        setState { state ->
            state.copy(
                segments = state.segments.filter { it != state.editedSegment }
                    .sortedAndRecolored(),
                editedSegment = null,
            )
        }
    }
}
