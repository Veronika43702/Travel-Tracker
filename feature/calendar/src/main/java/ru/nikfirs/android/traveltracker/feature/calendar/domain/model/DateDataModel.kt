package ru.nikfirs.android.traveltracker.feature.calendar.domain.model

import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.Visa

data class DateDataModel(
    val trip: Trip? = null,
    val visa: Visa? = null,
    val remainingDays: Int? = null,
    val isIncreased: Boolean? = false,
    val isUsed: Boolean? = false,
)