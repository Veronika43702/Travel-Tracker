package ru.nikfirs.android.traveltracker.feature.home.ui.main

import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.DaysCalculation
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeItem
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeTab

sealed class HomeContract {
    data class State(
        val isLoading: Boolean = true,
        val selectedTab: HomeTab = HomeTab.ALL,
        val visas: List<Visa> = emptyList(),
        val trips: List<Trip> = emptyList(),
        val daysCalculation: DaysCalculation? = null,
        val exemptCountries: Set<String> = emptySet(),
        val error: CustomString? = null
    ) : MviState {

        val activeVisas: List<Visa>
            get() = visas.filter { it.isActive && !it.isExpired }

        val currentSchengenVisa: Visa?
            get() = activeVisas
                .filter { it.requiresSchengenTracking }
                .minByOrNull { it.expiryDate }

        val hasActiveSchengenVisa: Boolean
            get() = activeVisas.any { it.requiresSchengenTracking }

        val filteredItems: List<HomeItem>
            get() = when (selectedTab) {
                HomeTab.ALL -> {
                    val items = mutableListOf<HomeItem>()
                    items.addAll(activeVisas.map { HomeItem.VisaItem(it) })
                    items.addAll(trips.map { HomeItem.TripItem(it, isExempt(it)) })
                    items.sortedByDescending {
                        when (it) {
                            is HomeItem.VisaItem -> it.visa.expiryDate
                            is HomeItem.TripItem -> it.trip.startDate
                        }
                    }
                }
                HomeTab.VISAS -> activeVisas.map { HomeItem.VisaItem(it) }
                HomeTab.TRIPS -> trips.map { HomeItem.TripItem(it, isExempt(it)) }
            }

        val ongoingTrips: List<Trip>
            get() = trips.filter { it.isOngoing }

        val plannedTrips: List<Trip>
            get() = trips.filter { it.isFuture }

        val pastTrips: List<Trip>
            get() = trips.filter { it.isPast }

        private fun isExempt(trip: Trip): Boolean {
            return trip.segments.any { segment ->
                segment.type == ru.nikfirs.android.traveltracker.core.domain.model.SegmentType.STAY &&
                        segment.country in exemptCountries
            }
        }
    }

    sealed class Action : MviAction {
        data object LoadData : Action()
        data object RefreshData : Action()
        data class SelectTab(val tab: HomeTab) : Action()
        data object NavigateToAddVisa : Action()
        data object NavigateToAddTrip : Action()
        data class NavigateToEditVisa(val visa: Visa) : Action()
        data class NavigateToEditTrip(val trip: Trip) : Action()
        data class DeleteTrip(val trip: Trip) : Action()
        data class DeleteVisa(val visa: Visa) : Action()
        data object DismissError : Action()
        data object RetryLoadData : Action()
    }

    sealed class Effect : MviEffect {
        data object NavigateToAddVisa : Effect()
        data object NavigateToAddTrip : Effect()
        data class NavigateToEditVisa(val visaId: Long) : Effect()
        data class NavigateToEditTrip(val tripId: Long) : Effect()
        data class ShowMessage(val message: CustomString) : Effect()
    }
}