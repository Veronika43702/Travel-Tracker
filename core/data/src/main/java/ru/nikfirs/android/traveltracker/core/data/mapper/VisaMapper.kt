package ru.nikfirs.android.traveltracker.core.data.mapper

import ru.nikfirs.android.traveltracker.core.data.database.entity.VisaEntity
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.data.database.entity.VisaType as EntityVisaType
import ru.nikfirs.android.traveltracker.core.domain.model.VisaType as DomainVisaType

fun VisaEntity.toModel(): Visa {
    return Visa(
        id = id,
        visaNumber = visaNumber,
        visaType = visaType,
        issueDate = issueDate,
        expiryDate = expiryDate,
        durationOfStay = durationOfStay,
        entries = entries.toDomainVisaType(),
        isActive = isActive,
        notes = notes
    )
}

fun Visa.toEntity(): VisaEntity {
    return VisaEntity(
        id = id,
        visaNumber = visaNumber,
        visaType = visaType,
        issueDate = issueDate,
        expiryDate = expiryDate,
        durationOfStay = durationOfStay,
        entries = entries.toEntityVisaType(),
        isActive = isActive,
        notes = notes
    )
}

// Enum mappers
fun EntityVisaType.toDomainVisaType(): DomainVisaType {
    return when (this) {
        EntityVisaType.SINGLE -> DomainVisaType.SINGLE
        EntityVisaType.DOUBLE -> DomainVisaType.DOUBLE
        EntityVisaType.MULTI -> DomainVisaType.MULTI
    }
}

fun DomainVisaType.toEntityVisaType(): EntityVisaType {
    return when (this) {
        DomainVisaType.SINGLE -> EntityVisaType.SINGLE
        DomainVisaType.DOUBLE -> EntityVisaType.DOUBLE
        DomainVisaType.MULTI -> EntityVisaType.MULTI
    }
}