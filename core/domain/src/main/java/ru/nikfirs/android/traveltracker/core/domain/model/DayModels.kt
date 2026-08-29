package ru.nikfirs.android.traveltracker.core.domain.model

import ru.nikfirs.android.traveltracker.core.domain.MAX_STAY_DAYS
import ru.nikfirs.android.traveltracker.core.domain.WARNING_THRESHOLD
import java.time.LocalDate

data class DaysCalculation(
    val totalDaysUsed: Int,
    val remainingDays: Int,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
) {
    val isNearLimit = totalDaysUsed > WARNING_THRESHOLD
    val isOverLimit = totalDaysUsed > MAX_STAY_DAYS
}