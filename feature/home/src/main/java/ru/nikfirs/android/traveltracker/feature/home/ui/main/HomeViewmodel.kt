package ru.nikfirs.android.traveltracker.feature.home.ui.main

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import ru.nikfirs.android.traveltracker.core.domain.repository.VisaRepository
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeTab
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeContract.Effect
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val visaRepository: VisaRepository,
    private val tripRepository: TripRepository
) : ViewModel<Action, Effect, State>() {

    init {
        setAction(Action.LoadData)
    }

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.LoadData -> loadData()
            is Action.RefreshData -> loadData()
            is Action.SelectTab -> selectTab(action.tab)
            is Action.NavigateToAddVisa -> navigateToAddVisa()
            is Action.NavigateToAddTrip -> navigateToAddTrip()
            is Action.NavigateToEditVisa -> navigateToEditVisa(action.visa)
            is Action.NavigateToEditTrip -> navigateToEditTrip(action.trip)
            is Action.DismissError -> setError(null)
            is Action.RetryLoadData -> loadData()
        }
    }

    private fun loadData() {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            try {
                combine(
                    visaRepository.getAllVisas(),
                    tripRepository.getAllTrips()
                ) { visas, trips ->
                    Pair(visas, trips)
                }.collect { (visas, trips) ->
                    val daysCalculation = tripRepository.calculateDaysInPeriod(LocalDate.now())

                    setState {
                        it.copy(
                            isLoading = false,
                            visas = visas,
                            trips = trips,
                            daysCalculation = daysCalculation,
                        )
                    }
                }
            } catch (e: Exception) {
                setError(CustomString.text(e.message))
            }
        }
    }

    private fun selectTab(tab: HomeTab) {
        setState { it.copy(selectedTab = tab) }
    }

    private fun navigateToAddVisa() {
        setEffect { Effect.NavigateToAddVisa }
    }

    private fun navigateToAddTrip() {
        setEffect { Effect.NavigateToAddTrip }
    }

    private fun navigateToEditVisa(visa: Visa) {
        setEffect { Effect.NavigateToEditVisa(visa.id) }
    }

    private fun navigateToEditTrip(trip: Trip) {
        setEffect { Effect.NavigateToEditTrip(trip.id) }
    }

    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error) }
    }
}