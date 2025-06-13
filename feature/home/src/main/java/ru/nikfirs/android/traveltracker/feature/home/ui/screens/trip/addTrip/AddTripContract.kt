package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip

import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.SegmentType
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class AddTripContract {
    data class State(
        val isLoading: Boolean = false,
        val startDate: LocalDate = LocalDate.now(),
        val endDate: LocalDate = LocalDate.now(),
        val purpose: TripPurpose = TripPurpose.TOURISM,
        val selectedVisa: Visa? = null,
        val availableVisas: List<Visa> = emptyList(),
        val segments: List<TripSegmentUi> = emptyList(),
        val notes: String = "",
        val exemptCountries: Set<String> = emptySet(),
        val isVisaDropdownExpanded: Boolean = false,
        val isPurposeDropdownExpanded: Boolean = false,
        val showStartDatePicker: Boolean = false,
        val showEndDatePicker: Boolean = false,
        val blockedDatesForStart: Set<LocalDate> = emptySet(),
        val blockedDatesForEnd: Set<LocalDate> = emptySet(),
        val error: CustomString? = null,
        val validationErrors: ValidationErrors = ValidationErrors(),
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        val daysAvailableAtStart: DaysAvailableInfo? = null,
        val daysAvailableAtEnd: DaysAvailableInfo? = null,
    ) : MviState {

        val totalDuration: Long
            get() = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1

        val countableDuration: Long
            get() = segments.filter { !it.isExempt }.sumOf { it.duration }

        val hasExemptSegments: Boolean
            get() = segments.any { it.isExempt }

        val hasSelectedVisa: Boolean
            get() = selectedVisa != null
    }

    data class TripSegmentUi(
        val country: String,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val type: SegmentType = SegmentType.STAY,
        val cities: List<String> = emptyList(),
        val isExempt: Boolean = false
    ) {
        val duration: Long
            get() = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
    }

    data class DaysAvailableInfo(
        val used: Int,
        val total: Int,
        val remaining: Int,
        val isNearLimit: Boolean = false,
        val isOverLimit: Boolean = false
    ) {
        val displayText: String get() = "$remaining / $total"
    }

    data class ValidationErrors(
        val startDateError: CustomString? = null,
        val endDateError: CustomString? = null,
        val visaError: CustomString? = null,
        val segmentsError: CustomString? = null,
        val daysLimitError: CustomString? = null
    ) {
        fun isEmpty(): Boolean = startDateError == null &&
                endDateError == null &&
                visaError == null &&
                segmentsError == null &&
                daysLimitError == null
    }

    sealed class Action : MviAction {
        data object LoadData : Action()
        data class UpdateStartDate(val date: LocalDate) : Action()
        data class UpdateEndDate(val date: LocalDate) : Action()
        data object ShowStartDatePicker : Action()
        data object HideStartDatePicker : Action()
        data object ShowEndDatePicker : Action()
        data object HideEndDatePicker : Action()
        data class UpdatePurpose(val purpose: TripPurpose) : Action()
        data class UpdateSelectedVisa(val visa: Visa?) : Action()
        data class UpdateNotes(val notes: String) : Action()
        data class SetVisaDropdownExpanded(val expanded: Boolean) : Action()
        data class SetPurposeDropdownExpanded(val expanded: Boolean) : Action()
        data class AddSegment(val segment: TripSegmentUi) : Action()
        data class UpdateSegment(val index: Int, val segment: TripSegmentUi) : Action()
        data class RemoveSegment(val index: Int) : Action()
        data object SaveTrip : Action()
        data class SetError(val error: CustomString? = null) : Action()
        data object DismissError : Action()
        data object RecalculateDays : Action()
    }

    sealed class Effect : MviEffect {
        data object NavigateBack : Effect()
        data object ScrollUp : Effect()
        data class ShowMessage(val message: CustomString) : Effect()
        data class OpenSegmentEditor(val segment: TripSegmentUi?, val index: Int?) : Effect()
    }
}