package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip

import ru.nikfirs.android.traveltracker.core.domain.MAX_STAY_DAYS
import ru.nikfirs.android.traveltracker.core.domain.WARNING_THRESHOLD
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState
import ru.nikfirs.android.traveltracker.feature.home.domain.model.TripSegmentUi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class AddTripContract {
    data class State(
        val isLoading: Boolean = false,

        val selectedVisa: Visa? = null,
        val isVisaDropdownExpanded: Boolean = false,
        val availableVisas: List<Visa> = emptyList(),

        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null,
        val showDatePicker: Boolean = false,
        val blockedDates: Set<LocalDate> = emptySet(),

        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        val daysAvailableAtStart: DaysAvailableInfo? = null,
        val daysAvailableAtEnd: DaysAvailableInfo? = null,
        val countableDuration: Int = 1,

        val purpose: TripPurpose = TripPurpose.TOURISM,
        val isPurposeDropdownExpanded: Boolean = false,

        val segments: List<TripSegmentUi> = emptyList(),
        val notes: String = "",

        val error: CustomString? = null,
        val validationErrors: ValidationErrors = ValidationErrors(),
        val warningTextDaysOutSegments: CustomString? = null,
    ) : MviState {

        val totalDuration: Long
            get() = if (startDate != null && endDate != null) {
                java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
            } else 0

        val hasExemptSegments: Boolean
            get() = segments.any { it.isExempt }

        val hasSelectedVisa: Boolean
            get() = selectedVisa != null

        val hasSelectedDates: Boolean
            get() = startDate != null && endDate != null

        val exemptVisaCountry: String?
            get() = if (selectedVisa?.visaType != VisaCategory.TYPE_C) {
                selectedVisa?.country
            } else null
    }

    data class DaysAvailableInfo(
        val used: Int,
        val remaining: Int,
    ) {
        val displayText: String get() = "$remaining / $MAX_STAY_DAYS"
        val isNearLimit = used > WARNING_THRESHOLD
        val isOverLimit = used > MAX_STAY_DAYS
    }

    data class ValidationErrors(
        val startDateError: CustomString? = null,
        val visaError: CustomString? = null,
        val segmentsError: CustomString? = null,
        val daysLimitError: CustomString? = null
    ) {
        fun isEmpty(): Boolean = startDateError == null &&
                visaError == null &&
                segmentsError == null &&
                daysLimitError == null
    }

    sealed class Action : MviAction {
        data object LoadData : Action()
        data object UpdateSegmentList : Action()

        data class SetVisaDropdownExpanded(val expanded: Boolean) : Action()
        data class UpdateSelectedVisa(val visa: Visa?) : Action()
        data class UpdateDates(val startDate: LocalDate, val endDate: LocalDate) : Action()
        data class ShowDatePicker(val value: Boolean) : Action()
        data class SetPurposeDropdownExpanded(val expanded: Boolean) : Action()
        data class UpdatePurpose(val purpose: TripPurpose) : Action()

        data class UpdateNotes(val notes: String) : Action()


        data class DeleteSegment(val segment: TripSegmentUi) : Action()
        data object OpenAddSegmentEditor : Action()
        data class OpenEditSegmentEditor(val segment: TripSegmentUi) : Action()

        data object SaveTrip : Action()
        data object SaveTripWithTransit : Action()
        data class SetError(val error: CustomString? = null) : Action()
        data class SetWarning(val value: CustomString? = null) : Action()

        data object RecalculateDays : Action()
    }

    sealed class Effect : MviEffect {
        data object NavigateBack : Effect()
        data object ScrollUp : Effect()
        data class ShowMessage(val message: CustomString) : Effect()
        data object OpenSegmentEditor : Effect()
    }
}