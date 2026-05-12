package ru.nikfirs.android.traveltracker

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.nikfirs.android.traveltracker.core.data.datastore.DatastoreHelper
import ru.nikfirs.android.traveltracker.core.domain.model.AppThemeModel
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var datastoreHelper: DatastoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            observeLanguageChanges()
        }

        enableEdgeToEdge()
        setContent {
            ThemedApp {
                val navController = rememberNavController()
                NavigationHost(navController = navController)
            }
        }
    }

    @Composable
    private fun ThemedApp(content: @Composable () -> Unit) {
        val themeSettings by datastoreHelper.themeFlow.collectAsState(initial = AppThemeModel.SYSTEM)
        val systemInDarkTheme = isSystemInDarkTheme()

        val darkTheme = when (themeSettings) {
            AppThemeModel.SYSTEM -> systemInDarkTheme
            AppThemeModel.LIGHT -> false
            AppThemeModel.DARK -> true
        }

        AppTheme(
            darkTheme = darkTheme,
            content = content
        )
    }

    private fun observeLanguageChanges() {
        lifecycleScope.launch {
            datastoreHelper.languageFlow.collectLatest { language ->
                // checking new language is set
                // (excludes recreation when activity is firstly created)
                val currentLocale = resources.configuration.locales[0]
                if (language != null && Locale(language).language != currentLocale.language) {
                    recreate()
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val localizedContext = updateResources(newBase)
        super.attachBaseContext(localizedContext)
    }

    private fun updateResources(context: Context?): Context? {
        context ?: return null
        val languageCode = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            updateResourcesForAndroidUntil12(context)
        } else {
            context.resources.configuration.locales[0].language
        }

        val mondayFirstLocale = createMondayFirstLocale(languageCode)
        Locale.setDefault(mondayFirstLocale)

        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(mondayFirstLocale)

        return context.createConfigurationContext(config)
    }


    private fun updateResourcesForAndroidUntil12(context: Context): String {
        datastoreHelper = DatastoreHelper(context)
        return runBlocking {
            datastoreHelper.languageFlow.firstOrNull()
                ?: context.resources.configuration.locales[0].language
        }
    }

    private fun createMondayFirstLocale(languageCode: String): Locale {
        return when (languageCode) {
            "en" -> Locale("en", "GB")
            "ru" -> Locale("ru", "RU")
            else -> {
                val systemLocale = Locale(languageCode)
                if (isFirstDaySunday(systemLocale)) {
                    Locale("en", "GB")
                } else {
                    systemLocale
                }
            }
        }
    }

    private fun isFirstDaySunday(locale: Locale): Boolean {
        val calendar = Calendar.getInstance(locale)
        return calendar.firstDayOfWeek == Calendar.SUNDAY
    }
}
