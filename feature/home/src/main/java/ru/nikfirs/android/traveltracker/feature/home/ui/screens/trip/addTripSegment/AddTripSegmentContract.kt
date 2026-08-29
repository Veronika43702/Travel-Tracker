package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment

import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.model.Country
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.ui.model.DateRangeSelection
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState
import ru.nikfirs.android.traveltracker.feature.home.ui.model.TripSegmentUi
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.common.AddTripCommonContract
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class AddTripSegmentContract {
    data class State(
        val isLoading: Boolean = false,
        val countryText: String = "",
        val country: String = "",
        val countryListFull: List<Country> = emptyList(),
        val countryListToShow: List<Country> = emptyList(),
        val isCountryDropdownExpanded: Boolean = false,
        val selectedDateRange: DateRangeSelection = DateRangeSelection(),
        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null,
        val showCalendar: Boolean = false,
        val cities: String = "",
        val tripStartDate: LocalDate = LocalDate.now(),
        val tripEndDate: LocalDate = LocalDate.now(),
        val segmentList: List<TripSegmentUi> = emptyList(),
        val isEditMode: Boolean = false,
        val visaExemptCountry: String? = null,
        val error: CustomString? = null,
        val validationErrors: ValidationErrors = ValidationErrors(),
        val dateFormatter: DateTimeFormatter = AppDateFormatModel.getDefault().getFormatter(),
    ) : MviState {

        val duration: Long
            get() = if (selectedDateRange.isComplete) {
                java.time.temporal.ChronoUnit.DAYS.between(
                    selectedDateRange.startDate!!,
                    selectedDateRange.endDate!!
                ) + 1
            } else 0

        val availableDateRange: ClosedRange<LocalDate>
            get() = tripStartDate..tripEndDate
    }

    data class ValidationErrors(
        val countryEmptyError: CustomString? = null,
        val countryNotFromListError: CustomString? = null,
        val datesError: CustomString? = null,
    ) {
        fun isEmpty(): Boolean = countryEmptyError == null
                && countryNotFromListError == null
                && datesError == null
    }

    sealed class Action : MviAction {
        data class LoadData(val commonState: AddTripCommonContract.State) : Action()

        data class UpdateCountryText(
            val value: String,
            val language: String,
            val resetCountry: Boolean = true,
        ) : Action()
        data class UpdateCountry(val country: String) : Action()
        data class ShowDatePicker(val value: Boolean) : Action()
        data class UpdateDateRange(val dateRange: DateRangeSelection) : Action()
        data class OnDateRangeComplete(val startDate: LocalDate, val endDate: LocalDate) : Action()
        data class UpdateCities(val cities: String) : Action()

        data class SetCountryDropdownExpanded(val expanded: Boolean) : Action()

        data object SaveSegment : Action()
        data object DeleteSegment : Action()

        data class SetError(val error: CustomString? = null) : Action()
    }

    sealed class Effect : MviEffect {
        data class SegmentSaved(val segment: TripSegmentUi) : Effect()
        data object SegmentDeleted : Effect()
    }
}