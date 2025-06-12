package ru.nikfirs.android.traveltracker.feature.home.ui.screens.main

import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.DaysCalculation
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeItem
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeTab
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.utils.HomeAction
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.utils.HomeActionModel
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.utils.VisaAction
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails.VisaDetailsContract.Action

sealed class HomeContract {
    data class State(
        val isLoading: Boolean = true,
        val selectedTab: HomeTab = HomeTab.ALL,
        val visas: List<Visa> = emptyList(),
        val trips: List<Trip> = emptyList(),
        val daysCalculation: DaysCalculation? = null,
        val exemptCountries: Set<String> = emptySet(),
        val error: CustomString? = null,
        val dialogText: CustomString? = null,
        val action: HomeActionModel? = null,
    ) : MviState {

        val activeVisas: List<Visa>
            get() = visas.filter { it.isActive }

        val currentSchengenVisa: Visa?
            get() = activeVisas
                .filter { it.validVisa }
                .minByOrNull { it.expiryDate }


        val filteredItems: List<HomeItem>
            get() = when (selectedTab) {
                HomeTab.ALL -> {
                    val items = mutableListOf<HomeItem>()
                    items.addAll(visas.map { HomeItem.VisaItem(it) })
                    items.addAll(trips.map { HomeItem.TripItem(it, isExempt(it)) })
                    items.sortedByDescending {
                        when (it) {
                            is HomeItem.VisaItem -> it.visa.expiryDate
                            is HomeItem.TripItem -> it.trip.startDate
                        }
                    }
                }

                HomeTab.VISAS -> visas.map { HomeItem.VisaItem(it) }
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
        data class NavigateToVisaDetails(val visaId: Long) : Action()
        data class NavigateToEditVisa(val visa: Visa) : Action()
        data class NavigateToEditTrip(val trip: Trip) : Action()
        data class DeleteTrip(val trip: Trip) : Action()
        data class DeleteVisa(val visa: Visa) : Action()
        data class SetError(val error: CustomString? = null) : Action()
        data object RetryLoadData : Action()
        data class ShowDeleteVisaDialog(val visa: Visa) : Action()
        data class ShowDeleteTripDialog(val trip: Trip) : Action()
        data object HideDialog : Action()
    }

    sealed class Effect : MviEffect {
        data object NavigateToAddVisa : Effect()
        data object NavigateToAddTrip : Effect()
        data class NavigateToVisaDetails(val visaId: Long) : Effect()
        data class NavigateToEditVisa(val visaId: Long) : Effect()
        data class NavigateToEditTrip(val tripId: Long) : Effect()
        data class ShowMessage(val message: CustomString) : Effect()
    }
}