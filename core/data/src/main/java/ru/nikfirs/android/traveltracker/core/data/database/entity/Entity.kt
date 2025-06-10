package ru.nikfirs.android.traveltracker.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "visas")
data class VisaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val visaNumber: String,
    val visaCategory: VisaCategory,
    val country: String? = null,
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
    val visaId: Long? = null,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val purpose: TripPurpose = TripPurpose.TOURISM,
    val isPlanned: Boolean = false,
    val notes: String? = null,
    val createdAt: LocalDate = LocalDate.now()
)

@Entity(
    tableName = "trip_segments",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tripId"])]
)
data class TripSegmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val country: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val segmentType: SegmentType = SegmentType.STAY,
    val cities: String? = null
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

enum class VisaCategory {
    TYPE_C,
    TYPE_D,
    RESIDENCE_PERMIT
}

enum class SegmentType {
    STAY,
    TRANSIT
}