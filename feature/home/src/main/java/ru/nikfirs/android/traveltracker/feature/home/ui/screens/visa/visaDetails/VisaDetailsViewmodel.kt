package ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.R as uiR
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.DeactivateVisaByIdUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.DeleteVisaUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.visa.GetVisaByIdUseCase
import ru.nikfirs.android.traveltracker.feature.home.R
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.GetVisaDurationUsedUseCase
import ru.nikfirs.android.traveltracker.feature.home.ui.model.VisaUi
import ru.nikfirs.android.traveltracker.feature.home.ui.utils.VisaAction
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails.VisaDetailsContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails.VisaDetailsContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails.VisaDetailsContract.State
import javax.inject.Inject

@HiltViewModel
class VisaDetailsViewModel @Inject constructor(
    private val getVisaByIdUseCase: GetVisaByIdUseCase,
    private val deactivateVisaByIdUseCase: DeactivateVisaByIdUseCase,
    private val deleteVisaUseCase: DeleteVisaUseCase,
    private val getVisaDurationUsedUseCase: GetVisaDurationUsedUseCase,
) : ViewModel<Action, Effect, State>() {

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.LoadData -> loadVisa(action.visaId)
            Action.ShowAnnulDialog -> showAnnulDialog()
            Action.ShowDeleteDialog -> showDeleteDialog()
            Action.Annul -> annulVisa()
            Action.Delete -> deleteVisa()
            is Action.SetError -> setError(action.error)
            Action.HideDialog -> hideDialog()
        }
    }

    private fun loadVisa(visaId: Long) {
        launch {
            setState { it.copy(isLoading = true) }
            try {
                val visa = getVisaByIdUseCase.invoke(visaId)
                visa?.let {
                    val durationUsed = getVisaDurationUsedUseCase.invoke(visa.id)
                    val durationLeft = visa.durationOfStay - durationUsed
                    setState {
                        it.copy(
                            isLoading = false,
                            visa = visa,
                            daysLeft = durationLeft
                        )
                    }
                } ?: setError(CustomString.resource(R.string.home_error_visa_not_found))
            } catch (e: Exception) {
                setError(CustomString.resource(R.string.home_error_visa_loading))
                Log.e(null, "loadVisa", e)
            }
        }
    }

    private fun showAnnulDialog() {
        setState {
            it.copy(
                dialogText = CustomString.resource(R.string.home_visa_dialog_annul),
                action = VisaAction.ANNUL
            )
        }
    }

    private fun annulVisa() {
        setState { it.copy(isLoading = true, dialogText = null, action = null) }
        launch {
            try {
                currentState.visa?.id?.let { id ->
                    deactivateVisaByIdUseCase.invoke(id)
                    loadVisa(id)
                } ?: setError(CustomString.resource(R.string.home_error_visa_not_found))
            } catch (e: Exception) {
                setError(CustomString.resource(uiR.string.error_updating_data))
                Log.e(null, "annulVisa", e)
            }
        }
    }

    private fun showDeleteDialog() {
        setState {
            it.copy(
                dialogText = CustomString.resource(R.string.home_visa_dialog_delete),
                action = VisaAction.DELETE
            )
        }
    }

    private fun deleteVisa() {
        setState { it.copy(isLoading = true, dialogText = null, action = null) }
        launch {
            setState { it.copy(isLoading = true) }
            try {
                currentState.visa?.let { id ->
                    deleteVisaUseCase.invoke(id)
                } ?: setError(CustomString.resource(R.string.home_error_visa_not_found))
                setEffect { Effect.NavigateBack }
            } catch (e: Exception) {
                setError(CustomString.resource(R.string.home_error_visa_deleting))
                Log.e(null, "deleteVisa", e)
            }
        }
    }

    private fun hideDialog() {
        setState { it.copy(dialogText = null, action = null) }
    }

    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error, action = null) }
    }
}