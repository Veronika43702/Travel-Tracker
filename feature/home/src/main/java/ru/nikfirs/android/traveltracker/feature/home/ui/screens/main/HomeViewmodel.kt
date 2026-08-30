package ru.nikfirs.android.traveltracker.feature.home.ui.screens.main

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import ru.nikfirs.android.traveltracker.core.domain.PERIOD_DAYS
import ru.nikfirs.android.traveltracker.core.domain.coroutines.DispatcherProvider
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.CalculateDaysInPeriodUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.dataStore.GetDateFormatUseCase
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.feature.home.R
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.GetHomeDataUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.DeleteTripUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.DeleteVisaUseCase
import ru.nikfirs.android.traveltracker.feature.home.ui.model.HomeTab
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.HomeContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.HomeContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.HomeContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.utils.HomeAction
import ru.nikfirs.android.traveltracker.feature.home.ui.utils.HomeActionModel
import java.time.LocalDate
import javax.inject.Inject
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val calculateDaysInPeriodUseCase: CalculateDaysInPeriodUseCase,
    private val deleteTripUseCase: DeleteTripUseCase,
    private val deleteVisaUseCase: DeleteVisaUseCase,
    private val getDateFormatUseCase: GetDateFormatUseCase,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel<Action, Effect, State>(dispatcherProvider) {

    init {
        setAction(Action.LoadData)
    }

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.LoadData -> loadData()
            Action.UpdateDaysCalculation -> updateDaysCalculation()
            is Action.SelectTab -> selectTab(action.tab)
            is Action.NavigateToAddVisa -> navigateToAddVisa()
            is Action.NavigateToAddTrip -> navigateToAddTrip()
            is Action.NavigateToVisaDetails -> navigateToVisaDetails(action.visaId)
            is Action.NavigateToTripDetails -> navigateToTripDetails(action.tripId)
            is Action.NavigateToEditVisa -> navigateToEditVisa(action.visaId)
            is Action.NavigateToEditTrip -> navigateToEditTrip(action.tripId)
            is Action.DeleteTrip -> deleteTrip(action.trip)
            is Action.DeleteVisa -> deleteVisa(action.visa)
            is Action.SetError -> setError(action.error)
            is Action.RetryLoadData -> loadData()
            is Action.ShowDeleteTripDialog -> showDeleteTripDialog(action.trip)
            is Action.ShowDeleteVisaDialog -> showDeleteVisaDialog(action.visa)
            Action.HideDialog -> hideDialog()
        }
    }

    private fun loadData() {
        setState { it.copy(isLoading = true, error = null) }

        launchIO {
            try {
                getDateFormatUseCase.invoke().collectLatest { dateFormat ->
                    setState { it.copy(dateFormatter = dateFormat.getFormatter()) }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "loadData, date format", e)
            }
        }

        launchIO {
            try {
                getHomeDataUseCase(LocalDate.now().minusDays(PERIOD_DAYS.toLong()))
                    .catch { e ->
                        setError(CustomString.Resource(uiR.string.error_loading_data))
                        Log.e(null, "loadData, flow", e)
                    }
                    .collectLatest { homeData ->
                        val tripsWithLimitInfo = addLimitOverInfoToTrips(homeData.allTrips)

                        setState {
                            it.copy(
                                visas = homeData.allVisas,
                                trips = tripsWithLimitInfo,
                                isLoading = false,
                                error = null
                            )
                        }

                        updateDaysCalculation()
                    }
            } catch (e: Exception) {
                setError(CustomString.Resource(uiR.string.error_loading_data))
                Log.e(null, "loadData", e)
            }
        }
    }

    private suspend fun addLimitOverInfoToTrips(trips: List<Trip>): List<Trip> {
        return trips.map { trip ->
            trip.copy(hasOverLimitDay = checkTripHasOverLimitDay(trip))
        }
    }

    private suspend fun checkTripHasOverLimitDay(trip: Trip): Boolean {
        val startDate = trip.startDate ?: return false
        val endDate = trip.endDate ?: return false

        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            try {
                val calculation = calculateDaysInPeriodUseCase.invoke(periodEnd = currentDate)

                if (calculation.isOverLimit) {
                    return true
                }
            } catch (e: Exception) {
                Log.e(null, "checkTripHasOverLimitDay", e)
            }
            currentDate = currentDate.plusDays(1)
        }

        return false
    }

    private fun updateDaysCalculation() {
        launch {
            try {
                val calculation = calculateDaysInPeriodUseCase(
                    periodEnd = LocalDate.now(),
                )
                setState { it.copy(daysCalculation = calculation) }
            } catch (e: Exception) {
                setError(CustomString.internal())
                Log.e(null, "updateDaysCalculation", e)
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

    private fun navigateToVisaDetails(visaId: Long) {
        setEffect { Effect.NavigateToVisaDetails(visaId) }
    }

    private fun navigateToTripDetails(tripId: Long) {
        setEffect { Effect.NavigateToTripDetails(tripId) }
    }

    private fun navigateToEditVisa(visaId: Long) {
        setEffect { Effect.NavigateToEditVisa(visaId) }
    }

    private fun navigateToEditTrip(tripId: Long) {
        setEffect { Effect.NavigateToEditTrip(tripId) }
    }

    private fun deleteTrip(trip: Trip) {
        launch {
            try {
                deleteTripUseCase(trip)
                setEffect {
                    Effect.ShowMessage(CustomString.resource(R.string.home_trip_deleted_successfully))
                }
            } catch (e: Exception) {
                setError(CustomString.resource(R.string.home_error_trip_deleting))
                Log.e(null, "deleteTrip", e)
            }
        }
    }

    private fun deleteVisa(visa: Visa) {
        launch {
            try {
                deleteVisaUseCase(visa)
                setEffect {
                    Effect.ShowMessage(CustomString.resource(R.string.home_visa_deleted_successfully))
                }
            } catch (e: Exception) {
                setError(CustomString.resource(R.string.home_error_visa_deleting))
                Log.e(null, "deleteVisa", e)
            }
        }
    }

    private fun showDeleteVisaDialog(visa: Visa) {
        setState {
            it.copy(
                dialogText = CustomString.resource(R.string.home_visa_dialog_delete),
                action = HomeActionModel(
                    action = HomeAction.DELETE_VISA,
                    visa = visa,
                )
            )
        }
    }

    private fun showDeleteTripDialog(trip: Trip) {
        setState {
            it.copy(
                dialogText = CustomString.resource(R.string.home_trip_dialog_delete),
                action = HomeActionModel(
                    action = HomeAction.DELETE_TRIP,
                    trip = trip,
                )
            )
        }
    }

    private fun hideDialog() {
        setState { it.copy(dialogText = null, action = null) }
    }

    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error) }
    }
}