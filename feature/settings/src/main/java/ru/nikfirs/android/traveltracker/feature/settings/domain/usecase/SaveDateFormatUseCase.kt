package ru.nikfirs.android.traveltracker.feature.settings.domain.usecase

import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.feature.settings.domain.Repository
import javax.inject.Inject

class SaveDateFormatUseCase @Inject constructor(
    private val repository: Repository
) {
    suspend operator fun invoke(dateFormat: AppDateFormatModel) {
        repository.setDateFormat(dateFormat)
    }
}