package ru.nikfirs.android.traveltracker.core.data.entity

import ru.nikfirs.android.traveltracker.core.data.database.entity.TripEntity
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripPurpose
import java.time.LocalDate

fun testTripEntity(
    id: Long = 0,
    visaId: Long? = 2,
    startDate: LocalDate = LocalDate.of(2026, 8, 29),
    endDate: LocalDate = LocalDate.of(2026, 9, 10),
    purpose: TripPurpose = TripPurpose.EDUCATION,
    notes: String? = "some notes for tests",
    createdAt: LocalDate = LocalDate.of(2026, 8, 1),
): TripEntity = TripEntity(
    id,
    visaId,
    startDate,
    endDate,
    purpose,
    notes,
    createdAt,
)