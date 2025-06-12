package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.TripContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.TripContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.TripContract.Effect
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel<Action, Effect, State>() {

    init {
        setAction(Action.LoadData)
    }

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.LoadData -> loadData()
            is Action.DeleteTrip -> deleteVisa(action.trip)
        }
    }

    private fun loadData() {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            try {
                tripRepository.getAllTrips().collectLatest { trips ->
                    setState {
                        it.copy(
                            isLoading = false,
                            trips = trips,
                        )
                    }
                }
            } catch (e: Exception) {
                setError(CustomString.text(e.message))
            }
        }
    }

    private fun deleteVisa(trip: Trip) {
        launch {
            try {
                tripRepository.deleteTrip(trip)
                setEffect { Effect.DataDeleted }
            } catch (e: Exception) {
                setError(CustomString.text(e.message))
            }
        }
    }


    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error) }
    }
}