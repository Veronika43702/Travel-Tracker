package ru.nikfirs.android.traveltracker.core.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Trip(
    val id: Long = 0,
    val visaId: Long? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val segments: List<TripSegment> = emptyList(),
    val purpose: TripPurpose = TripPurpose.TOURISM,
    val notes: String? = null,
    val createdAt: LocalDate = LocalDate.now()
) {
    val duration: Long
        get() = ChronoUnit.DAYS.between(startDate, endDate) + 1

    val isFuture: Boolean
        get() = startDate?.isAfter(LocalDate.now()) == true

    val isOngoing: Boolean
        get() {
            val today = LocalDate.now()
            return !isFuture && today.isAfter(startDate?.minusDays(1)) && today.isBefore(
                endDate?.plusDays(
                    1
                )
            )
        }

    val isPast: Boolean
        get() = !isFuture && endDate?.isBefore(LocalDate.now()) == true

    val countries: List<String>
        get() = segments
            .map { it.country }
            .distinct()

    val primaryCountry: String?
        get() = segments.maxByOrNull { it.endDate.toEpochDay() - it.startDate.toEpochDay() }?.country

    val isMultiCountry: Boolean
        get() = countries.size > 1
}

data class TripSegment(
    val country: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val cities: List<String> = emptyList(),
) {
    val duration: Long
        get() = ChronoUnit.DAYS.between(startDate, endDate) + 1
}

enum class SegmentType {
    STAY,
    TRANSIT
}

enum class TripPurpose {
    TOURISM,
    BUSINESS,
    FAMILY,
    MEDICAL,
    EDUCATION,
    OTHER
}