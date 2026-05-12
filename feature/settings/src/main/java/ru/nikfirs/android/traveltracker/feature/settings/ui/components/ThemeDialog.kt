package ru.nikfirs.android.traveltracker.feature.settings.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.nikfirs.android.traveltracker.core.domain.model.AppThemeModel
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.ui.component.DarkENScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.RadioButtonRow
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.settings.R
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

data class ThemeOption(
    val theme: AppThemeModel,
    val name: String
)

@Composable
fun ThemeSelectionDialog(
    selectedTheme: AppThemeModel,
    onThemeSelected: (AppThemeModel) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeOptions = listOf(
        ThemeOption(AppThemeModel.SYSTEM, stringResource(R.string.settings_theme_system)),
        ThemeOption(AppThemeModel.LIGHT, stringResource(R.string.settings_theme_light)),
        ThemeOption(AppThemeModel.DARK, stringResource(R.string.settings_theme_dark))
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier.clip(MaterialTheme.shapes.medium),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.settings_theme_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.settings_theme_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Theme options
                themeOptions.forEach { themeOption ->
                    RadioButtonRow(
                        text = themeOption.name,
                        selected = selectedTheme == themeOption.theme,
                        onClick = { onThemeSelected(themeOption.theme) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                CustomButton(
                    text = stringResource(uiR.string.action_save),
                    onClick = onApply,
                    modifier = Modifier.fillMaxWidth(),
                    smallButton = true
                )
            }
        }
    }
}

// Previews
@LightRUScreenPreview
@Composable
private fun ThemeSelectionDialogLightPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ThemeSelectionDialog(
                selectedTheme = AppThemeModel.SYSTEM,
                onThemeSelected = {},
                onApply = {},
                onDismiss = {}
            )
        }
    }
}

@DarkENScreenPreview
@Composable
private fun ThemeSelectionDialogDarkPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ThemeSelectionDialog(
                selectedTheme = AppThemeModel.LIGHT,
                onThemeSelected = {},
                onApply = {},
                onDismiss = {}
            )
        }
    }
}