package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.tripDetails

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.dataStore.GetDateFormatUseCase
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.feature.home.R
import ru.nikfirs.android.traveltracker.feature.home.ui.model.TripSegmentUi
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.DeleteTripUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.trip.GetTripByIdUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.visa.GetVisaByIdUseCase
import ru.nikfirs.android.traveltracker.core.ui.mvi.launchIO
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
    private val getDateFormatUseCase: GetDateFormatUseCase,
) : ViewModel<Action, Effect, State>() {

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.LoadData -> loadTrip(action.tripId)
            Action.ShowDeleteDialog -> showDeleteDialog()
            Action.Delete -> deleteTrip()
            is Action.SetError -> setError(action.error)
            Action.HideDialog -> hideDialog()
            Action.ChangeExpandSegment -> expandSegments()
        }
    }

    private fun loadTrip(tripId: Long) {
        setState { it.copy(isLoading = true) }
        launchIO {
            try {
                getDateFormatUseCase.invoke().collectLatest { dateFormat ->
                    setState { it.copy(dateFormatter = dateFormat.getFormatter()) }
                }
            } catch (exception: Exception) {
                Log.e("TripDetailsViewModel", "loadTrip, date format", exception)
            }
        }

        launchIO {
            try {
                val trip = getTripByIdUseCase.invoke(tripId)
                if (trip == null) {
                    setError(CustomString.resource(R.string.home_error_trip_not_found))
                    return@launchIO
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
                setError(CustomString.resource(R.string.home_error_trip_loading))
                Log.e(null, "loadTrip", e)
            }
        }
    }

    private fun showDeleteDialog() {
        setState {
            it.copy(
                dialogText = CustomString.resource(R.string.home_trip_dialog_delete)
            )
        }
    }

    private fun deleteTrip() {
        setState { it.copy(isLoading = true, dialogText = null) }
        launch {
            try {
                currentState.trip?.let { trip ->
                    deleteTripUseCase.invoke(trip)
                } ?: setError(CustomString.resource(R.string.home_error_trip_not_found))
                setEffect { Effect.NavigateBack }
            } catch (e: Exception) {
                setError(CustomString.resource(R.string.home_error_trip_deleting))
                Log.e(null, "deleteTrip", e)
            }
        }
    }

    private fun hideDialog() {
        setState { it.copy(dialogText = null) }
    }

    private fun expandSegments() {
        setState { it.copy(expandSegments = !currentState.expandSegments) }
    }

    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error) }
    }
}