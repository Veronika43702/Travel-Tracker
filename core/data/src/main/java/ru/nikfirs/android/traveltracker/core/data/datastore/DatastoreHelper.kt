package ru.nikfirs.android.traveltracker.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.nikfirs.android.traveltracker.core.data.model.LANGUAGE
import ru.nikfirs.android.traveltracker.core.data.model.SETTINGS
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS)

@Singleton
class DatastoreHelper @Inject constructor(val context: Context) {
    /**
     * returns language string ("en", "ru") from dataStore
     */
    private val languageKey = stringPreferencesKey(LANGUAGE)
    val languageFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[languageKey] ?: "en"
        }

    /**
     * saves language string ("en", "ru") settings in dataStore
     */
    suspend fun setLanguage(value: String) {
        context.dataStore.edit { settings ->
            settings[languageKey] = value
        }
    }
}