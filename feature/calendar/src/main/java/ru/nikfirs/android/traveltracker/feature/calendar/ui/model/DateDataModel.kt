package ru.nikfirs.android.traveltracker.feature.calendar.ui.model

import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import java.time.LocalDate

data class DateDataModel(
    val date: LocalDate = LocalDate.now(),
    val trip: Trip? = null,
    val visa: Visa? = null,
    val remainingDays: Int? = null,
    val isIncreased: Boolean? = false,
)