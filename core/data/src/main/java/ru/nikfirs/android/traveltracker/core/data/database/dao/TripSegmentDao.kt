package ru.nikfirs.android.traveltracker.core.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.data.database.entity.SegmentType
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripEntity
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripSegmentEntity
import ru.nikfirs.android.traveltracker.core.data.model.CountryStatistics
import java.time.LocalDate

@Dao
interface TripSegmentDao {
    @Query("SELECT * FROM trip_segments WHERE tripId = :tripId ORDER BY startDate")
    suspend fun getSegmentsByTripId(tripId: Long): List<TripSegmentEntity>

    @Query("SELECT * FROM trip_segments WHERE tripId = :tripId ORDER BY startDate")
    fun getSegmentsByTripIdFlow(tripId: Long): Flow<List<TripSegmentEntity>>

    @Insert
    suspend fun insertSegment(segment: TripSegmentEntity): Long

    @Insert
    suspend fun insertSegments(segments: List<TripSegmentEntity>)

    @Update
    suspend fun updateSegment(segment: TripSegmentEntity)

    @Delete
    suspend fun deleteSegment(segment: TripSegmentEntity)

    @Query("DELETE FROM trip_segments WHERE tripId = :tripId")
    suspend fun deleteSegmentsByTripId(tripId: Long)

    @Query("""
        SELECT * FROM trip_segments 
        WHERE country = :country 
        AND startDate <= :endDate 
        AND endDate >= :startDate
        ORDER BY startDate
    """)
    suspend fun getSegmentsByCountryInPeriod(
        country: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TripSegmentEntity>

    @Query("""
        SELECT DISTINCT country FROM trip_segments 
        WHERE segmentType = :segmentType
        ORDER BY country
    """)
    suspend fun getCountriesBySegmentType(segmentType: SegmentType): List<String>

    @Query("""
        SELECT country, SUM(JULIANDAY(endDate) - JULIANDAY(startDate) + 1) as days
        FROM trip_segments
        WHERE startDate <= :endDate AND endDate >= :startDate
        AND segmentType != 'TRANSIT'
        GROUP BY country
    """)
    suspend fun getCountryStatisticsInPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CountryStatistics>
}