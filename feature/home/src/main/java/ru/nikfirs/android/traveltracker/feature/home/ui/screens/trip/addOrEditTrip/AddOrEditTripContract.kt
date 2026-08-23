package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addOrEditTrip

import ru.nikfirs.android.traveltracker.core.domain.MAX_STAY_DAYS
import ru.nikfirs.android.traveltracker.core.domain.WARNING_THRESHOLD
import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.ui.ui.model.BlockDateModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState
import ru.nikfirs.android.traveltracker.feature.home.ui.model.TripSegmentUi
import ru.nikfirs.android.traveltracker.feature.home.ui.model.VisaUi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class AddOrEditTripContract {
    data class State(
        val isLoading: Boolean = false,
        val tripId: Long? = null,

        val selectedVisa: VisaUi? = null,
        val isVisaDropdownExpanded: Boolean = false,
        val availableVisas: List<VisaUi> = emptyList(),

        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null,
        val showDatePicker: Boolean = false,
        val blockedPeriods: Set<BlockDateModel> = emptySet(),

        val dateFormatter: DateTimeFormatter = AppDateFormatModel.getDefault().getFormatter(),
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
            get() = if (selectedVisa?.visa?.visaType != VisaCategory.TYPE_C) {
                selectedVisa?.visa?.country
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
        data class LoadData(val tripId: Long?) : Action()
        data class UpdateSegmentList(val segments: List<TripSegmentUi>) : Action()

        data class SetVisaDropdownExpanded(val expanded: Boolean) : Action()
        data class UpdateSelectedVisa(val visa: VisaUi?) : Action()

        data class UpdateDates(val startDate: LocalDate, val endDate: LocalDate) : Action()
        data class CalculateBlockDaysByStartDate(val startDate: LocalDate?) : Action()
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
    }

    sealed class Effect : MviEffect {
        data object NavigateBack : Effect()
        data object ScrollUp : Effect()
        data class OpenSegmentEditor(
            val tripStartDate: LocalDate,
            val tripEndDate: LocalDate,
            val segments: List<TripSegmentUi>,
            val visaExemptCountry: String?,
            val editedSegment: TripSegmentUi? = null,
        ) : Effect()
    }
}