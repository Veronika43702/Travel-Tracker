package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.tripDetails

import dagger.hilt.android.lifecycle.HiltViewModel
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.feature.home.R
import ru.nikfirs.android.traveltracker.feature.home.ui.model.TripSegmentUi
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.DeleteTripUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.trip.GetTripByIdUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.visa.GetVisaByIdUseCase
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.tripDetails.TripDetailsContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.tripDetails.TripDetailsContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.tripDetails.TripDetailsContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.utils.getTripSegmentColorByIndex
import javax.inject.Inject
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@HiltViewModel
class TripDetailsViewModel @Inject constructor(
    private val getTripByIdUseCase: GetTripByIdUseCase,
    private val getVisaByIdUseCase: GetVisaByIdUseCase,
    private val deleteTripUseCase: DeleteTripUseCase,
) : ViewModel<Action, Effect, State>() {

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.LoadData -> loadTrip(action.tripId)
            Action.ShowDeleteDialog -> showDeleteDialog()
            Action.Delete -> deleteTrip()
            is Action.SetError -> setError(action.error)
            Action.HideDialog -> hideDialog()
            Action.ChangeExpandSegment -> {
                setState { it.copy(expandSegments = !currentState.expandSegments) }
            }
        }
    }

    private fun loadTrip(tripId: Long) {
        launch {
            setState { it.copy(isLoading = true) }
            try {
                val trip = getTripByIdUseCase.invoke(tripId)
                if (trip == null) {
                    setError(CustomString.resource(R.string.error_trip_not_found))
                    return@launch
                }

                val visa = trip.visaId?.let { getVisaByIdUseCase.invoke(it) }
                val segments = trip.segments
                    .sortedWith(compareBy({ it.startDate }, { it.endDate }))
                    .mapIndexed { index, segment ->
                        TripSegmentUi(
                            country = segment.country,
                            startDate = segment.startDate,
                            endDate = segment.endDate,
                            isExempt = segment.isExempt,
                            cities = segment.cities,
                            color = getTripSegmentColorByIndex(index)
                        )
                    }

                setState {
                    it.copy(
                        isLoading = false,
                        trip = trip,
                        visa = visa,
                        segmentsForView = segments,
                    )
                }
            } catch (e: Exception) {
                setError(CustomString.resource(R.string.error_loading_trip))
            }
        }
    }

    private fun showDeleteDialog() {
        setState {
            it.copy(
                dialogText = CustomString.resource(uiR.string.trip_delete_dialog)
            )
        }
    }

    private fun deleteTrip() {
        setState { it.copy(isLoading = true, dialogText = null) }
        launch {
            try {
                currentState.trip?.let { trip ->
                    deleteTripUseCase.invoke(trip)
                } ?: setError(CustomString.resource(R.string.error_trip_not_found))
                setEffect { Effect.NavigateBack }
            } catch (e: Exception) {
                setError(CustomString.resource(R.string.error_deleting_trip))
            }
        }
    }

    private fun hideDialog() {
        setState { it.copy(dialogText = null) }
    }

    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error) }
    }
}