package ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails

import dagger.hilt.android.lifecycle.HiltViewModel
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.R as uiR
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.DeactivateVisaByIdUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.DeleteVisaUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.GetVisaByIdUseCase
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.utils.VisaAction
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails.VisaDetailsContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails.VisaDetailsContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails.VisaDetailsContract.State
import javax.inject.Inject

@HiltViewModel
class VisaDetailsViewModel @Inject constructor(
    private val getVisaByIdUseCase: GetVisaByIdUseCase,
    private val deactivateVisaByIdUseCase: DeactivateVisaByIdUseCase,
    private val deleteVisaUseCase: DeleteVisaUseCase,
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
                visa?.let { setState { it.copy(isLoading = false, visa = visa) } } ?: setError(
                    CustomString.resource(uiR.string.error_visa_not_found)
                )
            } catch (e: Exception) {
                setError(CustomString.resource(uiR.string.error_loading_data))
            }
        }
    }

    private fun showAnnulDialog() {
        setState {
            it.copy(
                dialogText = CustomString.resource(uiR.string.visa_annul_dialog),
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
                } ?: setError(CustomString.resource(uiR.string.error_visa_not_found))
            } catch (e: Exception) {
                setError(CustomString.resource(uiR.string.error_updating_data))
            }
        }
    }

    private fun showDeleteDialog() {
        setState {
            it.copy(
                dialogText = CustomString.resource(uiR.string.visa_delete_dialog),
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
                } ?: setError(CustomString.resource(uiR.string.error_visa_not_found))
                setEffect { Effect.NavigateBack }
            } catch (e: Exception) {
                setError(CustomString.resource(uiR.string.error_deleting_data))
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