package ru.nikfirs.android.traveltracker.feature.settings.domain.usecase

import ru.nikfirs.android.traveltracker.feature.settings.domain.Repository
import javax.inject.Inject

class SaveLanguageUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(language: String) {
        repository.setLanguage(language)
    }
}