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
import ru.nikfirs.android.traveltracker.core.ui.ui.extension.getLanguage
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
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
        val localizedContext = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            updateResources(newBase)
        } else newBase

        super.attachBaseContext(localizedContext)
    }

    private fun updateResources(context: Context?): Context? {
        context ?: return null
        datastoreHelper = DatastoreHelper(context)
        val languageCode = runBlocking {
            datastoreHelper.languageFlow.firstOrNull()
                ?: resources.configuration.locales[0].language.getLanguage()
        }
        val newLocale = Locale(languageCode)
        Locale.setDefault(newLocale)

        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(newLocale)

        return context.createConfigurationContext(config)
    }
}
