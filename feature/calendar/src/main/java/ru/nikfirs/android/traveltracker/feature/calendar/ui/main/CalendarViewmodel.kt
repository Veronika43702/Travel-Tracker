package ru.nikfirs.android.traveltracker.feature.calendar.ui.main

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import ru.nikfirs.android.traveltracker.core.domain.PERIOD_DAYS
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.ui.R as uiR
import ru.nikfirs.android.traveltracker.core.ui.ui.model.DateType
import ru.nikfirs.android.traveltracker.core.ui.ui.model.DayCalculation
import ru.nikfirs.android.traveltracker.core.ui.ui.model.ExistingRange
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.core.ui.mvi.launchIO
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.CalendarGray
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.Primary
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.SuccessGreen
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.VisaCalendar
import ru.nikfirs.android.traveltracker.feature.calendar.ui.model.DateDataModel
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.CalculateDaysInPeriodUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.dataStore.GetDateFormatUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.trip.GetTripByIdUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.trip.GetTripsFlowByDatesUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.visa.GetVisaByIdUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.visa.GetVisaFlowByDateUseCase
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.Action
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.Effect
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.Filters
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.State
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CalendarViewmodel @Inject constructor(
    private val getTripsFlowByDatesUseCase: GetTripsFlowByDatesUseCase,
    private val getVisaFlowByDateUseCase: GetVisaFlowByDateUseCase,
    private val calculateDaysInPeriodUseCase: CalculateDaysInPeriodUseCase,
    private val getTripByIdUseCase: GetTripByIdUseCase,
    private val getVisaByIdUseCase: GetVisaByIdUseCase,
    private val getDateFormatUseCase: GetDateFormatUseCase,
) : ViewModel<Action, Effect, State>() {

    init {
        loadData()
    }

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            Action.LoadData -> loadData()
            is Action.SetError -> setError(action.error)
            is Action.ShowFilters -> showFilters(action.value)
            is Action.UpdateFilters -> updateFilters(action.filters)
            is Action.GetDateInfo -> getDateData(action.date)
            Action.ClearDateInfo -> clearDateData()
            Action.NavigateToTripDetails -> navigateToTripDetails()
            Action.NavigateToVisaDetails -> navigateToVisaDetails()
        }
    }

    private fun loadData() {
        setState { it.copy(isLoading = true, error = null) }

        launchIO {
            launchIO {
                try {
                    getDateFormatUseCase.invoke().collectLatest { dateFormat ->
                        setState { it.copy(dateFormatter = dateFormat.getFormatter()) }
                    }
                } catch (e: Exception) {
                    Log.e("CalendarViewmodel", "loadData, date format", e)
                }
            }
        }

        launch {
            try {
                val startDate = LocalDate.now().minusDays(PERIOD_DAYS.toLong())

                combine(
                    getTripsFlowByDatesUseCase(startDate),
                    getVisaFlowByDateUseCase(startDate, true)
                ) { trips, visas ->
                    Pair(trips, visas)
                }.collectLatest { (trips, visas) ->
                    val dateRange = calculateAvailableDateRange(
                        trips = trips,
                        visaExpiryDate = visas.maxOfOrNull { it.expiryDate }
                    )
                    val tripRanges = createTripRanges(trips)
                    val visaRanges = createVisaRanges(visas)
                    val dateList = createDateList(trips, dateRange)

                    setState {
                        it.copy(
                            isLoading = false,
                            tripRanges = tripRanges,
                            visaRanges = visaRanges,
                            dateList = dateList,
                            availableDateRange = dateRange,
                        )
                    }
                }
            } catch (e: Exception) {
                setError(CustomString.Resource(uiR.string.error_loading_data))
                Log.e(null, "loadData", e)
            }
        }
    }

    /**
     * Defines available range for calendar:
     * - startDate - first day of month when there's trip that crosses 180 days from now
     * (or 180 days before now if now such trip)
     * - endDate - the last day of month of last future trip (or plus 1 month from now)
     */
    private fun calculateAvailableDateRange(
        trips: List<Trip>,
        visaExpiryDate: LocalDate?,
    ): ClosedRange<LocalDate> {
        val today = LocalDate.now()
        val defaultStartDate = today.minusDays(PERIOD_DAYS.toLong())
        val defaultEndDate = today.plusMonths(1)

        val earliestTripInRange = trips
            .filter {
                it.endDate != null
                        && (it.endDate ?: defaultStartDate.minusDays(1)) >= defaultStartDate
            }
            .minByOrNull { it.startDate ?: defaultStartDate }

        val startDate = minOf(earliestTripInRange?.startDate ?: defaultStartDate, defaultStartDate)

        val latestFutureTrip = trips
            .filter { it.isFuture && it.endDate != null }
            .maxByOrNull { it.endDate ?: defaultEndDate }

        val endDate =
            maxOf(latestFutureTrip?.endDate ?: defaultEndDate, visaExpiryDate ?: defaultEndDate)

        return startDate.withDayOfMonth(1)..endDate.withDayOfMonth(endDate.lengthOfMonth())
    }

    /**
     * Creates trip list of ExistingRange with colors:
     * - Past trips: CalendarGray
     * - Ongoing trip: SuccessGreen
     * - Future trips: Primary
     */
    private fun createTripRanges(trips: List<Trip>): List<ExistingRange> {
        return trips.mapNotNull { trip ->
            val startDate = trip.startDate ?: return@mapNotNull null
            val endDate = trip.endDate ?: return@mapNotNull null

            val color = when {
                trip.isPast -> CalendarGray
                trip.isOngoing -> SuccessGreen
                trip.isFuture -> Primary
                else -> CalendarGray
            }

            ExistingRange(
                startDate = startDate,
                endDate = endDate,
                color = color,
                type = DateType.Trip(trip.id)
            )
        }
    }

    /**
     * Creates visa list of ExistingRange with color
     */
    private fun createVisaRanges(visas: List<Visa>): List<ExistingRange> {
        val color = VisaCalendar
        return visas.map { visa ->
            ExistingRange(
                startDate = visa.startDate,
                endDate = visa.expiryDate,
                color = color,
                type = DateType.Visa(visa.id)
            )
        }
    }

    /**
     * Create list of DayCalculation with visa usage data:
     * - isUsed = true, when during this day there's a segment with isExempt = false of any trip
     * - isIncreased = true, when 180 days ago there was a segment with isExempt = false of any trip
     * - remaining = days remaining from 90
     * Add days only if isUsed or isIncreased = true (changes in remaining days)
     */
    private suspend fun createDateList(
        trips: List<Trip>,
        dateRange: ClosedRange<LocalDate>,
    ): List<DayCalculation> {
        val dateList = mutableListOf<DayCalculation>()
        var currentDate = dateRange.start

        while (currentDate <= dateRange.endInclusive) {
            val isUsed = trips.any { trip ->
                val startDate = trip.startDate ?: return@any false
                val endDate = trip.endDate ?: return@any false
                currentDate in startDate..endDate &&
                        trip.segments.any {
                            currentDate >= it.startDate &&
                                    currentDate <= it.endDate &&
                                    !it.isExempt
                        }

            }

            val dateMinusPeriod = currentDate.minusDays(PERIOD_DAYS.toLong())
            val isIncreased = trips.any { trip ->
                val startDate = trip.startDate ?: return@any false
                val endDate = trip.endDate ?: return@any false
                dateMinusPeriod in startDate..endDate &&
                        trip.segments.any {
                            dateMinusPeriod >= it.startDate &&
                                    dateMinusPeriod <= it.endDate &&
                                    !it.isExempt
                        }
            }

            // calculate remaining days
            val daysCalculation = calculateDaysInPeriodUseCase.invoke(
                periodEnd = currentDate,
            )
            val remaining = daysCalculation.remainingDays

            if (isUsed || isIncreased) {
                dateList.add(
                    DayCalculation(
                        date = currentDate,
                        remaining = remaining,
                        isUsed = isUsed,
                        isIncreased = isIncreased
                    )
                )
            }

            currentDate = currentDate.plusDays(1)
        }

        return dateList
    }

    private fun showFilters(value: Boolean) {
        setState { it.copy(showFilters = value) }
    }

    private fun updateFilters(filters: Filters) {
        setState { it.copy(filters = filters) }
    }

    private fun getDateData(date: LocalDate) {
        launchIO {
            val deferredTrip = async {
                val tripId = currentState.tripRanges.find { trip ->
                    !date.isAfter(trip.endDate) && !date.isBefore(trip.startDate)
                }?.type?.id

                tripId?.let { getTripByIdUseCase.invoke(it) }
            }

            val deferredVisa = async {
                val visaId = currentState.visaRanges.find { trip ->
                    !date.isAfter(trip.endDate) && !date.isBefore(trip.startDate)
                }?.type?.id

                visaId?.let { getVisaByIdUseCase.invoke(it) }
            }

            val deferredDaysCalculation =
                async { calculateDaysInPeriodUseCase.invoke(periodEnd = date) }
            val isIncreased = currentState.dateList.find { it.date == date }?.isIncreased

            val trip = deferredTrip.await()
            val visa = deferredVisa.await()
            val daysCalculation = deferredDaysCalculation.await()

            setState {
                it.copy(
                    dateInformation = DateDataModel(
                        date = date,
                        trip = trip,
                        visa = visa,
                        remainingDays = daysCalculation.remainingDays,
                        isIncreased = isIncreased,
                    )
                )
            }
        }
    }

    private fun clearDateData() {
        setState { it.copy(dateInformation = null) }
    }

    private fun navigateToTripDetails() {
        currentState.dateInformation?.trip?.id?.let {
            setEffect { Effect.NavigateToTripDetails(it) }
        }
    }

    private fun navigateToVisaDetails() {
        currentState.dateInformation?.visa?.id?.let {
            setEffect { Effect.NavigateToVisaDetails(it) }
        }
    }

    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error) }
    }
}