package ru.nikfirs.android.traveltracker.feature.settings.ui.components

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.nikfirs.android.traveltracker.core.ui.R as uiR
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.ui.component.DarkENScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.RadioButtonRow
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.settings.R
import java.util.Locale

data class LanguageOption(
    val code: String,
    val name: String
)

@Composable
fun LanguageSelectionDialog(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseContext = LocalContext.current
    val localizedContext = remember(selectedLanguage) {
        baseContext.createLocalizedContext(selectedLanguage)
    }
    LanguageSelectionDialogContent(
        selectedLanguage,
        onLanguageSelected,
        onApply,
        onDismiss,
        modifier,
        localizedContext,
    )
}

@Composable
fun LanguageSelectionDialogContent(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    localizedContext: Context = LocalContext.current,
) {
    val languages = listOf(
        LanguageOption("ru", localizedContext.getString(R.string.settings_language_russian)),
        LanguageOption("en", localizedContext.getString(R.string.settings_language_english))
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
                    text = localizedContext.getString(R.string.settings_language_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = localizedContext.getString(R.string.settings_language_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Languages
                languages.forEach { language ->
                    RadioButtonRow(
                        text = language.name,
                        selected = selectedLanguage == language.code,
                        onClick = { onLanguageSelected(language.code) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomButton(
                        text = localizedContext.getString(uiR.string.action_cancel),
                        onClick = onDismiss,
                        secondaryBtn = true,
                        modifier = Modifier.weight(1f),
                        smallButton = true
                    )

                    CustomButton(
                        text = localizedContext.getString(uiR.string.action_save),
                        onClick = onApply,
                        modifier = Modifier.weight(1f),
                        smallButton = true
                    )
                }
            }
        }
    }
}

fun Context.createLocalizedContext(languageCode: String): Context {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(resources.configuration)
    config.setLocale(locale)

    return createConfigurationContext(config)
}

// Previews
@LightRUScreenPreview
@DarkENScreenPreview
@Composable
private fun LanguageSelectionDialogLightPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LanguageSelectionDialogContent(
                selectedLanguage = "en",
                onLanguageSelected = {},
                onApply = {},
                onDismiss = {}
            )
        }
    }
}