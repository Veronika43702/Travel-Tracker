package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment

import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class AddTripSegmentContract {
    data class State(
        val isLoading: Boolean = false,
        val country: String = "",
        val startDate: LocalDate = LocalDate.now(),
        val endDate: LocalDate = LocalDate.now(),
        val cities: String = "",
        val isCountryDropdownExpanded: Boolean = false,
        val showStartDatePicker: Boolean = false,
        val showEndDatePicker: Boolean = false,
        val tripStartDate: LocalDate = LocalDate.now(),
        val tripEndDate: LocalDate = LocalDate.now(),
        val selectedSegmentDays: Set<LocalDate> = emptySet(),
        val isEditMode: Boolean = false,
        val error: CustomString? = null,
        val validationErrors: ValidationErrors = ValidationErrors(),
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"),
    ) : MviState {

        val duration: Long
            get() = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1

        val availableStartDate: LocalDate
            get() = if (startDate.isBefore(tripStartDate)) tripStartDate else startDate

        val availableEndDate: LocalDate
            get() = if (endDate.isAfter(tripEndDate)) tripEndDate else endDate
    }

    data class ValidationErrors(
        val countryError: CustomString? = null,
        val startDateError: CustomString? = null,
        val endDateError: CustomString? = null,
        val datesRangeError: CustomString? = null
    ) {
        fun isEmpty(): Boolean = countryError == null &&
                startDateError == null &&
                endDateError == null &&
                datesRangeError == null
    }

    sealed class Action : MviAction {
        data object LoadData : Action()

        data class UpdateCountry(val country: String) : Action()
        data class UpdateStartDate(val date: LocalDate) : Action()
        data class UpdateEndDate(val date: LocalDate) : Action()
        data class UpdateCities(val cities: String) : Action()

        data class SetCountryDropdownExpanded(val expanded: Boolean) : Action()

        data object ShowStartDatePicker : Action()
        data object HideStartDatePicker : Action()
        data object ShowEndDatePicker : Action()
        data object HideEndDatePicker : Action()

        data object SaveSegment : Action()
        data object DeleteSegment : Action()

        data class SetError(val error: CustomString? = null) : Action()
    }

    sealed class Effect : MviEffect {
        data object NavigateBack : Effect()
    }
}