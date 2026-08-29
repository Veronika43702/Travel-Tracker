package ru.nikfirs.android.traveltracker.core.domain.model

import java.time.LocalDate

fun testVisa(
    id: Long = 0,
    visaNumber: String = "123",
    visaType: VisaCategory = VisaCategory.TYPE_D,
    country: String = "SLO",
    startDate: LocalDate = LocalDate.of(2026, 8, 1),
    expiryDate: LocalDate = LocalDate.of(2026, 11, 1),
    durationOfStay: Int = 10,
    entries: VisaEntries = VisaEntries.SINGLE,
    isActive: Boolean = true,
    notes: String = "some notes for test"
): Visa = Visa(
    id,
    visaNumber,
    visaType,
    country,
    startDate,
    expiryDate,
    durationOfStay,
    entries,
    isActive,
    notes
)
