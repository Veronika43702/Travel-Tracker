package ru.nikfirs.android.traveltracker.feature.home.domain.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

data class TripSegmentUi(
    val country: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val cities: List<String> = emptyList(),
    val isExempt: Boolean = false,
    val color: Color = Color.Blue
) {
    val duration: Long
        get() = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
}