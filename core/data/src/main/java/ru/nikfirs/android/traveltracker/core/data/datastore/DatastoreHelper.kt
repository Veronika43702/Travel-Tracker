package ru.nikfirs.android.traveltracker.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.nikfirs.android.traveltracker.core.data.model.DATE_FORMAT
import ru.nikfirs.android.traveltracker.core.data.model.LANGUAGE
import ru.nikfirs.android.traveltracker.core.data.model.SETTINGS
import ru.nikfirs.android.traveltracker.core.data.model.THEME
import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.model.AppThemeModel
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS)

@Singleton
class DatastoreHelper @Inject constructor(val context: Context) {
    /**
     * returns language string ("en", "ru") from dataStore
     */
    private val languageKey = stringPreferencesKey(LANGUAGE)
    val languageFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[languageKey]
        }

    /**
     * saves language string ("en", "ru") settings in dataStore
     */
    suspend fun setLanguage(value: String) {
        context.dataStore.edit { settings ->
            settings[languageKey] = value
        }
    }

    /**
     * returns app theme from dataStore
     */
    private val themeKey = stringPreferencesKey(THEME)
    val themeFlow: Flow<AppThemeModel> = context.dataStore.data
        .map { preferences ->
            AppThemeModel.fromString(preferences[themeKey])
        }

    /**
     * saves app theme settings in dataStore
     */
    suspend fun setTheme(theme: AppThemeModel) {
        context.dataStore.edit { settings ->
            settings[themeKey] = theme.name
        }
    }

    /**
    * returns app date format from dataStore
    */
    private val dateFormatKey = stringPreferencesKey(DATE_FORMAT)
    val dateFormatFlow: Flow<AppDateFormatModel> = context.dataStore.data
        .map { preferences ->
            AppDateFormatModel.fromString(preferences[dateFormatKey])
        }

    /**
     * saves app date format settings in dataStore
     */
    suspend fun setDateFormat(dateFormat: AppDateFormatModel) {
        context.dataStore.edit { settings ->
            settings[dateFormatKey] = dateFormat.name
        }
    }
}