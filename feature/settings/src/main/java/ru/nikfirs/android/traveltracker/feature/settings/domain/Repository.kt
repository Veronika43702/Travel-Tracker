package ru.nikfirs.android.traveltracker.feature.settings.domain

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.model.AppThemeModel

interface Repository {
    suspend fun setLanguage(language: String)
    fun getLanguage(): Flow<String?>

    suspend fun setTheme(theme: AppThemeModel)
    fun getTheme(): Flow<AppThemeModel>

    suspend fun setDateFormat(dateFormat: AppDateFormatModel)
}