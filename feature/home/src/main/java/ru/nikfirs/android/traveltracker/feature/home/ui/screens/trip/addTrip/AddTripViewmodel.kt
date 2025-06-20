package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import ru.nikfirs.android.traveltracker.core.data.model.TRANSIT
import ru.nikfirs.android.traveltracker.core.domain.MAX_STAY_DAYS
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
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
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.DaysAvailableInfo
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
    val addTripHolder: AddTripHolder,
) : ViewModel<Action, Effect, State>() {

    var daysOutOfSegments: Set<LocalDate> = emptySet()

    init {
        setAction(Action.LoadData)
    }

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.LoadData -> loadData()
            Action.UpdateSegmentList -> updateSegmentList()

            is Action.SetVisaDropdownExpanded -> setVisaDropdownExpanded(action.expanded)
            is Action.UpdateSelectedVisa -> updateSelectedVisa(action.visa)
            is Action.UpdateDates -> updateStartDate(action.startDate, action.endDate)
            is Action.ShowDatePicker -> showDatePicker(action.value)
            is Action.SetPurposeDropdownExpanded -> setPurposeDropdownExpanded(action.expanded)
            is Action.UpdatePurpose -> updatePurpose(action.purpose)

            is Action.UpdateNotes -> updateNotes(action.notes)

            is Action.DeleteSegment -> removeSegment(action.segment)
            is Action.OpenAddSegmentEditor -> openSegmentEditor()
            is Action.OpenEditSegmentEditor -> openSegmentEditor(action.segment)

            is Action.SaveTrip -> saveTrip()
            is Action.SaveTripWithTransit -> saveTripWithTransit()
            is Action.SetError -> setError(action.error)
            is Action.SetWarning -> setWarning(action.value)
            is Action.RecalculateDays -> recalculateAvailableDays()
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
                    calculateBlockedDates(trips) // TODO block days for datePicker
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

                val daysInfo = DaysAvailableInfo(
                    used = calculation.totalDaysUsed,
                    remaining = calculation.remainingDays,
                )

                val daysInfoEnd = DaysAvailableInfo(
                    used = 1,
                    remaining = calculation.remainingDays - 1,
                )

                setState {
                    it.copy(
                        daysAvailableAtStart = daysInfo,
                        daysAvailableAtEnd = daysInfoEnd
                    )
                }
            } catch (e: Exception) {
                val defaultDaysInfo = DaysAvailableInfo(
                    used = 0,
                    remaining = MAX_STAY_DAYS
                )
                val defaultDaysInfoEnd = DaysAvailableInfo(
                    used = 1,
                    remaining = MAX_STAY_DAYS - 1
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

    private fun setVisaDropdownExpanded(expanded: Boolean) {
        setState { it.copy(isVisaDropdownExpanded = expanded) }
    }

    private fun updateSelectedVisa(visa: Visa?) {
        setState {
            it.copy(
                selectedVisa = visa,
                isVisaDropdownExpanded = false,
                validationErrors = currentState.validationErrors.copy(visaError = null),
                startDate = null,
                endDate = null,
                segments = emptyList(),
            )
        }

        // update segment list and available days
        recalculateSegmentsExemption()
        recalculateAvailableDays()

        // Пересчитать заблокированные даты с учетом диапазона новой визы
        recalculateBlockedDatesForExistingTrips() // TODO
    }

    private fun updateStartDate(startDate: LocalDate, endDate: LocalDate) {
        setState {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                showDatePicker = false,
                // validationErrors = validateDates(startDate, endDate) // TODO check for other trip
            )
        }

        recalculateAvailableDays()
        recalculateBlockedDatesForExistingTrips()
    }


    private fun showDatePicker(value: Boolean) {
        setState { it.copy(showDatePicker = value) }
        recalculateBlockedDatesForExistingTrips()
    }

    private fun setPurposeDropdownExpanded(expanded: Boolean) {
        setState { it.copy(isPurposeDropdownExpanded = expanded) }
    }

    private fun updatePurpose(purpose: TripPurpose) {
        setState { it.copy(purpose = purpose, isPurposeDropdownExpanded = false) }
    }

    private fun updateSegmentList() {
        setState { it.copy(segments = addTripHolder.segmentList) }
        recalculateAvailableDays()
    }

    private fun openSegmentEditor(segment: TripSegmentUi? = null) {
        if (!(currentState.hasSelectedVisa && currentState.hasSelectedDates)) return

        if (segment != null) {
            // Edit existing segment
            addTripHolder.prepareForEditSegment(
                tripStartDate = currentState.startDate,
                tripEndDate = currentState.endDate,
                existingSegments = currentState.segments,
                exemptCountry = currentState.exemptVisaCountry,
                segment = segment,
                segmentIndex = currentState.segments.indexOf(segment),
            )
        } else {
            // Adding New Segment
            addTripHolder.prepareForAddSegment(
                tripStartDate = currentState.startDate,
                tripEndDate = currentState.endDate,
                existingSegments = currentState.segments,
                exemptCountry = currentState.exemptVisaCountry,
            )
        }

        setEffect { Effect.OpenSegmentEditor }
    }

    private fun removeSegment(segment: TripSegmentUi) {
        addTripHolder.deleteSegmentFromList(segment)
        setState { it.copy(segments = addTripHolder.segmentList) }
        recalculateAvailableDays()
    }

    private fun updateNotes(notes: String) {
        setState { it.copy(notes = notes) }
    }

    private fun recalculateSegmentsExemption() {
        val updatedSegments = currentState.segments.map { segment ->
            segment.copy(isExempt = isCountryExempt(segment.country))
        }

        setState { it.copy(segments = updatedSegments) }
    }

    private fun isCountryExempt(country: String): Boolean {
        val visa = currentState.selectedVisa ?: return false

        return (visa.visaType == VisaCategory.TYPE_D
                || visa.visaType == VisaCategory.RESIDENCE_PERMIT) &&
                visa.country == country
    }

    private fun recalculateAvailableDays() {
        launch {
            try {
                val startCalculation = calculateDaysInPeriodUseCase(
                    periodEnd = currentState.startDate ?: LocalDate.now(),
                    exemptCountries = currentState.exemptCountries
                )

                val startDaysInfo = DaysAvailableInfo(
                    used = startCalculation.totalDaysUsed,
                    remaining = startCalculation.remainingDays,
                )

                val countableDays = if (currentState.segments.isNotEmpty()) {
                    val countedDays: Set<LocalDate> = buildSet {
                        currentState.segments
                            .filter { !it.isExempt }
                            .forEach { segment ->
                                var date = segment.startDate
                                while (!date.isAfter(segment.endDate)) {
                                    add(date)
                                    date = date.plusDays(1)
                                }
                            }
                    }
                    countedDays.size
                } else {
                    currentState.totalDuration.toInt()
                }
                val endUsed = startCalculation.totalDaysUsed + countableDays
                val endRemaining = (90 - endUsed).coerceAtLeast(0)

                val endDaysInfo = DaysAvailableInfo(
                    used = endUsed,
                    remaining = endRemaining,
                )

                setState {
                    it.copy(
                        daysAvailableAtStart = startDaysInfo,
                        daysAvailableAtEnd = endDaysInfo,
                        countableDuration = countableDays,
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
            if (!checkDaysOutOfSegments()) return

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
        val selectedVisa = currentState.selectedVisa
        val segments = currentState.segments
        val daysAtEnd = currentState.daysAvailableAtEnd

        return AddTripContract.ValidationErrors(
            visaError = if (selectedVisa == null)
                CustomString.resource(uiR.string.error_visa_required) else null,
            segmentsError = if (segments.isEmpty())
                CustomString.resource(uiR.string.error_no_segments) else null,
            daysLimitError = if (daysAtEnd?.isOverLimit == true)
                CustomString.resource(uiR.string.error_days_limit_exceeded) else null
        )
    }

    private fun checkDaysOutOfSegments(): Boolean {
        val setOfTripDays: Set<LocalDate> = buildSet {
            var date = currentState.startDate ?: return false
            while (!date.isAfter(currentState.endDate)) {
                add(date)
                date = date.plusDays(1)
            }
        }

        val setOfSegmentDays: Set<LocalDate> = buildSet {
            currentState.segments
                .filter { !it.isExempt }
                .forEach { segment ->
                    var date = segment.startDate
                    while (!date.isAfter(segment.endDate)) {
                        add(date)
                        date = date.plusDays(1)
                    }
                }
        }

        daysOutOfSegments = setOfTripDays - setOfSegmentDays
        if (daysOutOfSegments.isNotEmpty()) {
            setState {
                it.copy(
                    warningTextDaysOutSegments = CustomString.resource(uiR.string.error_segment_gap)
                )
            }
        }

        return daysOutOfSegments.isEmpty()
    }

    private fun saveTripWithTransit() {
        launch {
            setState { it.copy(isLoading = true) }

            try {
                val segments: MutableList<TripSegment> = currentState.segments.map { segmentUi ->
                    TripSegment(
                        country = segmentUi.country,
                        startDate = segmentUi.startDate,
                        endDate = segmentUi.endDate,
                        cities = segmentUi.cities
                    )
                }.toMutableList()

                val groupedTransitSegments = daysOutOfSegments
                    .sorted()
                    .fold(mutableListOf<MutableList<LocalDate>>()) { acc, date ->
                        if (acc.isEmpty() || acc.last().last().plusDays(1) != date) {
                            // new group
                            acc.add(mutableListOf(date))
                        } else {
                            // continue current group
                            acc.last().add(date)
                        }
                        acc
                    }

                val transitSegments = groupedTransitSegments.map { group ->
                    TripSegment(
                        country = TRANSIT,
                        startDate = group.first(),
                        endDate = group.last(),
                        cities = emptyList()
                    )
                }

                segments.addAll(transitSegments)

                val trip = Trip(
                    id = 0,
                    visaId = currentState.selectedVisa?.id,
                    startDate = currentState.startDate,
                    endDate = currentState.endDate,
                    segments = segments,
                    purpose = currentState.purpose,
                    notes = currentState.notes.takeIf { it.isNotBlank() }
                )

                saveTripUseCase(trip)
                daysOutOfSegments = emptySet()
                setEffect { Effect.NavigateBack }

            } catch (e: Exception) {
                daysOutOfSegments = emptySet()
                setState {
                    it.copy(
                        isLoading = false,
                        error = CustomString.resource(uiR.string.error_saving_trip)
                    )
                }
            }
        }
    }

    private fun setError(error: CustomString? = null) {
        setState { it.copy(isLoading = false, error = error) }
    }

    private fun setWarning(value: CustomString? = null) {
        setState { it.copy(warningTextDaysOutSegments = value) }
    }

    private fun calculateBlockedDates(trips: List<Trip>) {
        launch {
            val blockedForStart = calculateBlockedDatesForStartDate(trips)
            val blockedForEnd = calculateBlockedDatesForEndDate(trips)

            setState {
                it.copy(
                    blockedDates = blockedForStart,
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
            while (date?.isAfter(trip.endDate) == false) {
                date?.let {
                    blockedDates.add(it)
                    date = it.plusDays(1)
                }
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

//        // Заблокировать даты существующих поездок (только будущие)
//        val today = LocalDate.now()
//        trips.filter { it.startDate?.isAfter(today) == true }.forEach { trip ->
//            var date = trip.startDate
//            while (date?.isAfter(trip.endDate) == false) {
//                date?.let {
//                    blockedDates.add(it)
//                    date = it.plusDays(1)
//                }
//            }
//        }
//
//        // Заблокировать даты меньше startDate
//        val startDate = currentState.startDate
//        var date = today.minusYears(1)
//        while (date.isBefore(startDate)) {
//            blockedDates.add(date)
//            date = date.plusDays(1)
//        }
//
//        // Заблокировать даты вне диапазона действия выбранной визы
//        val selectedVisa = currentState.selectedVisa
//        if (selectedVisa != null) {
//            // Блокируем даты после окончания действия визы
//            date = selectedVisa.expiryDate.plusDays(1)
//            val oneYearAhead = today.plusYears(1)
//            while (!date.isAfter(oneYearAhead)) {
//                blockedDates.add(date)
//                date = date.plusDays(1)
//            }
//        }
//
//        // Заблокировать даты, которые превышают правило 90 дней с учетом длительности поездки
//        val oneYearAhead = today.plusYears(1)
//
//        var checkDate = startDate
//        while (checkDate?.isAfter(oneYearAhead) == false) {
//            try {
//                // Симулируем поездку от startDate до checkDate
//                val potentialDuration =
//                    java.time.temporal.ChronoUnit.DAYS.between(startDate, checkDate) + 1
//
//                val calculation = calculateDaysInPeriodUseCase(
//                    periodEnd = checkDate,
//                    exemptCountries = currentState.exemptCountries
//                )
//
//                // Если добавление этой поездки превысит 90 дней, блокируем дату
//                if (calculation.totalDaysUsed + potentialDuration > 90) {
//                    blockedDates.add(checkDate)
//                }
//            } catch (e: Exception) {
//                // Пропускаем дату при ошибке расчета
//            }
//            checkDate = checkDate.plusDays(1)
//        }

        return blockedDates
    }
}