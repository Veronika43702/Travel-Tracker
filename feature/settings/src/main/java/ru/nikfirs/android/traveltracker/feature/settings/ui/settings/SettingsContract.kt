package ru.nikfirs.android.traveltracker.feature.settings.ui.settings

import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState

sealed class SettingsContract {
    data class State(
        val loading: Boolean = false,

        val language: String? = null,
        val showLanguageDialog: Boolean = false,
        val selectedLanguageInDialog: String = "ru",

        val error: CustomString? = null,
    ) : MviState

    sealed class Action : MviAction {
        data object LoadData : Action()

        data class ShowLanguageDialog(val value: Boolean = true) : Action()
        data class SelectLanguageInDialog(val language: String) : Action()
        data object ApplySelectedLanguage : Action()

        data class SetError(val error: CustomString? = null) : Action()
    }

    sealed class Effect : MviEffect
}