package ru.nikfirs.android.traveltracker.feature.settings.ui.settings

import android.os.Build
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.model.AppThemeModel
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.dataStore.GetDateFormatUseCase
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.feature.settings.BuildConfig
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.GetLanguageUseCase
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.GetThemeUseCase
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.SaveDateFormatUseCase
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.SaveLanguageUseCase
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.SaveThemeUseCase
import ru.nikfirs.android.traveltracker.feature.settings.ui.settings.SettingsContract.Action
import ru.nikfirs.android.traveltracker.feature.settings.ui.settings.SettingsContract.Effect
import ru.nikfirs.android.traveltracker.feature.settings.ui.settings.SettingsContract.State
import javax.inject.Inject
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@HiltViewModel
class SettingsViewmodel @Inject constructor(
    private val getLanguageUseCase: GetLanguageUseCase,
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val getThemeUseCase: GetThemeUseCase,
    private val saveThemeUseCase: SaveThemeUseCase,
    private val getDateFormatUseCase: GetDateFormatUseCase,
    private val saveDateFormatUseCase: SaveDateFormatUseCase,
) : ViewModel<Action, Effect, State>() {
    override fun createInitialState(): State = State(
        appVersion = BuildConfig.VERSION_NAME
    )

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

            // Date format actions
            is Action.ShowDateFormatDialog -> showDateFormatDialog(action.value)
            is Action.SelectDateFormatInDialog -> selectDateFormatInDialog(action.format)
        }
    }

    private fun loadData() {
        launchIO {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    combine(
                        getLanguageUseCase.invoke(),
                        getThemeUseCase.invoke(),
                        getDateFormatUseCase.invoke()
                    ) { language, theme, dateFormat ->
                        Triple(language, theme, dateFormat)
                    }.collectLatest { (language, theme, dateFormat) ->
                        setState {
                            it.copy(
                                language = language,
                                selectedLanguageInDialog = language,
                                selectedThemeInDialog = theme,
                                selectedDateFormatInDialog = dateFormat
                            )
                        }
                    }
                } else {
                    combine(
                        getThemeUseCase.invoke(),
                        getDateFormatUseCase.invoke()
                    ) { theme, dateFormat ->
                        Pair(theme, dateFormat)
                    }.collectLatest { (theme, dateFormat) ->
                        setState {
                            it.copy(
                                selectedThemeInDialog = theme,
                                selectedDateFormatInDialog = dateFormat
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SettingsViewmodel", "loadData", e)
                setError(CustomString.Resource(uiR.string.error_loading_data))
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
                Log.e(null, "selectThemeInDialog", e)
            }
        }
    }

    private fun showDateFormatDialog(show: Boolean) {
        setState { it.copy(showDateFormatDialog = show) }
    }

    private fun selectDateFormatInDialog(format: AppDateFormatModel) {
        launchIO {
            try {
                saveDateFormatUseCase.invoke(format)
            } catch (e: Exception) {
                Log.e(null, "selectDateFormatInDialog", e)
                setError(CustomString.Resource(uiR.string.error_updating_data))
            }
        }
    }

    private fun setError(error: CustomString?) {
        setState { it.copy(error = error) }
    }
}