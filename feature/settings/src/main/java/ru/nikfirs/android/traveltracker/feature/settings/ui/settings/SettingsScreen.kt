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
import androidx.compose.material.icons.filled.Info
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
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.ui.component.DarkENScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.ui.model.IconType
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.settings.R
import ru.nikfirs.android.traveltracker.feature.settings.ui.components.LanguageSelectionDialog
import ru.nikfirs.android.traveltracker.feature.settings.ui.settings.SettingsContract.Action
import ru.nikfirs.android.traveltracker.feature.settings.ui.settings.SettingsContract.State

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
                        description = stringResource(R.string.settings_language_description),
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
                        description = stringResource(R.string.settings_theme_description),
                        currentValue = getCurrentThemeText(),
                        onClick = {
                            // TODO: Открыть диалог выбора темы
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Формат даты
                    SettingsItem(
                        icon = IconType.DrawableRes(R.drawable.ic_schedule),
                        title = stringResource(R.string.settings_date_format_title),
                        description = stringResource(R.string.settings_date_format_description),
                        currentValue = getCurrentDateFormatText(),
                        onClick = {
                            // TODO: Открыть диалог выбора формата даты
                        }
                    )
                }
            )
        }


        item {
            SettingsSection(
                title = stringResource(R.string.settings_category_about),
                items = {
                    SettingsItem(
                        icon = IconType.VectorIcon(Icons.Default.Info),
                        title = stringResource(R.string.settings_app_version),
                        description = null,
                        currentValue = stringResource(R.string.settings_app_version_value),
                        onClick = null,
                        showChevron = false
                    )
                }
            )
        }
    }

    if (state.showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguage = state.selectedLanguageInDialog,
            onLanguageSelected = { language -> onAction(Action.SelectLanguageInDialog(language)) },
            onApply = { onAction(Action.ApplySelectedLanguage) },
            onDismiss = { onAction(Action.ShowLanguageDialog(false)) }
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
    description: String?,
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Text(
            text = currentValue,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = if (showChevron) 8.dp else 0.dp)
        )

        if (showChevron && onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
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
private fun getCurrentThemeText(): String {
    // TODO: Получить реальную тему из настроек
    return stringResource(R.string.settings_theme_system) // Заглушка
}

@Composable
private fun getCurrentDateFormatText(): String {
    // TODO: Получить реальный формат даты из настроек
    return stringResource(R.string.settings_date_format_dd_mm_yyyy) // Заглушка
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