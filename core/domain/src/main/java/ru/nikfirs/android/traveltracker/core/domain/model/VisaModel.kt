package ru.nikfirs.android.traveltracker.core.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Visa(
    val id: Long = 0,
    val visaNumber: String,
    val visaType: VisaCategory = VisaCategory.TYPE_C,
    val country: String? = null,
    val issueDate: LocalDate,
    val expiryDate: LocalDate,
    val durationOfStay: Int,
    val entries: VisaEntries = VisaEntries.MULTI,
    val isActive: Boolean = true,
    val notes: String? = null
) {
    val isExpired: Boolean
        get() = expiryDate.isBefore(LocalDate.now())

    val daysUntilExpiry: Long
        get() = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate)

    val requiresSchengenTracking: Boolean
        get() = visaType == VisaCategory.TYPE_C
}

enum class VisaCategory {
    TYPE_C,
    TYPE_D,
    RESIDENCE_PERMIT,
}

enum class VisaEntries {
    SINGLE,
    DOUBLE,
    MULTI
}