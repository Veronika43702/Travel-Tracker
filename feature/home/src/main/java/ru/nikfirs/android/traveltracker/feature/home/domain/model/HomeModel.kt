package ru.nikfirs.android.traveltracker.feature.home.domain.model

import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.Visa

enum class HomeTab {
    TRIPS,
    VISAS,
}

sealed class HomeItem {
    data class VisaItem(val visa: Visa) : HomeItem()
    data class TripItem(val trip: Trip) : HomeItem()
}

data class HomeData(
    val allVisas: List<Visa>,
    val allTrips: List<Trip>,
    val exemptCountries: Set<String>
)