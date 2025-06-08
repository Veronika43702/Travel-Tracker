package ru.nikfirs.android.traveltracker.core.domain.model

import java.time.LocalDate

data class DaysCalculation(
    val totalDaysUsed: Int,
    val remainingDays: Int,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val isNearLimit: Boolean = false,
    val isOverLimit: Boolean = false
) {
    companion object {
        const val PERIOD_DAYS = 180
        const val MAX_STAY_DAYS = 90
        const val WARNING_THRESHOLD = 75
    }
}

data class CalendarDay(
    val date: LocalDate,
    val isAvailable: Boolean,
    val isInTrip: Boolean = false,
    val tripId: Long? = null,
    val remainingDaysOnDate: Int? = null
)