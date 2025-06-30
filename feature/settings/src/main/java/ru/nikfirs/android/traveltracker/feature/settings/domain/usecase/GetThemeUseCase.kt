package ru.nikfirs.android.traveltracker.feature.settings.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.model.AppThemeModel
import ru.nikfirs.android.traveltracker.feature.settings.domain.Repository
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val repository: Repository
) {
    operator fun invoke(): Flow<AppThemeModel> {
        return repository.getTheme()
    }
}