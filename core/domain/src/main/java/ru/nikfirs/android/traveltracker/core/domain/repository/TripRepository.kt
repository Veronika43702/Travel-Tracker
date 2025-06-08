package ru.nikfirs.android.traveltracker.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.DaysCalculation
import java.time.LocalDate

interface TripRepository {
    fun getAllTrips(): Flow<List<Trip>>
    fun getPastTrips(): Flow<List<Trip>>
    fun getPlannedTrips(): Flow<List<Trip>>
    fun getTripsInPeriod(startDate: LocalDate, endDate: LocalDate): Flow<List<Trip>>
    suspend fun getTripById(tripId: Long): Trip?
    suspend fun insertTrip(trip: Trip): Long
    suspend fun updateTrip(trip: Trip)
    suspend fun deleteTrip(trip: Trip)
    suspend fun calculateDaysInPeriod(periodEnd: LocalDate): DaysCalculation
    suspend fun checkIfDatesAvailable(startDate: LocalDate, endDate: LocalDate): Boolean
}