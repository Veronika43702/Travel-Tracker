package ru.nikfirs.android.traveltracker.core.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Trip(
    val id: Long = 0,
    val visaId: Long? = null,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val country: String = "EU",
    val city: String? = null,
    val purpose: TripPurpose = TripPurpose.TOURISM,
    val isPlanned: Boolean = false,
    val notes: String? = null,
    val createdAt: LocalDate = LocalDate.now()
) {
    val duration: Long
        get() = ChronoUnit.DAYS.between(startDate, endDate) + 1

    val isOngoing: Boolean
        get() {
            val today = LocalDate.now()
            return !isPlanned && today.isAfter(startDate.minusDays(1)) && today.isBefore(endDate.plusDays(1))
        }

    val isPast: Boolean
        get() = !isPlanned && endDate.isBefore(LocalDate.now())

    val isFuture: Boolean
        get() = isPlanned || startDate.isAfter(LocalDate.now())
}

enum class TripPurpose {
    TOURISM,
    BUSINESS,
    FAMILY,
    MEDICAL,
    EDUCATION,
    OTHER
}