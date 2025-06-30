package ru.nikfirs.android.traveltracker.feature.settings.ui.settings

import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.model.AppThemeModel
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState

sealed class SettingsContract {
    data class State(
        val language: String? = null,
        val showLanguageDialog: Boolean = false,
        val selectedLanguageInDialog: String? = null,

        val showThemeDialog: Boolean = false,
        val selectedThemeInDialog: AppThemeModel = AppThemeModel.SYSTEM,

        val showDateFormatDialog: Boolean = false,
        val selectedDateFormatInDialog: AppDateFormatModel = AppDateFormatModel.getDefault(),

        val error: CustomString? = null,
    ) : MviState

    sealed class Action : MviAction {
        data object LoadData : Action()

        data class ShowLanguageDialog(val value: Boolean = true) : Action()
        data class SelectLanguageInDialog(val language: String) : Action()
        data object ApplySelectedLanguage : Action()

        data class ShowThemeDialog(val value: Boolean = true) : Action()
        data class SelectThemeInDialog(val theme: AppThemeModel) : Action()

        data class ShowDateFormatDialog(val value: Boolean = true) : Action()
        data class SelectDateFormatInDialog(val format: AppDateFormatModel) : Action()

        data class SetError(val error: CustomString? = null) : Action()
    }

    sealed class Effect : MviEffect
}