package ru.nikfirs.android.traveltracker.feature.home.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.nikfirs.android.traveltracker.core.data.database.dao.TripDao
import ru.nikfirs.android.traveltracker.core.data.mapper.toEntity
import ru.nikfirs.android.traveltracker.core.data.mapper.toModel
import ru.nikfirs.android.traveltracker.core.domain.model.DaysCalculation
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao
) : TripRepository {

    override fun getAllTrips(): Flow<List<Trip>> {
        return tripDao.getAllTrips().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getPastTrips(): Flow<List<Trip>> {
        return tripDao.getPastTrips().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getPlannedTrips(): Flow<List<Trip>> {
        return tripDao.getPlannedTrips().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getTripsInPeriod(startDate: LocalDate, endDate: LocalDate): Flow<List<Trip>> {
        return tripDao.getTripsInPeriod(startDate, endDate).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun getTripById(tripId: Long): Trip? {
        return tripDao.getTripById(tripId)?.toModel()
    }

    override suspend fun insertTrip(trip: Trip): Long {
        return tripDao.insertTrip(trip.toEntity())
    }

    override suspend fun updateTrip(trip: Trip) {
        tripDao.updateTrip(trip.toEntity())
    }

    override suspend fun deleteTrip(trip: Trip) {
        tripDao.deleteTrip(trip.toEntity())
    }

    override suspend fun calculateDaysInPeriod(periodEnd: LocalDate): DaysCalculation {
        val periodStart = periodEnd.minusDays((DaysCalculation.PERIOD_DAYS - 1).toLong())

        val daysUsed = tripDao.getDaysCountInPeriod(periodStart, periodEnd)
        val remainingDays = DaysCalculation.MAX_STAY_DAYS - daysUsed

        return DaysCalculation(
            totalDaysUsed = daysUsed,
            remainingDays = remainingDays.coerceAtLeast(0),
            periodStart = periodStart,
            periodEnd = periodEnd,
            isNearLimit = daysUsed >= DaysCalculation.WARNING_THRESHOLD,
            isOverLimit = daysUsed > DaysCalculation.MAX_STAY_DAYS
        )
    }

    override suspend fun checkIfDatesAvailable(startDate: LocalDate, endDate: LocalDate): Boolean {
        // Проверяем каждый день в диапазоне
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val calculation = calculateDaysInPeriod(currentDate)

            // Если на любую дату превышен лимит, даты недоступны
            if (calculation.isOverLimit) {
                return false
            }

            // Проверяем, сколько дней поездки попадает в период 180 дней от текущей даты
            val tripDaysInPeriod = countTripDaysInPeriod(
                tripStart = startDate,
                tripEnd = endDate,
                periodStart = calculation.periodStart,
                periodEnd = calculation.periodEnd
            )

            // Если добавление этих дней превысит лимит, даты недоступны
            if (calculation.totalDaysUsed + tripDaysInPeriod > DaysCalculation.MAX_STAY_DAYS) {
                return false
            }

            currentDate = currentDate.plusDays(1)
        }

        return true
    }

    private fun countTripDaysInPeriod(
        tripStart: LocalDate,
        tripEnd: LocalDate,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): Int {
        val effectiveStart = maxOf(tripStart, periodStart)
        val effectiveEnd = minOf(tripEnd, periodEnd)

        return if (effectiveStart.isAfter(effectiveEnd)) {
            0
        } else {
            ChronoUnit.DAYS.between(effectiveStart, effectiveEnd).toInt() + 1
        }
    }
}