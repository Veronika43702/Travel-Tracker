package ru.nikfirs.android.traveltracker.feature.home.ui.main

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeTab
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.CalculateDaysInPeriodUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.DeleteTripUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.DeleteVisaUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.GetHomeDataUseCase
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.main.HomeContract.Effect
import java.time.LocalDate
import javax.inject.Inject
import ru.nikfirs.android.traveltracker.core.ui.R as UiR

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val calculateDaysInPeriodUseCase: CalculateDaysInPeriodUseCase,
    private val deleteTripUseCase: DeleteTripUseCase,
    private val deleteVisaUseCase: DeleteVisaUseCase,
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
            is Action.DeleteTrip -> deleteTrip(action.trip)
            is Action.DeleteVisa -> deleteVisa(action.visa)
            is Action.DismissError -> dismissError()
            is Action.RetryLoadData -> loadData()
        }
    }

    private fun loadData() {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            try {
                getHomeDataUseCase()
                    .catch { exception ->
                        setError(CustomString.text(exception.message))
                    }
                    .collectLatest { homeData ->
                        setState {
                            it.copy(
                                visas = homeData.activeVisas,
                                trips = homeData.allTrips,
                                exemptCountries = homeData.exemptCountries,
                                isLoading = false,
                                error = null
                            )
                        }

                        // Обновляем подсчет дней
                        updateDaysCalculation(homeData.exemptCountries)
                    }
            } catch (e: Exception) {
                setError(CustomString.text(e.message))
            }
        }
    }

    private fun updateDaysCalculation(exemptCountries: Set<String>) {
        launch {
            try {
                val calculation = calculateDaysInPeriodUseCase(
                    periodEnd = LocalDate.now(),
                    exemptCountries = exemptCountries
                )
                setState { it.copy(daysCalculation = calculation) }
            } catch (e: Exception) {
                // Не показываем ошибку подсчета дней, просто логируем
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

    private fun deleteTrip(trip: Trip) {
        launch {
            try {
                deleteTripUseCase(trip)
                setEffect {
                    Effect.ShowMessage(CustomString.resource(UiR.string.trip_deleted_successfully))
                }
            } catch (e: Exception) {
                setError(CustomString.text(e.message))
            }
        }
    }

    private fun deleteVisa(visa: Visa) {
        launch {
            try {
                deleteVisaUseCase(visa)
                setEffect {
                    Effect.ShowMessage(CustomString.resource(UiR.string.visa_deleted_successfully))
                }
            } catch (e: Exception) {
                setError(CustomString.text(e.message))
            }
        }
    }

    private fun dismissError() {
        setState { it.copy(error = null) }
    }

    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error) }
    }
}