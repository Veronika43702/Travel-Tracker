package ru.nikfirs.android.traveltracker.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.DaysCalculation
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import java.time.LocalDate

interface TripRepository {
    fun getAllTrips(): Flow<List<Trip>>
    fun getPastTrips(): Flow<List<Trip>>
    fun getPlannedTrips(): Flow<List<Trip>>
    fun getTripsInPeriod(startDate: LocalDate, endDate: LocalDate): Flow<List<Trip>>
    fun getTripsByCountry(country: String): Flow<List<Trip>>
    suspend fun getTripById(tripId: Long): Trip?
    suspend fun insertTrip(trip: Trip): Long
    suspend fun updateTrip(trip: Trip)
    suspend fun deleteTrip(trip: Trip)
    suspend fun calculateDaysInPeriod(
        periodEnd: LocalDate,
        exemptCountries: Set<String> = emptySet()
    ): DaysCalculation
    suspend fun checkIfDatesAvailable(
        segments: List<TripSegment>,
        exemptCountries: Set<String> = emptySet(),
        excludeTripId: Long? = null
    ): Boolean
    suspend fun getCountryStatistics(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): Map<String, Int>
}