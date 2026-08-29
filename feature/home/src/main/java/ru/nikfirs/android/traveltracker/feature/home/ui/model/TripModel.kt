package ru.nikfirs.android.traveltracker.feature.home.ui.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.util.UUID
import java.time.temporal.ChronoUnit

data class TripSegmentUi(
    val country: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val cities: List<String> = emptyList(),
    val isExempt: Boolean,
    val color: Color = Color.Blue,
    val uid: String = UUID.randomUUID().toString(),
) {
    val duration: Long
        get() = ChronoUnit.DAYS.between(startDate, endDate) + 1
}