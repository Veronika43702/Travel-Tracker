package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addOrEditTrip

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.nikfirs.android.traveltracker.core.data.model.TRANSIT
import ru.nikfirs.android.traveltracker.core.domain.MAX_STAY_DAYS
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.CalculateDaysInPeriodUseCase
import ru.nikfirs.android.traveltracker.core.ui.ui.model.BlockDateModel
import ru.nikfirs.android.traveltracker.core.ui.ui.model.DateType
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.core.ui.mvi.launchIO
import ru.nikfirs.android.traveltracker.feature.home.R
import ru.nikfirs.android.traveltracker.feature.home.ui.model.TripSegmentUi
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.trip.GetTripByIdUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.GetTripsByDatesUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.SaveTripUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.UpdateTripUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.GetAvailableVisasByDateUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.visa.GetVisaByIdUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.GetVisaDurationUsedUseCase
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addOrEditTrip.AddOrEditTripContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addOrEditTrip.AddOrEditTripContract.DaysAvailableInfo
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addOrEditTrip.AddOrEditTripContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addOrEditTrip.AddOrEditTripContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addOrEditTrip.AddOrEditTripContract.ValidationErrors
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.utils.AddTripHolder
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.utils.getTripSegmentColorByIndex
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@HiltViewModel
class AddOrEditTripViewModel @Inject constructor(
    private val getAvailableVisasByDateUseCase: GetAvailableVisasByDateUseCase,
    private val calculateDaysInPeriodUseCase: CalculateDaysInPeriodUseCase,
    private val saveTripUseCase: SaveTripUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val getTripsByDatesUseCase: GetTripsByDatesUseCase,
    private val getTripByIdUseCase: GetTripByIdUseCase,
    private val getVisaByIdUseCase: GetVisaByIdUseCase,
    private val getVisaDurationUsedUseCase: GetVisaDurationUsedUseCase,
    val addTripHolder: AddTripHolder,
) : ViewModel<Action, Effect, State>() {

    private var daysOutOfSegments: Set<LocalDate> = emptySet()

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.LoadData -> loadData(action.tripId)
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

            is Action.SaveTrip -> saveOrUpdateTrip()
            is Action.SaveTripWithTransit -> saveTripWithTransit()

            is Action.SetError -> setError(action.error)
            is Action.SetWarning -> setWarning(action.value)
        }
    }

    private fun loadData(tripId: Long?) {
        launchIO {
            setState { it.copy(isLoading = true) }

            try {
                val visas = getAvailableVisasByDateUseCase.invoke(
                    LocalDate.now().minusMonths(6)
                )
                setState { it.copy(availableVisas = visas) }

                if (!currentState.hasSelectedVisa) {
                    tripId?.let {
                        loadTripData(tripId)
                        setDataForHolder()
                        recalculateAvailableDays()
                        currentState.selectedVisa?.let { visa ->
                            val blockedTripPeriods = calculateBlockedTripDates(visa, tripId)
                            setState { it.copy(blockedPeriods = blockedTripPeriods) }
                        }
                    }
                }
                setState { it.copy(isLoading = false, tripId = tripId) }
            } catch (e: Exception) {
                setError(CustomString.resource(uiR.string.error_loading_data))
                Log.e(null, "loadData", e)
            }
        }
    }

    private suspend fun loadTripData(tripId: Long?) {
        try {
            val trip = tripId?.let { getTripByIdUseCase.invoke(it) }
            if (trip == null) {
                setError(CustomString.resource(R.string.home_error_trip_not_found))
                return
            }

            val visa = trip.visaId?.let { getVisaByIdUseCase.invoke(it) }

            val segments =
                trip.segments.mapIndexed { index, segment ->
                    TripSegmentUi(
                        country = segment.country,
                        startDate = segment.startDate,
                        endDate = segment.endDate,
                        isExempt = segment.isExempt,
                        cities = segment.cities,
                        color = getTripSegmentColorByIndex(index)
                    )
                }
            setState {
                it.copy(
                    selectedVisa = visa,
                    startDate = trip.startDate,
                    endDate = trip.endDate,
                    countableDuration = trip.countableDays,
                    purpose = trip.purpose,
                    segments = segments,
                    notes = trip.notes ?: "",
                )
            }
        } catch (e: Exception) {
            setError(CustomString.resource(uiR.string.error_loading_data))
            Log.e(null, "recalculateAvailableDays", e)
        }
    }

    private fun setDataForHolder() {
        addTripHolder.prepareHolderForTripEdit(
            tripStartDate = currentState.startDate,
            tripEndDate = currentState.endDate,
            existingSegments = currentState.segments,
            exemptCountry = currentState.exemptVisaCountry,
        )
    }

    private fun calculateInitialDays() {
        launch {
            try {
                val today = LocalDate.now()
                val calculation = calculateDaysInPeriodUseCase(
                    periodEnd = today,
                    tripExceptionId = currentState.tripId,
                )

                val daysInfo = DaysAvailableInfo(
                    used = calculation.totalDaysUsed,
                    remaining = calculation.remainingDays,
                )

                setState {
                    it.copy(
                        daysAvailableAtStart = daysInfo,
                        daysAvailableAtEnd = daysInfo
                    )
                }
            } catch (e: Exception) {
                val defaultDaysInfo = DaysAvailableInfo(
                    used = 0,
                    remaining = MAX_STAY_DAYS
                )
                setState {
                    it.copy(
                        daysAvailableAtStart = defaultDaysInfo,
                        daysAvailableAtEnd = defaultDaysInfo,
                    )
                }
            }
        }
    }

    private fun setVisaDropdownExpanded(expanded: Boolean) {
        setState { it.copy(isVisaDropdownExpanded = expanded) }
    }

    private fun updateSelectedVisa(visa: Visa?) {
        launchIO {
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

            // update available days
            recalculateAvailableDays()

            // calculate block dates when other trips exist
            visa?.let {
                val blockedTripPeriods = calculateBlockedTripDates(visa)
                setState { it.copy(blockedPeriods = blockedTripPeriods) }
            }
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

        // update available days
        recalculateAvailableDays()
    }


    private fun showDatePicker(value: Boolean) {
        setState { it.copy(showDatePicker = value) }
        if (!value && currentState.startDate == null) {
            setState {
                it.copy(
                    blockedPeriods = currentState.blockedPeriods
                        .filter { date -> date.type is DateType.Trip }
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

        // update available days
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

        // update available days
        recalculateAvailableDays()
    }

    private fun updateNotes(notes: String) {
        setState { it.copy(notes = notes) }
    }

    private fun recalculateAvailableDays() {
        if (currentState.startDate == null || currentState.endDate == null) {
            return
        }
        launchIO {
            try {
                val countableDays =
                    if (currentState.segments.isNotEmpty() && checkDaysOutOfSegments(false)) {
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
                    tripExceptionId = currentState.tripId,
                )
                val startDaysInfo = DaysAvailableInfo(
                    used = startCalculation.totalDaysUsed,
                    remaining = startCalculation.remainingDays,
                )

                // end date data
                val endCalculation = calculateDaysInPeriodUseCase(
                    periodEnd = currentState.endDate ?: LocalDate.now(),
                    tripExceptionId = currentState.tripId,
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
                Log.e(null, "recalculateAvailableDays", e)
            }
        }
    }

    private fun saveOrUpdateTrip() {
        val id = currentState.tripId
        val validationErrors = validateForm()

        if (validationErrors.isEmpty()) {
            if (!checkDaysOutOfSegments()) return

            setState { it.copy(isLoading = true) }
            launchIO {
                try {
                    val trip = Trip(
                        id = id ?: 0,
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
                    if (id == null) {
                        saveTripUseCase.invoke(trip)
                    } else {
                        updateTripUseCase.invoke(trip)
                    }
                    setEffect { Effect.NavigateBack }

                } catch (e: Exception) {
                    setState {
                        it.copy(
                            isLoading = false,
                            error = CustomString.resource(R.string.home_error_trip_saving)
                        )
                    }
                    Log.e(null, "saveOrUpdateTrip", e)
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
                CustomString.resource(R.string.home_error_trip_visa_required) else null,
            segmentsError = if (segments.isEmpty())
                CustomString.resource(R.string.home_error_trip_no_segments) else null,
            daysLimitError = if (daysAtEnd?.isOverLimit == true)
                CustomString.resource(R.string.home_error_trip_days_limit_exceeded) else null
        )
    }

    private fun checkDaysOutOfSegments(showWarning: Boolean = true): Boolean {
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
        if (daysOutOfSegments.isNotEmpty() && showWarning) {
            setState {
                it.copy(
                    warningTextDaysOutSegments =
                    CustomString.resource(R.string.home_error_trip_segment_gap)
                )
            }
        }

        val extraDays = setOfSegmentDays - setOfTripDays
        if (extraDays.isNotEmpty()) {
            setState { it.copy(segments = emptyList()) }
        }

        return daysOutOfSegments.isEmpty() && (extraDays.isEmpty())
    }

    private fun saveTripWithTransit() {
        val id = currentState.tripId
        launchIO {
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
                        isExempt = false,
                    )
                }
                segments.addAll(transitSegments)

                val trip = Trip(
                    id = id ?: 0,
                    visaId = currentState.selectedVisa?.id,
                    startDate = currentState.startDate,
                    endDate = currentState.endDate,
                    segments = segments.sortedWith(compareBy({ it.startDate }, { it.endDate })),
                    purpose = currentState.purpose,
                    notes = currentState.notes.takeIf { it.isNotBlank() }
                )

                if (id == null) {
                    saveTripUseCase.invoke(trip)
                } else {
                    updateTripUseCase.invoke(trip)
                }
                daysOutOfSegments = emptySet()
                setEffect { Effect.NavigateBack }

            } catch (e: Exception) {
                daysOutOfSegments = emptySet()
                setState {
                    it.copy(
                        isLoading = false,
                        error = CustomString.resource(R.string.home_error_trip_saving)
                    )
                }
                Log.e(null, "saveTripWithTransit", e)
            }
        }
    }

    private fun setError(error: CustomString? = null) {
        setState { it.copy(isLoading = false, error = error) }
    }

    private fun setWarning(value: CustomString? = null) {
        setState { it.copy(warningTextDaysOutSegments = value) }
    }

    private suspend fun calculateBlockedTripDates(
        visa: Visa,
        tripId: Long? = null
    ): Set<BlockDateModel> {
        val blockTripDates: MutableSet<BlockDateModel> = mutableSetOf()

        try {
            val trips = getTripsByDatesUseCase(visa.startDate, visa.expiryDate)
            trips
                .filter {
                    it.startDate != null && it.endDate != null && it.id != tripId
                }
                .forEach { trip ->
                    blockTripDates.add(
                        BlockDateModel(
                            startDate = trip.startDate ?: visa.startDate.minusDays(-1),
                            endDate = trip.endDate ?: visa.startDate.minusDays(-1),
                            type = DateType.Trip(tripId = trip.id)
                        )
                    )
                }
        } catch (e: Exception) {
            setError(CustomString.internal())
            Log.e(null, "calculateBlockedTripDates", e)
        }

        return blockTripDates
    }

    private fun calculateTotalBlockDates(startDate: LocalDate?) {
        launchIO {
            val blockDates: MutableSet<BlockDateModel> = mutableSetOf()

            val blockTripDates =
                currentState.blockedPeriods.filter { it.type is DateType.Trip }.toSet()
            val blockDayLimitDates = calculateBlockDayLimitDates(startDate)
            val blockDaysVisaDuration = calculateBlockDayByVIsaDuration(startDate)

            blockDates.addAll(blockTripDates)
            blockDates.addAll(blockDayLimitDates)
            blockDates.addAll(blockDaysVisaDuration)

            setState { it.copy(blockedPeriods = blockDates) }
        }
    }

    private suspend fun calculateBlockDayLimitDates(startDate: LocalDate?): Set<BlockDateModel> {
        val blockDayLimitDates: MutableSet<BlockDateModel> = mutableSetOf()
        val visa = currentState.selectedVisa
        if (startDate == null || visa == null) return blockDayLimitDates

        val dayOfFirstTripAfterStartDate = currentState.blockedPeriods
            .filter { it.type is DateType.Trip }
            .sortedBy { it.startDate }
            .firstOrNull { it.startDate.isAfter(startDate) }?.startDate ?: visa.expiryDate.plusDays(
            1
        )

        var checkDate: LocalDate = startDate
        while (!checkDate.isAfter(dayOfFirstTripAfterStartDate)) {
            try {
                val calculation =
                    calculateDaysInPeriodUseCase(periodEnd = checkDate, currentState.tripId)

                val tripDuration = ChronoUnit.DAYS.between(startDate, checkDate) + 1
                if (calculation.totalDaysUsed + tripDuration > MAX_STAY_DAYS) {
                    break
                }
            } catch (e: Exception) {
                setError(CustomString.internal())
                Log.e(null, "calculateBlockDayLimitDates", e)
            }
            checkDate = checkDate.plusDays(1)
        }

        val firstLimitDay = minOf(checkDate, dayOfFirstTripAfterStartDate)
        blockDayLimitDates.add(
            BlockDateModel(
                startDate = firstLimitDay,
                endDate = visa.expiryDate,
                type = DateType.Other,
            )
        )

        return blockDayLimitDates
    }

    private suspend fun calculateBlockDayByVIsaDuration(startDate: LocalDate?): Set<BlockDateModel> {
        val blockDayLimitDates: MutableSet<BlockDateModel> = mutableSetOf()
        val visa = currentState.selectedVisa
        if (startDate == null
            || visa == null
            || visa.visaType != VisaCategory.TYPE_C
        ) return blockDayLimitDates

        val usedDaysByTripsWithVisa =  getVisaDurationUsedUseCase.invoke(visa.id)
        val visaRemainingDuration = visa.durationOfStay - usedDaysByTripsWithVisa
        val firstBlockDateOutDuration = startDate.plusDays(visaRemainingDuration.toLong())

        blockDayLimitDates.add(
            BlockDateModel(
                startDate = firstBlockDateOutDuration,
                endDate = visa.expiryDate,
                type = DateType.Other,
            )
        )

        return blockDayLimitDates
    }
}