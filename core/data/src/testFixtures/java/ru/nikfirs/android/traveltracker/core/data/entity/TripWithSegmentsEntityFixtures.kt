package ru.nikfirs.android.traveltracker.core.data.entity

import ru.nikfirs.android.traveltracker.core.data.database.entity.TripEntity
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripPurpose
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripSegmentEntity
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripWithSegments
import java.time.LocalDate

fun testTripWithSegmentsEntity(
    id: Long = 0,
    visaId: Long? = 2,
    startDate: LocalDate = LocalDate.of(2026, 8, 29),
    endDate: LocalDate = LocalDate.of(2026, 9, 10),
    segments: List<TripSegmentEntity> = listOf(testTripSegmentEntity()),
    purpose: TripPurpose = TripPurpose.EDUCATION,
    notes: String? = "some notes for tests",
    createdAt: LocalDate = LocalDate.of(2026, 8, 1),
): TripWithSegments = TripWithSegments(
    trip = TripEntity(
        id,
        visaId,
        startDate,
        endDate,
        purpose,
        notes,
        createdAt,
    ),
    segments = segments,
)

fun testTripSegmentEntity(
    id: Long = 0,
    tripId: Long = 0,
    country: String = "SLO",
    startDate: LocalDate = LocalDate.of(2026, 8, 30),
    endDate: LocalDate = LocalDate.of(2026, 9, 2),
    cities: String = "Ljubljana, Koper",
    isExempt: Boolean = false,
): TripSegmentEntity = TripSegmentEntity(
    id, tripId, country, startDate, endDate, cities, isExempt
)