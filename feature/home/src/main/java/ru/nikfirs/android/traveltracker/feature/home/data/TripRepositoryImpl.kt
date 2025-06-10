package ru.nikfirs.android.traveltracker.feature.home.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.nikfirs.android.traveltracker.core.data.database.dao.TripDao
import ru.nikfirs.android.traveltracker.core.data.database.dao.TripSegmentDao
import ru.nikfirs.android.traveltracker.core.data.mapper.toEntity
import ru.nikfirs.android.traveltracker.core.data.mapper.toModel
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

    override fun getPastTrips(): Flow<List<Trip>> {
        return tripDao.getPastTripsWithSegments().map { tripsWithSegments ->
            tripsWithSegments.map { it.toModel() }
        }
    }

    override fun getPlannedTrips(): Flow<List<Trip>> {
        return tripDao.getPlannedTripsWithSegments().map { tripsWithSegments ->
            tripsWithSegments.map { it.toModel() }
        }
    }

    override fun getTripsInPeriod(startDate: LocalDate, endDate: LocalDate): Flow<List<Trip>> {
        return tripDao.getTripsInPeriodWithSegments(startDate, endDate).map { tripsWithSegments ->
            tripsWithSegments.map { it.toModel() }
        }
    }

    override fun getTripsByCountry(country: String): Flow<List<Trip>> {
        return getAllTrips().map { trips ->
            trips.filter { trip ->
                trip.segments.any { it.country == country }
            }
        }
    }

    override suspend fun getTripById(tripId: Long): Trip? {
        return tripDao.getTripByIdWithSegments(tripId)?.toModel()
    }

    override suspend fun insertTrip(trip: Trip): Long {
        val tripId = tripDao.insertTrip(trip.toEntity())

        // Сохраняем сегменты
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
        exemptCountries: Set<String>
    ): DaysCalculation {
        val periodStart = periodEnd.minusDays(179)

        // Получаем количество дней с учетом исключений
        val totalDaysUsed = tripDao.getDaysCountInPeriodWithExemptions(
            periodStart = periodStart,
            periodEnd = periodEnd,
            exemptCountries = exemptCountries.toList()
        )

        // Получаем статистику по странам
        val countryStatisticsList = tripDao.getCountryStatistics(periodStart, periodEnd)
        val daysPerCountry = countryStatisticsList.associate { it.country to it.days }

        val remainingDays = DaysCalculation.MAX_STAY_DAYS - totalDaysUsed

        return DaysCalculation(
            totalDaysUsed = totalDaysUsed,
            remainingDays = remainingDays.coerceAtLeast(0),
            periodStart = periodStart,
            periodEnd = periodEnd,
            isNearLimit = totalDaysUsed >= DaysCalculation.WARNING_THRESHOLD,
            isOverLimit = totalDaysUsed > DaysCalculation.MAX_STAY_DAYS,
            exemptCountries = exemptCountries,
            daysPerCountry = daysPerCountry
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