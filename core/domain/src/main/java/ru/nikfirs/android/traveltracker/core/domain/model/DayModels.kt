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

data class CalendarDay(
    val date: LocalDate,
    val isAvailable: Boolean,
    val isInTrip: Boolean = false,
    val tripId: Long? = null,
    val country: String? = null,
    val isExempt: Boolean = false,
    val remainingDaysOnDate: Int? = null,
)

data class DayCountInfo(
    val date: LocalDate,
    val country: String,
    val tripId: Long,
    val isExempt: Boolean,
)