package ru.nikfirs.android.traveltracker.core.data.mapper

import ru.nikfirs.android.traveltracker.core.data.database.entity.TripEntity
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripPurpose as EntityTripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose as DomainTripPurpose

fun TripEntity.toModel(): Trip {
    return Trip(
        id = id,
        visaId = visaId,
        startDate = startDate,
        endDate = endDate,
        country = country,
        city = city,
        purpose = purpose.toDomainTripPurpose(),
        isPlanned = isPlanned,
        notes = notes,
        createdAt = createdAt
    )
}

fun Trip.toEntity(): TripEntity {
    return TripEntity(
        id = id,
        visaId = visaId,
        startDate = startDate,
        endDate = endDate,
        country = country,
        city = city,
        purpose = purpose.toEntityTripPurpose(),
        isPlanned = isPlanned,
        notes = notes,
        createdAt = createdAt
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