package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment

import dagger.hilt.android.lifecycle.HiltViewModel
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.utils.AddTripHolder
import java.time.LocalDate
import javax.inject.Inject
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@HiltViewModel
class AddTripSegmentViewModel @Inject constructor(
    private val addTripHolder: AddTripHolder
) : ViewModel<Action, Effect, State>() {

    init {
        setAction(Action.LoadData)
    }

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.LoadData -> loadData()
            is Action.UpdateCountry -> updateCountry(action.country)
            is Action.UpdateStartDate -> updateStartDate(action.date)
            is Action.UpdateEndDate -> updateEndDate(action.date)
            is Action.UpdateCities -> updateCities(action.cities)
            is Action.SetCountryDropdownExpanded -> setCountryDropdownExpanded(action.expanded)
            is Action.ShowStartDatePicker -> showStartDatePicker()
            is Action.HideStartDatePicker -> hideStartDatePicker()
            is Action.ShowEndDatePicker -> showEndDatePicker()
            is Action.HideEndDatePicker -> hideEndDatePicker()
            is Action.SaveSegment -> saveSegment()
            is Action.DeleteSegment -> deleteSegment()
            is Action.SetError -> setError(action.error)
        }
    }

    private fun loadData() {
        if (!addTripHolder.hasSegmentData()) {
            setError(CustomString.resource(uiR.string.error_loading_data))
            return
        }

        val tripStartDate = addTripHolder.tripStartDate ?: return
        val tripEndDate = addTripHolder.tripEndDate ?: return
        val selectedSegmentDays = addTripHolder.blockedDates
        val isEditMode = addTripHolder.isEditMode()

        val (startDate, endDate) = if (isEditMode) {
            // В режиме редактирования используем существующие даты
            Pair(
                addTripHolder.existingStartDate ?: tripStartDate,
                addTripHolder.existingEndDate ?: tripEndDate
            )
        } else {
            // В режиме добавления находим первый доступный диапазон
            findFirstAvailableDateRange(tripStartDate, tripEndDate, selectedSegmentDays)
        }

        setState {
            it.copy(
                tripStartDate = tripStartDate,
                tripEndDate = tripEndDate,
                selectedSegmentDays = selectedSegmentDays,
                isEditMode = isEditMode,
                country = addTripHolder.existingCountry ?: "",
                startDate = startDate,
                endDate = endDate,
                cities = addTripHolder.existingCities ?: ""
            )
        }
    }

    private fun findFirstAvailableDateRange(
        tripStart: LocalDate,
        tripEnd: LocalDate,
        blockedDates: Set<LocalDate>
    ): Pair<LocalDate, LocalDate> {
        // Если нет заблокированных дат, возвращаем всю поездку
        if (blockedDates.isEmpty()) {
            return Pair(tripStart, tripEnd)
        }

        // Ищем первый доступный период
        var currentStart = tripStart

        while (!currentStart.isAfter(tripEnd)) {
            if (!blockedDates.contains(currentStart)) {
                // Нашли начало доступного периода, ищем конец
                var currentEnd = currentStart
                while (!currentEnd.isAfter(tripEnd) && !blockedDates.contains(currentEnd)) {
                    currentEnd = currentEnd.plusDays(1)
                }
                currentEnd = currentEnd.minusDays(1) // Возвращаемся к последней доступной дате

                return Pair(currentStart, currentEnd)
            }
            currentStart = currentStart.plusDays(1)
        }

        // Если не нашли доступный период, возвращаем начальную дату
        return Pair(tripStart, tripStart)
    }

    private fun updateCountry(country: String) {
        setState {
            it.copy(
                country = country,
                isCountryDropdownExpanded = false,
                validationErrors = currentState.validationErrors.copy(
                    countryError = null
                )
            )
        }
    }

    private fun updateStartDate(date: LocalDate) {
        val endDate = if (currentState.endDate.isBefore(date)) date else currentState.endDate

        setState {
            it.copy(
                startDate = date,
                endDate = endDate,
                showStartDatePicker = false,
                validationErrors = validateDates(date, endDate)
            )
        }
    }

    private fun updateEndDate(date: LocalDate) {
        setState {
            it.copy(
                endDate = date,
                showEndDatePicker = false,
                validationErrors = validateDates(currentState.startDate, date)
            )
        }
    }

    private fun updateCities(cities: String) {
        setState { it.copy(cities = cities) }
    }

    private fun setCountryDropdownExpanded(expanded: Boolean) {
        setState { it.copy(isCountryDropdownExpanded = expanded) }
    }

    private fun showStartDatePicker() {
        setState { it.copy(showStartDatePicker = true) }
    }

    private fun hideStartDatePicker() {
        setState { it.copy(showStartDatePicker = false) }
    }

    private fun showEndDatePicker() {
        setState { it.copy(showEndDatePicker = true) }
    }

    private fun hideEndDatePicker() {
        setState { it.copy(showEndDatePicker = false) }
    }

    private fun saveSegment() {
        val validationErrors = validateForm()

        if (validationErrors.isEmpty()) {
            val citiesList = currentState.cities
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            // Сохраняем результат в холдер
            addTripHolder.setSegmentResult(
                country = currentState.country,
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                cities = citiesList,
                isUpdate = currentState.isEditMode,
                segmentIndex = addTripHolder.segmentIndex
            )

            setEffect { Effect.NavigateBack }
        } else {
            setState { it.copy(validationErrors = validationErrors) }
        }
    }

    private fun deleteSegment() {
        addTripHolder.segmentIndex?.let { index ->
            addTripHolder.setDeletedSegmentIndex(index)
            setEffect { Effect.NavigateBack }
        } ?: run {
            setError(CustomString.resource(uiR.string.error_segment_not_found))
        }
    }

    private fun validateForm(): AddTripSegmentContract.ValidationErrors {
        val startDate = currentState.startDate
        val endDate = currentState.endDate
        val country = currentState.country
        val tripStart = currentState.tripStartDate
        val tripEnd = currentState.tripEndDate

        return AddTripSegmentContract.ValidationErrors(
            countryError = if (country.isBlank())
                CustomString.resource(uiR.string.error_segment_country_required) else null,
            startDateError = when {
                startDate.isBefore(tripStart) ->
                    CustomString.resource(uiR.string.error_segment_start_before_trip)

                startDate.isAfter(tripEnd) ->
                    CustomString.resource(uiR.string.error_segment_start_after_trip)

                else -> null
            },
            endDateError = when {
                endDate.isBefore(startDate) ->
                    CustomString.resource(uiR.string.error_end_date_before_start)

                endDate.isAfter(tripEnd) ->
                    CustomString.resource(uiR.string.error_segment_end_after_trip)

                else -> null
            },
            datesRangeError = if (hasDateConflicts(startDate, endDate))
                CustomString.resource(uiR.string.error_segment_dates_conflict) else null
        )
    }

    private fun validateDates(
        startDate: LocalDate,
        endDate: LocalDate
    ): AddTripSegmentContract.ValidationErrors {
        val tripStart = currentState.tripStartDate
        val tripEnd = currentState.tripEndDate

        return currentState.validationErrors.copy(
            startDateError = when {
                startDate.isBefore(tripStart) ->
                    CustomString.resource(uiR.string.error_segment_start_before_trip)

                startDate.isAfter(tripEnd) ->
                    CustomString.resource(uiR.string.error_segment_start_after_trip)

                else -> null
            },
            endDateError = when {
                endDate.isBefore(startDate) ->
                    CustomString.resource(uiR.string.error_end_date_before_start)

                endDate.isAfter(tripEnd) ->
                    CustomString.resource(uiR.string.error_segment_end_after_trip)

                else -> null
            },
            datesRangeError = if (hasDateConflicts(startDate, endDate))
                CustomString.resource(uiR.string.error_segment_dates_conflict) else null
        )
    }

    private fun hasDateConflicts(startDate: LocalDate, endDate: LocalDate): Boolean {
        val blockedDates = currentState.selectedSegmentDays

        var checkDate = startDate
        while (!checkDate.isAfter(endDate)) {
            if (blockedDates.contains(checkDate)) {
                return true
            }
            checkDate = checkDate.plusDays(1)
        }
        return false
    }

    private fun setError(error: CustomString?) {
        setState { it.copy(error = error) }
    }
}