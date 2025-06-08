package ru.nikfirs.android.traveltracker.core.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripEntity
import java.time.LocalDate

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY startDate DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE isPlanned = 0 ORDER BY startDate DESC")
    fun getPastTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE isPlanned = 1 ORDER BY startDate ASC")
    fun getPlannedTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE startDate >= :startDate AND endDate <= :endDate ORDER BY startDate")
    fun getTripsInPeriod(startDate: LocalDate, endDate: LocalDate): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): TripEntity?

    @Insert
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("""
        SELECT COUNT(DISTINCT date) as dayCount
        FROM (
            SELECT DATE(startDate) as date
            FROM trips
            WHERE startDate BETWEEN :periodStart AND :periodEnd
            AND isPlanned = 0
            
            UNION ALL
            
            SELECT DATE(startDate, '+' || n || ' days') as date
            FROM trips, (
                SELECT 0 as n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 
                UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
                UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11
                UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
                UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19
                UNION ALL SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23
                UNION ALL SELECT 24 UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27
                UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30 UNION ALL SELECT 31
                UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35
                UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39
                UNION ALL SELECT 40 UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43
                UNION ALL SELECT 44 UNION ALL SELECT 45 UNION ALL SELECT 46 UNION ALL SELECT 47
                UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50 UNION ALL SELECT 51
                UNION ALL SELECT 52 UNION ALL SELECT 53 UNION ALL SELECT 54 UNION ALL SELECT 55
                UNION ALL SELECT 56 UNION ALL SELECT 57 UNION ALL SELECT 58 UNION ALL SELECT 59
                UNION ALL SELECT 60 UNION ALL SELECT 61 UNION ALL SELECT 62 UNION ALL SELECT 63
                UNION ALL SELECT 64 UNION ALL SELECT 65 UNION ALL SELECT 66 UNION ALL SELECT 67
                UNION ALL SELECT 68 UNION ALL SELECT 69 UNION ALL SELECT 70 UNION ALL SELECT 71
                UNION ALL SELECT 72 UNION ALL SELECT 73 UNION ALL SELECT 74 UNION ALL SELECT 75
                UNION ALL SELECT 76 UNION ALL SELECT 77 UNION ALL SELECT 78 UNION ALL SELECT 79
                UNION ALL SELECT 80 UNION ALL SELECT 81 UNION ALL SELECT 82 UNION ALL SELECT 83
                UNION ALL SELECT 84 UNION ALL SELECT 85 UNION ALL SELECT 86 UNION ALL SELECT 87
                UNION ALL SELECT 88 UNION ALL SELECT 89
            )
            WHERE DATE(startDate, '+' || n || ' days') <= DATE(endDate)
            AND DATE(startDate, '+' || n || ' days') BETWEEN :periodStart AND :periodEnd
            AND isPlanned = 0
        )
    """)
    suspend fun getDaysCountInPeriod(periodStart: LocalDate, periodEnd: LocalDate): Int
}