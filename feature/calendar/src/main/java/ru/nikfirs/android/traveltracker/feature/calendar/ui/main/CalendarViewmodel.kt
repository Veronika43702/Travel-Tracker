package ru.nikfirs.android.traveltracker.feature.calendar.ui.main

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import ru.nikfirs.android.traveltracker.core.domain.PERIOD_DAYS
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import ru.nikfirs.android.traveltracker.core.ui.component.DayCalculation
import ru.nikfirs.android.traveltracker.core.ui.component.ExistingRange
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.core.ui.theme.CalendarGray
import ru.nikfirs.android.traveltracker.core.ui.theme.Primary
import ru.nikfirs.android.traveltracker.core.ui.theme.SuccessGreen
import javax.inject.Inject
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.Action
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.Effect
import ru.nikfirs.android.traveltracker.feature.calendar.ui.main.CalendarContract.State
import java.time.LocalDate


@HiltViewModel
class CalendarViewmodel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel<Action, Effect, State>() {

    init {
        loadData()
    }

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            Action.LoadData -> loadData()
            is Action.SetError -> setError(action.error)
        }
    }

    private fun loadData() {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            try {
                tripRepository.getAllTrips().collectLatest { allTrips ->
                    val today = LocalDate.now()
                    val dateRange = calculateAvailableDateRange(allTrips, today)

                    // Фильтруем поездки в нужном диапазоне
                    val filteredTrips = allTrips.filter { trip ->
                        trip.startDate != null && trip.endDate != null &&
                                isTripsOverlapWithRange(trip, dateRange)
                    }

                    // Создаем tripRanges с цветами
                    val tripRanges = createTripRanges(filteredTrips)

                    // Создаем dateList с расчетом remaining дней
                    val dateList = createDateList(filteredTrips, dateRange, today)

                    setState {
                        it.copy(
                            isLoading = false,
                            trips = filteredTrips,
                            tripRanges = tripRanges,
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
     * Определяет доступные даты для календаря:
     * - Самый ранний день: либо 180 дней от текущего, либо начало поездки если попадает в этот диапазон
     * - Самый поздний день: либо конец последней поездки в будущем, либо +1 месяц от текущей даты
     */
    private fun calculateAvailableDateRange(
        trips: List<Trip>,
        today: LocalDate
    ): ClosedRange<LocalDate> {
        val defaultStartDate = today.minusDays(PERIOD_DAYS.toLong())
        val defaultEndDate = today.plusMonths(1)

        // Находим самую раннюю поездку в диапазоне 180 дней
        val earliestTripInRange = trips
            .filter {
                it.endDate != null
                        && (it.endDate ?: defaultStartDate.minusDays(1)) >= defaultStartDate
            }
            .minByOrNull { it.startDate ?: defaultStartDate }

        val startDate = minOf(earliestTripInRange?.startDate ?: defaultStartDate, defaultStartDate)

        // Находим самую позднюю запланированную поездку
        val latestFutureTrip = trips
            .filter { it.isFuture && it.endDate != null }
            .maxByOrNull { it.endDate ?: defaultEndDate }

        val endDate = maxOf(latestFutureTrip?.endDate ?: defaultEndDate, defaultEndDate)

        return startDate..endDate
    }

    /**
     * Проверяет, пересекается ли поездка с заданным диапазоном дат
     */
    private fun isTripsOverlapWithRange(trip: Trip, dateRange: ClosedRange<LocalDate>): Boolean {
        val tripStart = trip.startDate ?: return false
        val tripEnd = trip.endDate ?: return false

        return tripEnd >= dateRange.start && tripStart <= dateRange.endInclusive
    }

    /**
     * Создает список ExistingRange с цветами:
     * - Прошедшие поездки: SuccessGreen
     * - Текущая поездка: WarningAmber
     * - Будущие поездки: DangerRed
     */
    private fun createTripRanges(trips: List<Trip>): List<ExistingRange> {
        return trips.mapNotNull { trip ->
            val startDate = trip.startDate ?: return@mapNotNull null
            val endDate = trip.endDate ?: return@mapNotNull null

            val color = when {
                trip.isPast -> CalendarGray
                trip.isOngoing -> SuccessGreen
                trip.isFuture -> Primary
                else -> Primary // fallback
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
     * Создает список DayCalculation с информацией о днях:
     * - isUsed = true если поездка приходится на этот день
     * - isIncreased = true если ровно 180 дней назад была поездка
     * - remaining = количество дней оставшихся на этот день
     * Добавляет только дни где есть изменения в remaining или специальные флаги
     */
    private suspend fun createDateList(
        trips: List<Trip>,
        dateRange: ClosedRange<LocalDate>,
        today: LocalDate
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

            // Проверяем, была ли поездка ровно 180 дней назад
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

            // Рассчитываем remaining дни для этого дня
            val daysCalculation = tripRepository.calculateDaysInPeriod(
                periodEnd = currentDate,
                tripId = null
            )
            val remaining = daysCalculation.remainingDays

            // Добавляем день в список только если есть изменения или специальные флаги
            val shouldInclude = isUsed || isIncreased

            if (shouldInclude) {
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


    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error) }
    }
}