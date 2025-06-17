package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.feature.home.domain.model.TripSegmentUi
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.CalculateDaysInPeriodUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.GetExemptCountriesUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.GetAllTripsUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.SaveTripUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.GetActiveVisasUseCase
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.utils.AddTripHolder
import java.time.LocalDate
import javax.inject.Inject
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@HiltViewModel
class AddTripViewModel @Inject constructor(
    private val getActiveVisasUseCase: GetActiveVisasUseCase,
    private val getExemptCountriesUseCase: GetExemptCountriesUseCase,
    private val calculateDaysInPeriodUseCase: CalculateDaysInPeriodUseCase,
    private val getAllTripsUseCase: GetAllTripsUseCase,
    private val saveTripUseCase: SaveTripUseCase,
    private val addTripHolder: AddTripHolder,
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
            is Action.OpenAddSegmentEditor -> openSegmentEditor()
            is Action.OpenEditSegmentEditor -> openSegmentEditor(action.index)
            is Action.OnSegmentEditorResult -> onSegmentEditorResult(action)
            is Action.OnSegmentDeleted -> removeSegment(action.segmentIndex)
            is Action.SaveTrip -> saveTrip()
            is Action.SetError -> setError(action.error)
            is Action.DismissError -> dismissError()
            is Action.RecalculateDays -> recalculateDays()
            is Action.CheckSegmentResults -> checkSegmentResults()
        }
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

    private fun addSegment(segment: TripSegmentUi) {
        val segmentWithColor = segment.copy(
            isExempt = isCountryExempt(segment.country),
            color = AddTripHolder.getSegmentColor(currentState.segments.size)
        )

        val updatedSegments = currentState.segments + segmentWithColor

        setState {
            it.copy(
                segments = updatedSegments,
                validationErrors = currentState.validationErrors.copy(segmentsError = null)
            )
        }

        recalculateDays()
    }

    private fun updateSegment(index: Int, segment: TripSegmentUi) {
        val updatedSegments = currentState.segments.toMutableList()
        if (index in updatedSegments.indices) {
            val segmentWithUpdatedData = segment.copy(
                isExempt = isCountryExempt(segment.country),
                color = updatedSegments[index].color // Сохраняем существующий цвет
            )
            updatedSegments[index] = segmentWithUpdatedData

            setState { it.copy(segments = updatedSegments) }
            recalculateDays()
        }
    }

    private fun removeSegment(index: Int) {
        val updatedSegments = currentState.segments.toMutableList()
        if (index in updatedSegments.indices) {
            updatedSegments.removeAt(index)

            // Пересчитываем цвета для оставшихся сегментов
            val recoloredSegments = updatedSegments.mapIndexed { newIndex, segment ->
                segment.copy(color = AddTripHolder.getSegmentColor(newIndex))
            }

            setState { it.copy(segments = recoloredSegments) }
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

                val countableDays = if (currentState.segments.isNotEmpty()) {
                    currentState.segments.filter { !it.isExempt }.sumOf { it.duration }.toInt()
                } else {
                    currentState.totalDuration.toInt()
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

    private fun isCountryExempt(country: String): Boolean {
        val visa = currentState.selectedVisa ?: return false

        // Страна считается exempt если:
        // 1. Виза типа D или ВНЖ
        // 2. Страна совпадает со страной выдавшей визу
        return (visa.visaType == VisaCategory.TYPE_D || visa.visaType == VisaCategory.RESIDENCE_PERMIT) &&
                visa.country == country
    }
    private fun openSegmentEditor(segmentIndex: Int? = null) {
        // Создаем TripSegmentDisplay для передачи в редактор сегментов
        val existingSegments = currentState.segmentsForDisplay.let { segments ->
            if (segmentIndex != null) {
                // В режиме редактирования исключаем редактируемый сегмент
                segments.filterIndexed { index, _ -> index != segmentIndex }
            } else {
                segments
            }
        }

//        if (segmentIndex != null) {
//            // Режим редактирования
//            val segment = currentState.segments.getOrNull(segmentIndex)
//            if (segment != null) {
//                addTripHolder.prepareForEditSegment(
//                    tripStartDate = currentState.startDate,
//                    tripEndDate = currentState.endDate,
//                    existingSegments = existingSegments,
//                    segmentIndex = segmentIndex,
////                    existingCountry = segment.country,
////                    existingStartDate = segment.startDate,
////                    existingEndDate = segment.endDate,
////                    existingCities = segment.cities.joinToString(", ")
//                )
//            } else {
//                setError(CustomString.resource(uiR.string.error_segment_not_found))
//                return
//            }
//        } else {
//            // Режим добавления
//            addTripHolder.prepareForAddSegment(
//                tripStartDate = currentState.startDate,
//                tripEndDate = currentState.endDate,
//                existingSegments = existingSegments
//            )
//        }

        setEffect { Effect.OpenSegmentEditor }
    }

    private fun onSegmentEditorResult(action: Action.OnSegmentEditorResult) {
        val newSegment = TripSegmentUi(
            country = action.country,
            startDate = action.startDate,
            endDate = action.endDate,
            cities = action.cities,
            isExempt = isCountryExempt(action.country),
            color = if (action.isUpdate && action.segmentIndex != null) {
                // При обновлении сохраняем существующий цвет
                currentState.segments.getOrNull(action.segmentIndex)?.color
                    ?: AddTripHolder.getSegmentColor(action.segmentIndex)
            } else {
                // При добавлении назначаем новый цвет
                AddTripHolder.getSegmentColor(currentState.segments.size)
            }
        )

        if (action.isUpdate && action.segmentIndex != null) {
            updateSegment(action.segmentIndex, newSegment)
        } else {
            addSegment(newSegment)
        }

        // Автоматически пересчитываем дни после изменения сегментов
        recalculateDays()
    }

    private fun checkSegmentResults() {
        // Проверяем результат работы с сегментом
//        addTripHolder.consumeSegmentResult()?.let { result ->
//            onSegmentEditorResult(
//                Action.OnSegmentEditorResult(
//                    country = result.country,
//                    startDate = result.startDate,
//                    endDate = result.endDate,
//                    cities = result.cities,
//                    isUpdate = result.isUpdate,
//                    segmentIndex = result.segmentIndex
//                )
//            )
//        }

        // Проверяем удаленный сегмент
        addTripHolder.consumeDeletedSegmentIndex()?.let { index ->
            removeSegment(index)
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

    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error) }
    }

    private fun dismissError() {
        setState { it.copy(error = null) }
    }

    // Заглушки для методов работы с блокированными датами (пока оставляем старую логику)
    private fun calculateBlockedDates(trips: List<Trip>) {
        // TODO: Реализовать при необходимости
    }

    private fun recalculateBlockedDatesForExistingTrips() {
        // TODO: Реализовать при необходимости
    }
}