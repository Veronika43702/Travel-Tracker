package ru.nikfirs.android.traveltracker.feature.settings.ui.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.model.AppThemeModel
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.ui.component.DarkENScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.ui.extension.getLanguage
import ru.nikfirs.android.traveltracker.core.ui.ui.model.IconType
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.settings.R
import ru.nikfirs.android.traveltracker.feature.settings.ui.components.DateFormatSelectionDialog
import ru.nikfirs.android.traveltracker.feature.settings.ui.components.LanguageSelectionDialog
import ru.nikfirs.android.traveltracker.feature.settings.ui.components.ThemeSelectionDialog
import ru.nikfirs.android.traveltracker.feature.settings.ui.settings.SettingsContract.Action
import ru.nikfirs.android.traveltracker.feature.settings.ui.settings.SettingsContract.State
import java.time.LocalDate

@Composable
fun SettingsScreen(
    navigateRoute: (Any) -> Unit,
    viewModel: SettingsViewmodel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.setAction(Action.LoadData)
    }
    Screen(
        topTitle = stringResource(R.string.settings_title),
        bottomNavRouteRoute = BottomNavBarRoute.Settings,
        navigateRoute = navigateRoute,
    ) { paddingValues ->
        SettingsContent(
            state = state,
            onAction = viewModel::setAction,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
    ErrorDialog(
        message = state.error,
        onDismiss = { viewModel.setAction(Action.SetError()) },
    )
}

@Composable
fun SettingsContent(
    state: State,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val languageFromSystem = configuration.locales[0].language

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection(
                title = stringResource(R.string.settings_category_general),
                items = {
                    SettingsItem(
                        icon = IconType.DrawableRes(R.drawable.ic_language),
                        title = stringResource(R.string.settings_language_title),
                        currentValue = getCurrentLanguageText(
                            state.language,
                            languageFromSystem
                        ),
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                openSystemLanguageSettings(context)
                            } else {
                                onAction(Action.ShowLanguageDialog())
                            }
                        }
                    )
                }
            )
        }

        item {
            SettingsSection(
                title = stringResource(R.string.settings_category_appearance),
                items = {
                    SettingsItem(
                        icon = IconType.DrawableRes(R.drawable.ic_pallete),
                        title = stringResource(R.string.settings_theme_title),
                        currentValue = getCurrentThemeText(state.selectedThemeInDialog),
                        onClick = { onAction(Action.ShowThemeDialog()) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // date format
                    SettingsItem(
                        icon = IconType.DrawableRes(R.drawable.ic_schedule),
                        title = stringResource(R.string.settings_date_format_title),
                        currentValue = getCurrentDateFormatDisplay(state.selectedDateFormatInDialog),
                        onClick = { onAction(Action.ShowDateFormatDialog()) }
                    )
                }
            )
        }


        item {
            SettingsSection(
                title = stringResource(R.string.settings_category_about),
                items = {
                    SettingsItem(
                        icon = IconType.VectorIcon(Icons.Outlined.Info),
                        title = stringResource(R.string.settings_app_version),
                        currentValue = state.appVersion,
                        onClick = null,
                        showChevron = false
                    )
                }
            )
        }
    }

    if (state.showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguage = state.selectedLanguageInDialog ?: languageFromSystem.getLanguage(),
            onLanguageSelected = { language -> onAction(Action.SelectLanguageInDialog(language)) },
            onApply = { onAction(Action.ApplySelectedLanguage) },
            onDismiss = { onAction(Action.ShowLanguageDialog(false)) }
        )
    }

    if (state.showThemeDialog) {
        ThemeSelectionDialog(
            selectedTheme = state.selectedThemeInDialog,
            onThemeSelected = { theme -> onAction(Action.SelectThemeInDialog(theme)) },
            onApply = { onAction(Action.ShowThemeDialog(false)) },
            onDismiss = { onAction(Action.ShowThemeDialog(false)) }
        )
    }

    if (state.showDateFormatDialog) {
        DateFormatSelectionDialog(
            selectedDateFormat = state.selectedDateFormatInDialog,
            onDateFormatSelected = { format -> onAction(Action.SelectDateFormatInDialog(format)) },
            onApply = { onAction(Action.ShowDateFormatDialog(false)) },
            onDismiss = { onAction(Action.ShowDateFormatDialog(false)) }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    items: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            items()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: IconType,
    title: String,
    currentValue: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    showChevron: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { mod ->
                if (onClick != null) {
                    mod.clickableOnce { onClick() }
                } else {
                    mod
                }
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (icon) {
            is IconType.DrawableRes -> {
                Icon(
                    painter = painterResource(icon.resId),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            is IconType.VectorIcon -> {
                Icon(
                    imageVector = icon.imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = currentValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (showChevron && onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.padding(start = 16.dp).size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun getCurrentLanguageText(
    language: String?,
    languageFromSystem: String,
): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        when (languageFromSystem) {
            "ru" -> stringResource(R.string.settings_language_russian)
            else -> stringResource(R.string.settings_language_english)
        }
    } else {
        when (language) {
            "ru" -> stringResource(R.string.settings_language_russian)
            else -> stringResource(R.string.settings_language_english)
        }
    }
}


@Composable
private fun getCurrentThemeText(theme: AppThemeModel): String {
    return when (theme) {
        AppThemeModel.SYSTEM -> stringResource(R.string.settings_theme_system)
        AppThemeModel.LIGHT -> stringResource(R.string.settings_theme_light)
        AppThemeModel.DARK -> stringResource(R.string.settings_theme_dark)
    }
}

@Composable
private fun getCurrentDateFormatDisplay(format: AppDateFormatModel): String {
    val currentDate = LocalDate.now()
    val formatName = when (format) {
        AppDateFormatModel.DD_MM_YYYY_DOTS -> stringResource(R.string.settings_date_format_dd_mm_yyyy_dots)
        AppDateFormatModel.DD_MM_YYYY_SLASHES -> stringResource(R.string.settings_date_format_dd_mm_yyyy_slashes)
        AppDateFormatModel.MM_DD_YYYY -> stringResource(R.string.settings_date_format_mm_dd_yyyy)
        AppDateFormatModel.YYYY_MM_DD -> stringResource(R.string.settings_date_format_yyyy_mm_dd)
        AppDateFormatModel.DD_MMM_YYYY -> stringResource(R.string.settings_date_format_dd_mmm_yyyy)
    }
    val example = currentDate.format(format.getFormatter())
    return "$formatName ($example)"
}

private fun openSystemLanguageSettings(context: android.content.Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}

// Previews
@LightRUScreenPreview
@Composable
private fun SettingsContentLightPreview() {
    AppTheme {
        SettingsContent(
            state = State(),
            onAction = {},
        )
    }
}

@DarkENScreenPreview
@Composable
private fun SettingsContentDarkPreview() {
    AppTheme {
        SettingsContent(
            state = State(),
            onAction = {}
        )
    }
}