package ru.nikfirs.android.traveltracker.feature.settings.ui.settings

import android.os.Build
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import ru.nikfirs.android.traveltracker.core.domain.model.AppThemeModel
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.R as uiR
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launchIO
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.GetLanguageUseCase
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.GetThemeUseCase
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.SaveLanguageUseCase
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.SaveThemeUseCase
import ru.nikfirs.android.traveltracker.feature.settings.ui.settings.SettingsContract.Action
import ru.nikfirs.android.traveltracker.feature.settings.ui.settings.SettingsContract.Effect
import ru.nikfirs.android.traveltracker.feature.settings.ui.settings.SettingsContract.State
import javax.inject.Inject

@HiltViewModel
class SettingsViewmodel @Inject constructor(
    private val getLanguageUseCase: GetLanguageUseCase,
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val getThemeUseCase: GetThemeUseCase,
    private val saveThemeUseCase: SaveThemeUseCase,
) : ViewModel<Action, Effect, State>() {
    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            Action.LoadData -> loadData()
            is Action.SetError -> setError(action.error)

            // Language actions
            is Action.ShowLanguageDialog -> showLanguageDialog(action.value)
            is Action.SelectLanguageInDialog -> selectLanguageInDialog(action.language)
            Action.ApplySelectedLanguage -> applySelectedLanguage()

            // Theme actions
            is Action.ShowThemeDialog -> showThemeDialog(action.value)
            is Action.SelectThemeInDialog -> selectThemeInDialog(action.theme)
        }
    }

    private fun loadData() {
        launchIO {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    getLanguageUseCase.invoke().collectLatest { data ->
                        setState {
                            it.copy(
                                language = data,
                                selectedLanguageInDialog = data
                            )
                        }
                    }
                }

                getThemeUseCase.invoke().collectLatest { theme ->
                    setState { it.copy(selectedThemeInDialog = theme) }
                }
            } catch (e: Exception) {
                setError(CustomString.resource(uiR.string.error_loading_data))
                Log.e(null, "loadData", e)
            }
        }
    }


    private fun showLanguageDialog(value: Boolean) {
        setState { it.copy(showLanguageDialog = value) }
    }

    private fun selectLanguageInDialog(language: String) {
        setState { it.copy(selectedLanguageInDialog = language) }
    }

    private fun applySelectedLanguage() {
        launchIO {
            try {
                currentState.selectedLanguageInDialog?.let {
                    saveLanguageUseCase.invoke(it)
                }
                setState { it.copy(showLanguageDialog = false) }
            } catch (e: Exception) {
                setError(CustomString.resource(uiR.string.error_updating_data))
                Log.e(null, "applySelectedLanguage", e)
            }
        }
    }

    private fun showThemeDialog(value: Boolean) {
        setState { it.copy(showThemeDialog = value) }
    }

    private fun selectThemeInDialog(theme: AppThemeModel) {
        launchIO {
            try {
                saveThemeUseCase.invoke(theme)
            } catch (e: Exception) {
                setError(CustomString.resource(uiR.string.error_updating_data))
                Log.e(null, "applySelectedTheme", e)
            }
        }
    }

    private fun setError(error: CustomString?) {
        setState { it.copy(error = error) }
    }
}