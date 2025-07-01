package ru.nikfirs.android.traveltracker.core.ui.domain.usecase.dataStore

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.repository.DataStoreRepository
import javax.inject.Inject

class GetDateFormatUseCase @Inject constructor(
    private val repository: DataStoreRepository
) {
    operator fun invoke(): Flow<AppDateFormatModel> {
        return repository.getDateFormat()
    }
}