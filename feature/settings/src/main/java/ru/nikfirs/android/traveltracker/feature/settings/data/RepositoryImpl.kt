package ru.nikfirs.android.traveltracker.feature.settings.data

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.data.datastore.DatastoreHelper
import ru.nikfirs.android.traveltracker.feature.settings.domain.Repository
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val datastoreHelper: DatastoreHelper,
) : Repository {
    override suspend fun setLanguage(language: String) {
        datastoreHelper.setLanguage(language)
    }

    override fun getLanguage(): Flow<String> {
        return datastoreHelper.languageFlow
    }
}