package ru.nikfirs.android.traveltracker.feature.settings.data

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.data.datastore.DatastoreHelper
import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.model.AppThemeModel
import ru.nikfirs.android.traveltracker.feature.settings.domain.Repository
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val datastoreHelper: DatastoreHelper,
) : Repository {
    override suspend fun setLanguage(language: String) {
        datastoreHelper.setLanguage(language)
    }

    override fun getLanguage(): Flow<String?> {
        return datastoreHelper.languageFlow
    }

    override suspend fun setTheme(theme: AppThemeModel) {
        datastoreHelper.setTheme(theme)
    }

    override fun getTheme(): Flow<AppThemeModel> {
        return datastoreHelper.themeFlow
    }

    override suspend fun setDateFormat(dateFormat: AppDateFormatModel) {
        datastoreHelper.setDateFormat(dateFormat)
    }
}