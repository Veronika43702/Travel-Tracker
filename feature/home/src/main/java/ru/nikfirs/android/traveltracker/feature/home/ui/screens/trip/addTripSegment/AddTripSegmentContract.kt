package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment

import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.model.DateRangeSelection
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState
import ru.nikfirs.android.traveltracker.feature.home.domain.model.TripSegmentUi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class AddTripSegmentContract {
    data class State(
        val isLoading: Boolean = false,
        val country: String = "",
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
        val error: CustomString? = null,
        val validationErrors: ValidationErrors = ValidationErrors(),
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"),
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
        val countryError: CustomString? = null,
        val datesError: CustomString? = null
    ) {
        fun isEmpty(): Boolean = countryError == null && datesError == null
    }

    sealed class Action : MviAction {
        data object LoadData : Action()

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
        data object NavigateBack : Effect()
    }
}