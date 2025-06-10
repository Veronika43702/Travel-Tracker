package ru.nikfirs.android.traveltracker.core.data.mapper

import ru.nikfirs.android.traveltracker.core.data.database.entity.VisaEntity
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.data.database.entity.VisaType as EntityVisaType
import ru.nikfirs.android.traveltracker.core.domain.model.VisaEntries as DomainVisaEntries
import ru.nikfirs.android.traveltracker.core.data.database.entity.VisaCategory as EntityVisaCategory
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory as DomainVisaCategory

fun VisaEntity.toModel(): Visa {
    return Visa(
        id = id,
        visaNumber = visaNumber,
        visaType = visaCategory.toDomainVisaCategory(),
        country = country,
        issueDate = issueDate,
        expiryDate = expiryDate,
        durationOfStay = durationOfStay,
        entries = entries.toDomainVisaEntries(),
        isActive = isActive,
        notes = notes
    )
}

fun Visa.toEntity(): VisaEntity {
    return VisaEntity(
        id = id,
        visaNumber = visaNumber,
        visaCategory = visaType.toEntityVisaCategory(),
        country = country,
        issueDate = issueDate,
        expiryDate = expiryDate,
        durationOfStay = durationOfStay,
        entries = entries.toEntityVisaType(),
        isActive = isActive,
        notes = notes
    )
}

// Enum mappers
fun EntityVisaType.toDomainVisaEntries(): DomainVisaEntries {
    return when (this) {
        EntityVisaType.SINGLE -> DomainVisaEntries.SINGLE
        EntityVisaType.DOUBLE -> DomainVisaEntries.DOUBLE
        EntityVisaType.MULTI -> DomainVisaEntries.MULTI
    }
}

fun DomainVisaEntries.toEntityVisaType(): EntityVisaType {
    return when (this) {
        DomainVisaEntries.SINGLE -> EntityVisaType.SINGLE
        DomainVisaEntries.DOUBLE -> EntityVisaType.DOUBLE
        DomainVisaEntries.MULTI -> EntityVisaType.MULTI
    }
}

fun EntityVisaCategory.toDomainVisaCategory(): DomainVisaCategory {
    return when (this) {
        EntityVisaCategory.TYPE_C -> DomainVisaCategory.TYPE_C
        EntityVisaCategory.TYPE_D -> DomainVisaCategory.TYPE_D
        EntityVisaCategory.RESIDENCE_PERMIT -> DomainVisaCategory.RESIDENCE_PERMIT
    }
}

fun DomainVisaCategory.toEntityVisaCategory(): EntityVisaCategory {
    return when (this) {
        DomainVisaCategory.TYPE_C -> EntityVisaCategory.TYPE_C
        DomainVisaCategory.TYPE_D -> EntityVisaCategory.TYPE_D
        DomainVisaCategory.RESIDENCE_PERMIT -> EntityVisaCategory.RESIDENCE_PERMIT
    }
}