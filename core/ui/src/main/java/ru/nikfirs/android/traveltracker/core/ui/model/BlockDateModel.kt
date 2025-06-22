package ru.nikfirs.android.traveltracker.core.ui.model

import java.time.LocalDate

data class BlockDateModel(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val type: BlockDateType? = null,
)

sealed class BlockDateType {
    data class Trip(val tripId: Long) : BlockDateType()
    data object DayLimit : BlockDateType()
}
