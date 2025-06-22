package ru.nikfirs.android.traveltracker.core.data.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TripWithSegments(
    @Embedded val trip: TripEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val segments: List<TripSegmentEntity>
)