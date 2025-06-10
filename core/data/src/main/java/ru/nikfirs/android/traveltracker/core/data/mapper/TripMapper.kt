package ru.nikfirs.android.traveltracker.core.data.mapper

import ru.nikfirs.android.traveltracker.core.data.database.entity.TripEntity
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripWithSegments
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripSegmentEntity
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripPurpose as EntityTripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose as DomainTripPurpose
import ru.nikfirs.android.traveltracker.core.data.database.entity.SegmentType as EntitySegmentType
import ru.nikfirs.android.traveltracker.core.domain.model.SegmentType as DomainSegmentType

fun TripWithSegments.toModel(): Trip {
    return Trip(
        id = trip.id,
        visaId = trip.visaId,
        startDate = trip.startDate,
        endDate = trip.endDate,
        segments = segments.map { it.toModel() },
        purpose = trip.purpose.toDomainTripPurpose(),
        isPlanned = trip.isPlanned,
        notes = trip.notes,
        createdAt = trip.createdAt
    )
}

fun Trip.toEntity(): TripEntity {
    return TripEntity(
        id = id,
        visaId = visaId,
        startDate = startDate,
        endDate = endDate,
        purpose = purpose.toEntityTripPurpose(),
        isPlanned = isPlanned,
        notes = notes,
        createdAt = createdAt
    )
}

fun TripSegmentEntity.toModel(): TripSegment {
    return TripSegment(
        country = country,
        startDate = startDate,
        endDate = endDate,
        type = segmentType.toDomainSegmentType(),
        cities = cities?.split(", ") ?: emptyList()
    )
}

fun TripSegment.toEntity(tripId: Long): TripSegmentEntity {
    return TripSegmentEntity(
        tripId = tripId,
        country = country,
        startDate = startDate,
        endDate = endDate,
        segmentType = type.toEntitySegmentType(),
        cities = cities.takeIf { it.isNotEmpty() }?.joinToString(", ")
    )
}

fun EntityTripPurpose.toDomainTripPurpose(): DomainTripPurpose {
    return when (this) {
        EntityTripPurpose.TOURISM -> DomainTripPurpose.TOURISM
        EntityTripPurpose.BUSINESS -> DomainTripPurpose.BUSINESS
        EntityTripPurpose.FAMILY -> DomainTripPurpose.FAMILY
        EntityTripPurpose.MEDICAL -> DomainTripPurpose.MEDICAL
        EntityTripPurpose.EDUCATION -> DomainTripPurpose.EDUCATION
        EntityTripPurpose.OTHER -> DomainTripPurpose.OTHER
    }
}

fun DomainTripPurpose.toEntityTripPurpose(): EntityTripPurpose {
    return when (this) {
        DomainTripPurpose.TOURISM -> EntityTripPurpose.TOURISM
        DomainTripPurpose.BUSINESS -> EntityTripPurpose.BUSINESS
        DomainTripPurpose.FAMILY -> EntityTripPurpose.FAMILY
        DomainTripPurpose.MEDICAL -> EntityTripPurpose.MEDICAL
        DomainTripPurpose.EDUCATION -> EntityTripPurpose.EDUCATION
        DomainTripPurpose.OTHER -> EntityTripPurpose.OTHER
    }
}

fun EntitySegmentType.toDomainSegmentType(): DomainSegmentType {
    return when (this) {
        EntitySegmentType.STAY -> DomainSegmentType.STAY
        EntitySegmentType.TRANSIT -> DomainSegmentType.TRANSIT
    }
}

fun DomainSegmentType.toEntitySegmentType(): EntitySegmentType {
    return when (this) {
        DomainSegmentType.STAY -> EntitySegmentType.STAY
        DomainSegmentType.TRANSIT -> EntitySegmentType.TRANSIT
    }
}