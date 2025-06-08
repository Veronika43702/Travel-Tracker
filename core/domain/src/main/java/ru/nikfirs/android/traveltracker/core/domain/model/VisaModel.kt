package ru.nikfirs.android.traveltracker.core.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Visa(
    val id: Long = 0,
    val visaNumber: String,
    val visaType: String = "C",
    val issueDate: LocalDate,
    val expiryDate: LocalDate,
    val durationOfStay: Int = 90,
    val entries: VisaType = VisaType.MULTI,
    val isActive: Boolean = true,
    val notes: String? = null
) {
    val isExpired: Boolean
        get() = expiryDate.isBefore(LocalDate.now())

    val daysUntilExpiry: Long
        get() = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate)
}

enum class VisaType {
    SINGLE,
    DOUBLE,
    MULTI
}