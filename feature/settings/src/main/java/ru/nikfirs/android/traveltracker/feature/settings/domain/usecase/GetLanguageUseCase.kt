package ru.nikfirs.android.traveltracker.feature.settings.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.feature.settings.domain.Repository
import javax.inject.Inject

class GetLanguageUseCase @Inject constructor(
    private val repository: Repository
) {
    operator fun invoke(): Flow<String> {
        return repository.getLanguage()
    }
}