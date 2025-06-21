package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import ru.nikfirs.android.traveltracker.core.data.model.TRANSIT
import ru.nikfirs.android.traveltracker.core.domain.MAX_STAY_DAYS
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.ui.model.BlockDateModel
import ru.nikfirs.android.traveltracker.core.ui.model.BlockDateType
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.core.ui.mvi.launchIO
import ru.nikfirs.android.traveltracker.feature.home.domain.model.TripSegmentUi
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.CalculateDaysInPeriodUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.GetAllTripsUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.GetTripsByDatesUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.SaveTripUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.GetVisasByDateUseCase
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.*
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.utils.AddTripHolder
import java.time.LocalDate
import javax.inject.Inject
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@HiltViewModel
class AddTripViewModel @Inject constructor(
    private val getVisasByDateUseCase: GetVisasByDateUseCase,
    private val calculateDaysInPeriodUseCase: CalculateDaysInPeriodUseCase,
    private val getAllTripsUseCase: GetAllTripsUseCase,
    private val saveTripUseCase: SaveTripUseCase,
    private val getTripsByDatesUseCase: GetTripsByDatesUseCase,
    val addTripHolder: AddTripHolder,
) : ViewModel<Action, Effect, State>() {

    private var daysOutOfSegments: Set<LocalDate> = emptySet()

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

            is Action.CalculateBlockDaysByStartDate -> calculateTotalBlockDates(action.startDate)
            is Action.UpdateDates -> updateDates(action.startDate, action.endDate)
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
        }
    }

    private fun loadData() {
        launch {
            setState { it.copy(isLoading = true) }

            try {
                val visas = getVisasByDateUseCase(
                    LocalDate.now().minusMonths(6)
                ).first()
                setState {
                    it.copy(
                        isLoading = false,
                        availableVisas = visas,
                    )
                }

                calculateInitialDays()
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
                )

                val daysInfo = DaysAvailableInfo(
                    used = calculation.totalDaysUsed,
                    remaining = calculation.remainingDays,
                )

                val daysInfoEnd = DaysAvailableInfo(
                    used = calculation.totalDaysUsed + 1,
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
                validationErrors = ValidationErrors(),
                startDate = null,
                endDate = null,
                segments = emptyList(),
            )
        }

        // update and available days
        recalculateAvailableDays()

        // calculate block dates when other trips exist
        visa?.let {
            setState { it.copy(blockedDates = calculateBlockedTripDates(visa)) }
        }
    }

    private fun updateDates(startDate: LocalDate, endDate: LocalDate) {
        setState {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                showDatePicker = false,
                validationErrors = currentState.validationErrors.copy(daysLimitError = null)
            )
        }

        recalculateAvailableDays()
    }


    private fun showDatePicker(value: Boolean) {
        setState { it.copy(showDatePicker = value) }
        if (!value && currentState.startDate == null) {
            setState {
                it.copy(
                    blockedDates = currentState.blockedDates
                        .filter { date -> date.type is BlockDateType.Trip }
                        .toSet()
                )
            }
        }
    }

    private fun setPurposeDropdownExpanded(expanded: Boolean) {
        setState { it.copy(isPurposeDropdownExpanded = expanded) }
    }

    private fun updatePurpose(purpose: TripPurpose) {
        setState { it.copy(purpose = purpose, isPurposeDropdownExpanded = false) }
    }

    private fun updateSegmentList() {
        setState {
            it.copy(
                validationErrors = currentState.validationErrors.copy(segmentsError = null),
                segments = addTripHolder.segmentList
            )
        }
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
        setState {
            it.copy(
                validationErrors = currentState.validationErrors.copy(segmentsError = null)
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

    private fun recalculateAvailableDays() {
        if (currentState.startDate == null || currentState.endDate == null) {
            calculateInitialDays()
            return
        }
        launchIO {
            try {
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

                // start date data
                val startCalculation = calculateDaysInPeriodUseCase(
                    periodEnd = currentState.startDate ?: LocalDate.now(),
                )
                val startDaysInfo = DaysAvailableInfo(
                    used = startCalculation.totalDaysUsed,
                    remaining = startCalculation.remainingDays,
                )

                // end date data
                val endCalculation = calculateDaysInPeriodUseCase(
                    periodEnd = currentState.endDate ?: LocalDate.now(),
                )
                val endDaysInfo = DaysAvailableInfo(
                    used = endCalculation.totalDaysUsed + countableDays,
                    remaining = endCalculation.remainingDays - countableDays,
                )

                setState {
                    it.copy(
                        daysAvailableAtStart = startDaysInfo,
                        daysAvailableAtEnd = endDaysInfo,
                        countableDuration = countableDays,
                    )
                }

            } catch (e: Exception) {
                calculateInitialDays()
                setError(CustomString.internal())
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
                                cities = segmentUi.cities,
                                isExempt = segmentUi.isExempt
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

    private fun validateForm(): ValidationErrors {
        val selectedVisa = currentState.selectedVisa
        val segments = currentState.segments
        val daysAtEnd = currentState.daysAvailableAtEnd

        return ValidationErrors(
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
                    warningTextDaysOutSegments =
                    CustomString.resource(uiR.string.error_segment_gap)
                )
            }
        }

        return daysOutOfSegments.isEmpty()
    }

    private fun saveTripWithTransit() {
        launch {
            setState { it.copy(isLoading = true) }

            try {
                val segments: MutableList<TripSegment> =
                    currentState.segments.map { segmentUi ->
                        TripSegment(
                            country = segmentUi.country,
                            startDate = segmentUi.startDate,
                            endDate = segmentUi.endDate,
                            cities = segmentUi.cities,
                            isExempt = segmentUi.isExempt,
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
                        cities = emptyList(),
                        isExempt = true,
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

    private fun calculateBlockedTripDates(visa: Visa): Set<BlockDateModel> {
        var blockTripDates: Set<BlockDateModel> = emptySet()
        launchIO {
            try {
                val trips = getTripsByDatesUseCase(visa.startDate, visa.expiryDate)
                blockTripDates = trips
                    .filter { it.startDate != null && it.endDate != null }
                    .map { trip ->
                        BlockDateModel(
                            startDate = trip.startDate ?: visa.startDate.minusDays(-1),
                            endDate = trip.startDate ?: visa.startDate.minusDays(-1),
                            type = BlockDateType.Trip(tripId = trip.id)
                        )
                    }.toSet()
            } catch (e: Exception) { }
        }
        return blockTripDates
    }

    private fun calculateTotalBlockDates(startDate: LocalDate?): Set<BlockDateModel> {
        val blockDates: MutableSet<BlockDateModel> = mutableSetOf()

        val blockTripDates =
            currentState.blockedDates.filter { it.type is BlockDateType.Trip }.toSet()
        val blockDayLimitDates = calculateBlockDayLimitDates(startDate)

        blockDates.addAll(blockTripDates)
        blockDates.addAll(blockDayLimitDates)

        return blockTripDates
    }

    private fun calculateBlockDayLimitDates(startDate: LocalDate?): Set<BlockDateModel> {
        val blockDayLimitDates: MutableSet<BlockDateModel> = mutableSetOf()
        if (startDate == null) return blockDayLimitDates
        val visa = currentState.selectedVisa ?: return blockDayLimitDates
        launchIO {
            val firstBlockDate = minOf(
                visa.expiryDate,
                currentState.blockedDates
                    .filter { it.startDate.isAfter(startDate) }
                    .minOf { it.startDate }
            )

            var checkDate = visa.startDate
            var currentBlockStart: LocalDate? = null
            var previousBlockedDate: LocalDate? = null

            while (!checkDate.isAfter(firstBlockDate)) {
                try {
                    val calculation = calculateDaysInPeriodUseCase(periodEnd = checkDate)

                    if (calculation.totalDaysUsed >= MAX_STAY_DAYS) {
                        if (currentBlockStart == null) {
                            currentBlockStart = checkDate
                        }
                        previousBlockedDate = checkDate
                    } else {
                        // block exists and days over limit ended
                        if (currentBlockStart != null && previousBlockedDate != null) {
                            blockDayLimitDates.add(
                                BlockDateModel(
                                    startDate = currentBlockStart,
                                    endDate = previousBlockedDate,
                                    type = BlockDateType.DayLimit
                                )
                            )
                            currentBlockStart = null
                            previousBlockedDate = null
                        }
                    }
                } catch (e: Exception) { }
                checkDate = checkDate.plusDays(1)
            }


            if (currentBlockStart != null && previousBlockedDate != null) {
                blockDayLimitDates.add(
                    BlockDateModel(
                        startDate = currentBlockStart,
                        endDate = previousBlockedDate,
                        type = BlockDateType.DayLimit
                    )
                )
            }
        }

        return blockDayLimitDates
    }
}