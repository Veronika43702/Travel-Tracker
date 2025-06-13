package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.CalculateDaysInPeriodUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.GetExemptCountriesUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.GetAllTripsUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.SaveTripUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.GetActiveVisasUseCase
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.State
import java.time.LocalDate
import javax.inject.Inject
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@HiltViewModel
class AddTripViewModel @Inject constructor(
    private val getActiveVisasUseCase: GetActiveVisasUseCase,
    private val getExemptCountriesUseCase: GetExemptCountriesUseCase,
    private val calculateDaysInPeriodUseCase: CalculateDaysInPeriodUseCase,
    private val getAllTripsUseCase: GetAllTripsUseCase,
    private val saveTripUseCase: SaveTripUseCase
) : ViewModel<Action, Effect, State>() {

    init {
        setAction(Action.LoadData)
    }

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.LoadData -> loadData()
            is Action.UpdateStartDate -> updateStartDate(action.date)
            is Action.UpdateEndDate -> updateEndDate(action.date)
            is Action.ShowStartDatePicker -> showStartDatePicker()
            is Action.HideStartDatePicker -> hideStartDatePicker()
            is Action.ShowEndDatePicker -> showEndDatePicker()
            is Action.HideEndDatePicker -> hideEndDatePicker()
            is Action.UpdatePurpose -> updatePurpose(action.purpose)
            is Action.UpdateSelectedVisa -> updateSelectedVisa(action.visa)
            is Action.UpdateNotes -> updateNotes(action.notes)
            is Action.SetVisaDropdownExpanded -> setVisaDropdownExpanded(action.expanded)
            is Action.SetPurposeDropdownExpanded -> setPurposeDropdownExpanded(action.expanded)
            is Action.AddSegment -> addSegment(action.segment)
            is Action.UpdateSegment -> updateSegment(action.index, action.segment)
            is Action.RemoveSegment -> removeSegment(action.index)
            is Action.SaveTrip -> saveTrip()
            is Action.SetError -> setError(action.error)
            is Action.DismissError -> dismissError()
            is Action.RecalculateDays -> recalculateDays()
        }
    }

    private fun calculateBlockedDates(trips: List<Trip>) {
        launch {
            val blockedForStart = calculateBlockedDatesForStartDate(trips)
            val blockedForEnd = calculateBlockedDatesForEndDate(trips)

            setState {
                it.copy(
                    blockedDatesForStart = blockedForStart,
                    blockedDatesForEnd = blockedForEnd
                )
            }
        }
    }

    private fun recalculateBlockedDatesForExistingTrips() {
        launch {
            try {
                getAllTripsUseCase().collect { trips ->
                    calculateBlockedDates(trips)
                }
            } catch (e: Exception) {
                // Ошибка расчета не критична
            }
        }
    }

    private suspend fun calculateBlockedDatesForStartDate(trips: List<Trip>): Set<LocalDate> {
        val blockedDates = mutableSetOf<LocalDate>()

        trips.forEach { trip ->
            var date = trip.startDate
            while (!date.isAfter(trip.endDate)) {
                blockedDates.add(date)
                date = date.plusDays(1)
            }
        }

        // Заблокировать даты вне диапазона действия выбранной визы
        val selectedVisa = currentState.selectedVisa
        if (selectedVisa != null) {
            // Блокируем даты до начала действия визы
            var date = LocalDate.now().minusYears(1)
            while (date.isBefore(selectedVisa.startDate)) {
                blockedDates.add(date)
                date = date.plusDays(1)
            }

            // Блокируем даты после окончания действия визы
            date = selectedVisa.expiryDate.plusDays(1)
            val oneYearAhead = LocalDate.now().plusYears(1)
            while (!date.isAfter(oneYearAhead)) {
                blockedDates.add(date)
                date = date.plusDays(1)
            }
        }

        // Заблокировать даты, которые приведут к превышению 90 дней
        val today = LocalDate.now()
        val oneYearAhead = today.plusYears(1)

        var checkDate = today
        while (!checkDate.isAfter(oneYearAhead)) {
            try {
                val calculation = calculateDaysInPeriodUseCase(
                    periodEnd = checkDate,
                    exemptCountries = currentState.exemptCountries
                )

                // Если на эту дату уже используется 90+ дней, блокируем ее
                if (calculation.totalDaysUsed >= 90) {
                    blockedDates.add(checkDate)
                }
            } catch (e: Exception) {
                // Пропускаем дату при ошибке расчета
            }
            checkDate = checkDate.plusDays(1)
        }

        return blockedDates
    }

    private suspend fun calculateBlockedDatesForEndDate(trips: List<Trip>): Set<LocalDate> {
        val blockedDates = mutableSetOf<LocalDate>()

        // Заблокировать даты существующих поездок (только будущие)
        val today = LocalDate.now()
        trips.filter { it.startDate.isAfter(today) }.forEach { trip ->
            var date = trip.startDate
            while (!date.isAfter(trip.endDate)) {
                blockedDates.add(date)
                date = date.plusDays(1)
            }
        }

        // Заблокировать даты меньше startDate
        val startDate = currentState.startDate
        var date = today.minusYears(1)
        while (date.isBefore(startDate)) {
            blockedDates.add(date)
            date = date.plusDays(1)
        }

        // Заблокировать даты вне диапазона действия выбранной визы
        val selectedVisa = currentState.selectedVisa
        if (selectedVisa != null) {
            // Блокируем даты после окончания действия визы
            date = selectedVisa.expiryDate.plusDays(1)
            val oneYearAhead = today.plusYears(1)
            while (!date.isAfter(oneYearAhead)) {
                blockedDates.add(date)
                date = date.plusDays(1)
            }
        }

        // Заблокировать даты, которые превышают правило 90 дней с учетом длительности поездки
        val oneYearAhead = today.plusYears(1)

        var checkDate = startDate
        while (!checkDate.isAfter(oneYearAhead)) {
            try {
                // Симулируем поездку от startDate до checkDate
                val potentialDuration =
                    java.time.temporal.ChronoUnit.DAYS.between(startDate, checkDate) + 1

                val calculation = calculateDaysInPeriodUseCase(
                    periodEnd = checkDate,
                    exemptCountries = currentState.exemptCountries
                )

                // Если добавление этой поездки превысит 90 дней, блокируем дату
                if (calculation.totalDaysUsed + potentialDuration > 90) {
                    blockedDates.add(checkDate)
                }
            } catch (e: Exception) {
                // Пропускаем дату при ошибке расчета
            }
            checkDate = checkDate.plusDays(1)
        }

        return blockedDates
    }

    private fun loadData() {
        launch {
            setState { it.copy(isLoading = true) }

            try {
                combine(
                    getActiveVisasUseCase(),
                    getExemptCountriesUseCase(),
                    getAllTripsUseCase()
                ) { visas, exemptCountries, trips ->
                    setState {
                        it.copy(
                            isLoading = false,
                            availableVisas = visas,
                            exemptCountries = exemptCountries
                        )
                    }

                    calculateInitialDays()
                    calculateBlockedDates(trips)
                }.collect { }
            } catch (e: Exception) {
                setError(CustomString.resource(uiR.string.error_loading_data))
            }
        }
    }

    private fun calculateInitialDays() {
        launch {
            try {
                val today = LocalDate.now()
                val calculation = calculateDaysInPeriodUseCase(
                    periodEnd = today,
                    exemptCountries = currentState.exemptCountries
                )

                val daysInfo = AddTripContract.DaysAvailableInfo(
                    used = calculation.totalDaysUsed,
                    total = 90,
                    remaining = calculation.remainingDays,
                    isNearLimit = calculation.isNearLimit,
                    isOverLimit = calculation.isOverLimit
                )

                val daysInfoEnd = AddTripContract.DaysAvailableInfo(
                    used = 1,
                    total = 90,
                    remaining = calculation.remainingDays - 1,
                    isNearLimit = calculation.isNearLimit,
                    isOverLimit = calculation.isOverLimit
                )

                setState {
                    it.copy(
                        daysAvailableAtStart = daysInfo,
                        daysAvailableAtEnd = daysInfoEnd
                    )
                }
            } catch (e: Exception) {
                // При ошибке показываем начальное состояние
                val defaultDaysInfo = AddTripContract.DaysAvailableInfo(
                    used = 0,
                    total = 90,
                    remaining = 90
                )
                val defaultDaysInfoEnd = AddTripContract.DaysAvailableInfo(
                    used = 1,
                    total = 90,
                    remaining = 89
                )
                setState {
                    it.copy(
                        daysAvailableAtStart = defaultDaysInfo,
                        daysAvailableAtEnd = defaultDaysInfoEnd,
                    )
                }
            }
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

        recalculateDays()
        recalculateBlockedDatesForExistingTrips()
    }

    private fun updateEndDate(date: LocalDate) {
        setState {
            it.copy(
                endDate = date,
                showEndDatePicker = false,
                validationErrors = validateDates(currentState.startDate, date)
            )
        }

        recalculateDays()
    }

    private fun showStartDatePicker() {
        setState { it.copy(showStartDatePicker = true) }
        recalculateBlockedDatesForExistingTrips()
    }

    private fun hideStartDatePicker() {
        setState { it.copy(showStartDatePicker = false) }
    }

    private fun showEndDatePicker() {
        setState { it.copy(showEndDatePicker = true) }
        recalculateBlockedDatesForExistingTrips()
    }

    private fun hideEndDatePicker() {
        setState { it.copy(showEndDatePicker = false) }
    }

    private fun updatePurpose(purpose: ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose) {
        setState { it.copy(purpose = purpose) }
    }

    private fun updateSelectedVisa(visa: ru.nikfirs.android.traveltracker.core.domain.model.Visa?) {
        setState {
            it.copy(
                selectedVisa = visa,
                isVisaDropdownExpanded = false,
                validationErrors = currentState.validationErrors.copy(visaError = null)
            )
        }

        // Пересчитать сегменты с учетом новой визы
        recalculateSegmentsExemption()
        recalculateDays()

        // Пересчитать заблокированные даты с учетом диапазона новой визы
        recalculateBlockedDatesForExistingTrips()
    }

    private fun updateNotes(notes: String) {
        setState { it.copy(notes = notes) }
    }

    private fun setVisaDropdownExpanded(expanded: Boolean) {
        setState { it.copy(isVisaDropdownExpanded = expanded) }
    }

    private fun setPurposeDropdownExpanded(expanded: Boolean) {
        setState { it.copy(isPurposeDropdownExpanded = expanded) }
    }

    private fun addSegment(segment: AddTripContract.TripSegmentUi) {
        val updatedSegments = currentState.segments + segment.copy(
            isExempt = isCountryExempt(segment.country)
        )

        setState {
            it.copy(
                segments = updatedSegments,
                validationErrors = currentState.validationErrors.copy(segmentsError = null)
            )
        }

        recalculateDays()
    }

    private fun updateSegment(index: Int, segment: AddTripContract.TripSegmentUi) {
        val updatedSegments = currentState.segments.toMutableList()
        if (index in updatedSegments.indices) {
            updatedSegments[index] = segment.copy(
                isExempt = isCountryExempt(segment.country)
            )

            setState { it.copy(segments = updatedSegments) }
            recalculateDays()
        }
    }

    private fun removeSegment(index: Int) {
        val updatedSegments = currentState.segments.toMutableList()
        if (index in updatedSegments.indices) {
            updatedSegments.removeAt(index)
            setState { it.copy(segments = updatedSegments) }
            recalculateDays()
        }
    }

    private fun recalculateSegmentsExemption() {
        val updatedSegments = currentState.segments.map { segment ->
            segment.copy(isExempt = isCountryExempt(segment.country))
        }

        setState { it.copy(segments = updatedSegments) }
    }

    private fun recalculateDays() {
        launch {
            try {
                val startCalculation = calculateDaysInPeriodUseCase(
                    periodEnd = currentState.startDate,
                    exemptCountries = currentState.exemptCountries
                )

                val startDaysInfo = AddTripContract.DaysAvailableInfo(
                    used = startCalculation.totalDaysUsed,
                    total = 90,
                    remaining = startCalculation.remainingDays,
                    isNearLimit = startCalculation.isNearLimit,
                    isOverLimit = startCalculation.isOverLimit
                )

                val countableDays = if(currentState.segments.isNotEmpty()) {
                    currentState.segments.filter { !it.isExempt }.sumOf { it.duration }.toInt()
                } else {
                    currentState.totalDuration.toInt() // TODO
                }
                val endUsed = startCalculation.totalDaysUsed + countableDays
                val endRemaining = (90 - endUsed).coerceAtLeast(0)

                val endDaysInfo = AddTripContract.DaysAvailableInfo(
                    used = endUsed,
                    total = 90,
                    remaining = endRemaining,
                    isNearLimit = endUsed >= 75,
                    isOverLimit = endUsed > 90
                )

                setState {
                    it.copy(
                        daysAvailableAtStart = startDaysInfo,
                        daysAvailableAtEnd = endDaysInfo
                    )
                }

            } catch (e: Exception) {
                // Ошибка расчета не критична
            }
        }
    }

    private fun saveTrip() {
        val validationErrors = validateForm()

        if (validationErrors.isEmpty()) {
            setState { it.copy(isLoading = true) }

            launch {
                try {
                    val trip = Trip(
                        id = 0,
                        visaId = currentState.selectedVisa?.id,
                        startDate = currentState.startDate,
                        endDate = currentState.endDate,
                        segments = currentState.segments.map { segmentUi ->
                            TripSegment(
                                country = segmentUi.country,
                                startDate = segmentUi.startDate,
                                endDate = segmentUi.endDate,
                                type = segmentUi.type,
                                cities = segmentUi.cities
                            )
                        },
                        purpose = currentState.purpose,
                        isPlanned = currentState.startDate.isAfter(LocalDate.now()),
                        notes = currentState.notes.takeIf { it.isNotBlank() }
                    )

                    saveTripUseCase(trip)
                    setEffect { Effect.NavigateBack }

                } catch (e: Exception) {
                    setState {
                        it.copy(
                            isLoading = false,
                            error = CustomString.resource(uiR.string.error_saving_trip)
                        )
                    }
                }
            }
        } else {
            setState { it.copy(validationErrors = validationErrors) }
            setEffect { Effect.ScrollUp }
        }
    }

    private fun validateForm(): AddTripContract.ValidationErrors {
        val startDate = currentState.startDate
        val endDate = currentState.endDate
        val selectedVisa = currentState.selectedVisa
        val segments = currentState.segments
        val daysAtEnd = currentState.daysAvailableAtEnd

        return AddTripContract.ValidationErrors(
            endDateError = if (endDate.isBefore(startDate))
                CustomString.resource(uiR.string.error_end_date_before_start) else null,
            visaError = if (selectedVisa == null)
                CustomString.resource(uiR.string.error_visa_required) else null,
            segmentsError = if (segments.isEmpty())
                CustomString.resource(uiR.string.error_no_segments) else null,
            daysLimitError = if (daysAtEnd?.isOverLimit == true)
                CustomString.resource(uiR.string.error_days_limit_exceeded) else null
        )
    }

    private fun validateDates(
        startDate: LocalDate,
        endDate: LocalDate
    ): AddTripContract.ValidationErrors {
        return currentState.validationErrors.copy(
            endDateError = if (endDate.isBefore(startDate))
                CustomString.resource(uiR.string.error_end_date_before_start) else null
        )
    }

    private fun isCountryExempt(country: String): Boolean {
        val visa = currentState.selectedVisa ?: return false

        // Страна считается exempt если:
        // 1. Виза типа D или ВНЖ
        // 2. Страна совпадает со страной выдавшей визу
        return (visa.visaType == VisaCategory.TYPE_D || visa.visaType == VisaCategory.RESIDENCE_PERMIT) &&
                visa.country == country
    }

    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error) }
    }

    private fun dismissError() {
        setState { it.copy(error = null) }
    }
}