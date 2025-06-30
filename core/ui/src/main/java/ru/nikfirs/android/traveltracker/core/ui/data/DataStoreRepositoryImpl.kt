package ru.nikfirs.android.traveltracker.core.ui.data

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.data.datastore.DatastoreHelper
import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.repository.DataStoreRepository
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(
    private val datastoreHelper: DatastoreHelper,
) : DataStoreRepository {

    override fun getDateFormat(): Flow<AppDateFormatModel> {
        return datastoreHelper.dateFormatFlow
    }

}