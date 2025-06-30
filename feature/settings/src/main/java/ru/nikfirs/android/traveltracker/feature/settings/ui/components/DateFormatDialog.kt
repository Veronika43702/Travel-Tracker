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
import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.ui.component.DarkENScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.RadioButtonRow
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.settings.R
import java.time.LocalDate
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

data class DateFormatOption(
    val format: AppDateFormatModel,
    val name: String,
    val example: String
)

@Composable
fun DateFormatSelectionDialog(
    selectedDateFormat: AppDateFormatModel,
    onDateFormatSelected: (AppDateFormatModel) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDate = LocalDate.now()
    val dateFormatOptions = listOf(
        DateFormatOption(
            format = AppDateFormatModel.DD_MM_YYYY_DOTS,
            name = stringResource(R.string.settings_date_format_dd_mm_yyyy_dots),
            example = currentDate.format(AppDateFormatModel.DD_MM_YYYY_DOTS.getFormatter())
        ),
        DateFormatOption(
            format = AppDateFormatModel.DD_MM_YYYY_SLASHES,
            name = stringResource(R.string.settings_date_format_dd_mm_yyyy_slashes),
            example = currentDate.format(AppDateFormatModel.DD_MM_YYYY_SLASHES.getFormatter())
        ),
        DateFormatOption(
            format = AppDateFormatModel.MM_DD_YYYY,
            name = stringResource(R.string.settings_date_format_mm_dd_yyyy),
            example = currentDate.format(AppDateFormatModel.MM_DD_YYYY.getFormatter())
        ),
        DateFormatOption(
            format = AppDateFormatModel.YYYY_MM_DD,
            name = stringResource(R.string.settings_date_format_yyyy_mm_dd),
            example = currentDate.format(AppDateFormatModel.YYYY_MM_DD.getFormatter())
        ),
        DateFormatOption(
            format = AppDateFormatModel.DD_MMM_YYYY,
            name = stringResource(R.string.settings_date_format_dd_mmm_yyyy),
            example = currentDate.format(AppDateFormatModel.DD_MMM_YYYY.getFormatter())
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(MaterialTheme.shapes.large),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_date_format_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    dateFormatOptions.forEach { option ->
                        RadioButtonRow(
                            text = option.name,
                            subText = option.example,
                            selected = selectedDateFormat == option.format,
                            onClick = {
                                onDateFormatSelected(option.format)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)

                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    CustomButton(
                        text = stringResource(uiR.string.action_save),
                        onClick = onApply,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@LightRUScreenPreview
@Composable
private fun DateFormatSelectionDialogLightPreview() {
    AppTheme {
        DateFormatSelectionDialog(
            selectedDateFormat = AppDateFormatModel.DD_MM_YYYY_DOTS,
            onDateFormatSelected = { },
            onApply = { },
            onDismiss = { }
        )
    }
}

@DarkENScreenPreview
@Composable
private fun DateFormatSelectionDialogDarkPreview() {
    AppTheme {
        DateFormatSelectionDialog(
            selectedDateFormat = AppDateFormatModel.MM_DD_YYYY,
            onDateFormatSelected = { },
            onApply = { },
            onDismiss = { }
        )
    }
}