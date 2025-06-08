package ru.nikfirs.android.traveltracker.feature.home.ui.visa

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import ru.nikfirs.android.traveltracker.core.domain.repository.VisaRepository
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.feature.home.ui.visa.VisaContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.visa.VisaContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.visa.VisaContract.Effect
import javax.inject.Inject

@HiltViewModel
class VisaViewModel @Inject constructor(
    private val visaRepository: VisaRepository,
) : ViewModel<Action, Effect, State>() {

    init {
        setAction(Action.LoadData)
    }

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.LoadData -> loadData()
            is Action.DeleteVisa -> deleteVisa(action.visa)
        }
    }

    private fun loadData() {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            try {
                visaRepository.getAllVisas().collectLatest { visas ->
                    setState {
                        it.copy(
                            isLoading = false,
                            visas = visas,
                        )
                    }
                }
            } catch (e: Exception) {
                setError(CustomString.text(e.message))
            }
        }
    }

    private fun deleteVisa(visa: Visa) {
        launch {
            try {
                visaRepository.deleteVisa(visa)
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