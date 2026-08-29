package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.common

import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState
import ru.nikfirs.android.traveltracker.feature.home.ui.model.TripSegmentUi
import java.time.LocalDate

/**
 * Shared contract for [ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.common.AddTripCommonViewModel],
 * scoped to the AddTripGraph navigation graph. Carries the trip-segment editor sub-flow data
 * between AddOrEditTripScreen and AddTripSegmentScreen.
 */
sealed class AddTripCommonContract {

    data class State(
        val tripStartDate: LocalDate? = null,
        val tripEndDate: LocalDate? = null,
        val segments: List<TripSegmentUi> = emptyList(),
        val visaExemptCountry: String? = null,
        val editedSegment: TripSegmentUi? = null,
    ) : MviState {

        val hasTripData: Boolean
            get() = tripStartDate != null && tripEndDate != null

        val isEditMode: Boolean
            get() = editedSegment != null

        val editedSegmentCities: String
            get() = editedSegment?.cities?.joinToString(", ").orEmpty()
    }

    sealed class Action : MviAction {
        data class PrepareSegmentEditor(
            val tripStartDate: LocalDate,
            val tripEndDate: LocalDate,
            val segments: List<TripSegmentUi>,
            val visaExemptCountry: String?,
            val editedSegment: TripSegmentUi? = null,
        ) : Action()

        data class SaveSegment(val segment: TripSegmentUi) : Action()
        data object DeleteEditedSegment : Action()
    }

    sealed class Effect : MviEffect
}
