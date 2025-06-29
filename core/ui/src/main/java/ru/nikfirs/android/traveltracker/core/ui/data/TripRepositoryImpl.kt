package ru.nikfirs.android.traveltracker.core.ui.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.nikfirs.android.traveltracker.core.data.database.dao.TripDao
import ru.nikfirs.android.traveltracker.core.data.database.dao.TripSegmentDao
import ru.nikfirs.android.traveltracker.core.data.mapper.toEntity
import ru.nikfirs.android.traveltracker.core.data.mapper.toModel
import ru.nikfirs.android.traveltracker.core.domain.MAX_STAY_DAYS
import ru.nikfirs.android.traveltracker.core.domain.PERIOD_DAYS
import ru.nikfirs.android.traveltracker.core.domain.model.DaysCalculation
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import java.time.LocalDate
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao,
    private val tripSegmentDao: TripSegmentDao
) : TripRepository {

    override fun getAllTrips(): Flow<List<Trip>> {
        return tripDao.getAllTripsWithSegments().map { tripsWithSegments ->
            tripsWithSegments.map { it.toModel() }
        }
    }

    override suspend fun getTripById(tripId: Long): Trip? {
        return tripDao.getTripByIdWithSegments(tripId)?.toModel()
    }

    override fun getTripsFlowByDates(startDate: LocalDate, endDate: LocalDate?): Flow<List<Trip>> {
        return tripDao.getTripsByDatesFlow(startDate, endDate).map { tripsWithSegments ->
            tripsWithSegments.map { it.toModel() }
        }
    }

    override suspend fun getTripsByDates(startDate: LocalDate, endDate: LocalDate): List<Trip> {
        return tripDao.getTripsByDates(startDate, endDate).map { it.toModel() }
    }

    override suspend fun insertTrip(trip: Trip): Long {
        val tripId = tripDao.insertTrip(trip.toEntity())

        val segments = trip.segments.map { it.toEntity(tripId) }
        tripSegmentDao.insertSegments(segments)

        return tripId
    }

    override suspend fun updateTrip(trip: Trip) {
        tripDao.updateTrip(trip.toEntity())

        // Удаляем старые сегменты и вставляем новые
        tripSegmentDao.deleteSegmentsByTripId(trip.id)
        val segments = trip.segments.map { it.toEntity(trip.id) }
        tripSegmentDao.insertSegments(segments)
    }

    override suspend fun deleteTrip(trip: Trip) {
        tripDao.deleteTrip(trip.toEntity())
        // Сегменты удалятся автоматически благодаря CASCADE
    }

    override suspend fun calculateDaysInPeriod(
        periodEnd: LocalDate,
        tripId: Long?,
    ): DaysCalculation {
        val periodStart = periodEnd.minusDays((PERIOD_DAYS - 1).toLong())

        val totalDaysUsed = tripDao.getDaysCountInPeriodWithExemptions(
            periodStart = periodStart,
            periodEnd = periodEnd,
            tripId = tripId,
        )

        val remainingDays = MAX_STAY_DAYS - totalDaysUsed

        return DaysCalculation(
            totalDaysUsed = totalDaysUsed,
            remainingDays = remainingDays,
            periodStart = periodStart,
            periodEnd = periodEnd,
        )
    }

    override suspend fun checkIfDatesAvailable(
        segments: List<TripSegment>,
        exemptCountries: Set<String>,
        excludeTripId: Long?
    ): Boolean {
        // Находим минимальную и максимальную даты из всех сегментов
        val startDate = segments.minBy { it.startDate }.startDate
        val endDate = segments.maxBy { it.endDate }.endDate

        // Проверяем, не превысим ли лимит 90 дней
        val conflictCount = tripDao.checkDatesAvailability(
            startDate = startDate,
            endDate = endDate,
            exemptCountries = exemptCountries.toList(),
            excludeTripId = excludeTripId
        )

        return conflictCount == 0
    }

    override suspend fun getCountryStatistics(
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Map<String, Int> {
        val actualStartDate = startDate ?: LocalDate.now().minusYears(1)
        val actualEndDate = endDate ?: LocalDate.now()

        val statisticsList = tripDao.getCountryStatistics(actualStartDate, actualEndDate)
        return statisticsList.associate { it.country to it.days }
    }
}