package ru.nikfirs.android.traveltracker.core.domain.model

import java.time.LocalDate

fun testTrip(
    id: Long = 0,
    visaId: Long? = 2,
    startDate: LocalDate = LocalDate.of(2026, 8, 29),
    endDate: LocalDate = LocalDate.of(2026, 9, 10),
    segments: List<TripSegment> = listOf(testTripSegment()),
    purpose: TripPurpose = TripPurpose.EDUCATION,
    notes: String? = "some notes for tests",
    createdAt: LocalDate = LocalDate.of(2026, 8, 1),
    countableDays: Int = 5,
    hasOverLimitDay: Boolean = false,
): Trip = Trip(
    id,
    visaId,
    startDate,
    endDate,
    segments,
    purpose,
    notes,
    createdAt,
    countableDays,
    hasOverLimitDay
)

fun testTripSegment(
    country: String = "SLO",
    isExempt: Boolean = false,
    startDate: LocalDate = LocalDate.of(2026, 8, 30),
    endDate: LocalDate = LocalDate.of(2026, 9, 2),
    cities: List<String> = listOf("Ljubljana", "Koper")
): TripSegment = TripSegment(country, isExempt, startDate, endDate, cities)