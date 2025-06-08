package ru.nikfirs.android.traveltracker.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "visas")
data class VisaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val visaNumber: String,
    val visaType: String = "C",
    val issueDate: LocalDate,
    val expiryDate: LocalDate,
    val durationOfStay: Int = 90,
    val entries: VisaType = VisaType.MULTI,
    val isActive: Boolean = true,
    val notes: String? = null
)

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val visaId: Long? = null, // Связь с визой (опционально)
    val startDate: LocalDate,
    val endDate: LocalDate,
    val country: String = "EU",
    val city: String? = null,
    val purpose: TripPurpose = TripPurpose.TOURISM,
    val isPlanned: Boolean = false,
    val notes: String? = null,
    val createdAt: LocalDate = LocalDate.now()
)

enum class TripPurpose {
    TOURISM,
    BUSINESS,
    FAMILY,
    MEDICAL,
    EDUCATION,
    OTHER
}

enum class VisaType {
    SINGLE, DOUBLE, MULTI
}

// Вспомогательная сущность для подсчета дней
data class DayCount(
    val date: LocalDate,
    val dayNumber: Int,
    val isInTrip: Boolean,
    val tripId: Long? = null
)