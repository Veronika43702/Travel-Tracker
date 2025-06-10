package ru.nikfirs.android.traveltracker.core.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripEntity
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripWithSegments
import ru.nikfirs.android.traveltracker.core.data.model.CountryStatistics
import java.time.LocalDate

@Dao
interface TripDao {
    @Transaction
    @Query("SELECT * FROM trips ORDER BY startDate DESC")
    fun getAllTripsWithSegments(): Flow<List<TripWithSegments>>

    @Transaction
    @Query("SELECT * FROM trips WHERE isPlanned = 0 ORDER BY startDate DESC")
    fun getPastTripsWithSegments(): Flow<List<TripWithSegments>>

    @Transaction
    @Query("SELECT * FROM trips WHERE isPlanned = 1 ORDER BY startDate ASC")
    fun getPlannedTripsWithSegments(): Flow<List<TripWithSegments>>

    @Transaction
    @Query(
        """
        SELECT * FROM trips 
        WHERE startDate <= :endDate AND endDate >= :startDate 
        ORDER BY startDate
    """
    )
    fun getTripsInPeriodWithSegments(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<TripWithSegments>>

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripByIdWithSegments(tripId: Long): TripWithSegments?

    @Insert
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("""
            WITH RECURSIVE dates(day, segment_id) AS (
            SELECT DATE(s.startDate) AS day, s.id
            FROM trip_segments s
            INNER JOIN trips t ON t.id = s.tripId
            WHERE t.isPlanned = 0
            AND s.country NOT IN (:exemptCountries)
            AND DATE(s.startDate) BETWEEN :periodStart AND :periodEnd

            UNION ALL

            SELECT DATE(day, '+1 day'), segment_id
            FROM dates
            WHERE DATE(day, '+1 day') <= (
                SELECT DATE(endDate)
                FROM trip_segments
                WHERE id = segment_id
            )
            AND DATE(day, '+1 day') <= :periodEnd
        )

        SELECT COUNT(DISTINCT day) AS dayCount
        FROM dates
    """)
    suspend fun getDaysCountInPeriodWithExemptions(
        periodStart: LocalDate,
        periodEnd: LocalDate,
        exemptCountries: List<String>
    ): Int

    // count days by countries for statistics
    @Query("""
        WITH RECURSIVE dates(day, country, segment_id) AS (
            SELECT DATE(s.startDate) AS day, s.country, s.id
            FROM trip_segments s
            INNER JOIN trips t ON t.id = s.tripId
            WHERE t.isPlanned = 0
            AND DATE(s.startDate) BETWEEN :periodStart AND :periodEnd

            UNION ALL

            SELECT DATE(day, '+1 day'), country, segment_id
            FROM dates
            WHERE DATE(day, '+1 day') <= (
                SELECT DATE(endDate)
                FROM trip_segments
                WHERE id = segment_id
            )
            AND DATE(day, '+1 day') <= :periodEnd
        )

        SELECT country, COUNT(DISTINCT day) as days
        FROM dates
        GROUP BY country
    """)
    suspend fun getCountryStatistics(
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): List<CountryStatistics>

    // checking available dates for new journey
    @Query(
        """
        SELECT COUNT(*) FROM (
            WITH RECURSIVE dates(date) AS (
                SELECT DATE(:startDate)
                UNION ALL
                SELECT DATE(date, '+1 day')
                FROM dates
                WHERE DATE(date, '+1 day') <= DATE(:endDate)
            ),
            existing_days AS (
                SELECT COUNT(DISTINCT d.date) as day_count
                FROM dates d
                WHERE EXISTS (
                    SELECT 1 FROM trip_segments s
                    INNER JOIN trips t ON t.id = s.tripId
                    WHERE t.isPlanned = 0
                    AND t.id != :excludeTripId
                    AND s.country NOT IN (:exemptCountries)
                    AND DATE(d.date) BETWEEN DATE(s.startDate) AND DATE(s.endDate)
                    AND DATE(d.date) BETWEEN DATE(:startDate, '-179 days') AND DATE(d.date)
                )
            )
            SELECT * FROM existing_days WHERE day_count >= 90
        )
    """
    )
    suspend fun checkDatesAvailability(
        startDate: LocalDate,
        endDate: LocalDate,
        exemptCountries: List<String>,
        excludeTripId: Long?
    ): Int
}