package ru.nikfirs.android.traveltracker.feature.settings.domain.usecase

import ru.nikfirs.android.traveltracker.core.domain.model.AppThemeModel
import ru.nikfirs.android.traveltracker.feature.settings.domain.Repository
import javax.inject.Inject

class SaveThemeUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(theme: AppThemeModel) {
        repository.setTheme(theme)
    }
}