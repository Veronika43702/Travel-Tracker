package ru.nikfirs.android.traveltracker.feature.home.fake

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.model.DaysCalculation
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import java.time.LocalDate

/**
 * In-memory [TripRepository] implementation for unit tests (a practice example of the fake approach).
 */
class FakeTripRepository : TripRepository {
    val savedTrips = mutableListOf<Trip>()
    private var nextId = 1L

    override suspend fun insertTrip(trip: Trip): Long {
        val id = nextId++
        savedTrips += trip.copy(id = id)
        return id
    }

    override fun getAllTrips(): Flow<List<Trip>> = TODO("not needed in tests yet")

    override suspend fun updateTrip(trip: Trip) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTrip(trip: Trip) {
        TODO("Not yet implemented")
    }

    override suspend fun calculateDaysInPeriod(
        periodEnd: LocalDate,
        tripId: Long?
    ): DaysCalculation {
        TODO("Not yet implemented")
    }

    override suspend fun checkIfDatesAvailable(
        segments: List<TripSegment>,
        exemptCountries: Set<String>,
        excludeTripId: Long?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getCountryStatistics(
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Map<String, Int> {
        TODO("Not yet implemented")
    }

    override suspend fun getTripById(tripId: Long): Trip? =
        savedTrips.find { it.id == tripId }

    override suspend fun getTripsByDates(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Trip> {
        TODO("Not yet implemented")
    }

    override fun getTripsFlowByDates(
        startDate: LocalDate,
        endDate: LocalDate?
    ): Flow<List<Trip>> {
        TODO("Not yet implemented")
    }
}