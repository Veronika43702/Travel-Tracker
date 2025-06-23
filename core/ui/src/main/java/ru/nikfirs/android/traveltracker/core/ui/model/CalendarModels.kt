package ru.nikfirs.android.traveltracker.core.ui.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

data class DayCalculation(
    val date: LocalDate,
    val remaining: Int,
    val isUsed: Boolean = false,
    val isIncreased: Boolean = false,
)

data class ExistingRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val color: Color,
    val type: DateType? = null,
)

data class BlockDateModel(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val type: DateType? = null,
)

sealed class DateType {
    data class Trip(val tripId: Long) : DateType()
    data class Visa(val visaId: Long) : DateType()
    data object DayLimit : DateType()
}

data class DateRangeSelection(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
) {
    val isComplete: Boolean get() = startDate != null && endDate != null
}