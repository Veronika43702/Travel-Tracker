package ru.nikfirs.android.traveltracker.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel

interface DataStoreRepository {
    fun getDateFormat(): Flow<AppDateFormatModel>
}