package ru.nikfirs.android.traveltracker.feature.home.domain.model

import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.Visa

enum class HomeTab {
    ALL,
    VISAS,
    TRIPS
}

sealed class HomeItem {
    data class VisaItem(val visa: Visa) : HomeItem()
    data class TripItem(val trip: Trip, val isExempt: Boolean) : HomeItem()
}

data class HomeData(
    val activeVisas: List<Visa>,
    val allTrips: List<Trip>,
    val exemptCountries: Set<String>
)