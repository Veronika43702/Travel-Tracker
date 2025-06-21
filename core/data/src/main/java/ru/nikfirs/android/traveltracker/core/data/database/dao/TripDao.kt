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
    @Query("SELECT * FROM trips WHERE startDate <= :endDate AND endDate >= :startDate")
    fun getTripsByDates(startDate: LocalDate, endDate: LocalDate): List<TripWithSegments>

    @Transaction
    @Query("SELECT * FROM trips ORDER BY startDate DESC")
    fun getAllTripsWithSegments(): Flow<List<TripWithSegments>>

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
            -- segments of all approprate trips with isExempt = false
            SELECT
                CASE
                    WHEN DATE(s.startDate) < :periodStart THEN :periodStart
                    ELSE DATE(s.startDate)
                END AS day,
                s.id
            FROM trip_segments s
            INNER JOIN trips t ON t.id = s.tripId
            WHERE s.isExempt = 0
            AND DATE(s.startDate) <= :periodEnd
            AND DATE(s.endDate) >= :periodStart

            UNION ALL

            -- segment days
            SELECT DATE(day, '+1 day'), segment_id
            FROM dates
            WHERE DATE(day, '+1 day') <= (
                SELECT
                    CASE
                        WHEN DATE(endDate) > :periodEnd THEN :periodEnd
                        ELSE DATE(endDate)
                    END
                FROM trip_segments
                WHERE id = segment_id
            )
        )

        SELECT COUNT(DISTINCT day) AS dayCount
        FROM dates
    """)
    suspend fun getDaysCountInPeriodWithExemptions(
        periodStart: LocalDate,
        periodEnd: LocalDate,
    ): Int

    // count days by countries for statistics
    @Query(
        """
        WITH RECURSIVE dates(day, country, segment_id) AS (
            SELECT DATE(s.startDate) AS day, s.country, s.id
            FROM trip_segments s
            INNER JOIN trips t ON t.id = s.tripId
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
    """
    )
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