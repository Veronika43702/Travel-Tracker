package ru.nikfirs.android.traveltracker.feature.calendar.ui.main

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import ru.nikfirs.android.traveltracker.core.domain.PERIOD_DAYS
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.ui.component.DayCalculation
import ru.nikfirs.android.traveltracker.core.ui.component.ExistingRange
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.core.ui.theme.CalendarGray
import ru.nikfirs.android.traveltracker.core.ui.theme.Primary
import ru.nikfirs.android.traveltracker.core.ui.theme.SuccessGreen
import ru.nikfirs.android.traveltracker.core.ui.theme.VisaCalendar
import ru.nikfirs.android.traveltracker.feature.calendar.domain.usecase.CalculateDaysInPeriodUseCase
import ru.nikfirs.android.traveltracker.feature.calendar.domain.usecase.GetTripsFlowByDatesUseCase
import ru.nikfirs.android.traveltracker.feature.calendar.domain.usecase.GetVisaFlowByDateUseCase
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
        }
    }

    private fun loadData() {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            try {
                val startDate = LocalDate.now().minusDays(PERIOD_DAYS.toLong())

                combine(
                    getTripsFlowByDatesUseCase(startDate),
                    getVisaFlowByDateUseCase(startDate, true)
                ) { trips, visas ->
                    Pair(trips, visas)
                }.collectLatest { (trips, visas) ->
                    val dateRange = calculateAvailableDateRange(trips)
                    val tripRanges = createTripRanges(trips)
                    val visaRanges = createVisaRanges(visas)
                    val dateList = createDateList(trips, dateRange)

                    setState {
                        it.copy(
                            isLoading = false,
                            trips = trips,
                            tripRanges = tripRanges,
                            visaRanges = visaRanges,
                            dateList = dateList,
                            availableDateRange = dateRange,
                        )
                    }
                }
            } catch (e: Exception) {
                setError(CustomString.text(e.message ?: "Unknown error"))
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

        val endDate = maxOf(latestFutureTrip?.endDate ?: defaultEndDate, defaultEndDate)

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
                id = trip.id
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
                id = visa.id
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

    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error) }
    }
}