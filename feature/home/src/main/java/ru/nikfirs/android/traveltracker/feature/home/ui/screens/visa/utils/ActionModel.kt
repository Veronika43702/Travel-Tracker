package ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.utils

import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.Visa

enum class VisaAction {
    ANNUL, DELETE
}

data class HomeActionModel(
    val action: HomeAction,
    val visa: Visa? = null,
    val trip: Trip? = null,
)

enum class HomeAction {
    DELETE_VISA, DELETE_TRIP
}