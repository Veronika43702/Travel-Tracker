package ru.nikfirs.android.traveltracker.core.domain.model

import java.time.LocalDate

fun testDaysCalculation(
    totalDaysUsed: Int = 30,
    remainingDays: Int = 60,
    periodStart: LocalDate = LocalDate.of(2026, 8, 1),
    periodEnd: LocalDate = LocalDate.of(2026, 12, 31),
): DaysCalculation = DaysCalculation(totalDaysUsed, remainingDays, periodStart, periodEnd)