package ru.nikfirs.android.traveltracker.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.SchengenCountries
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.domain.model.VisaEntries
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.component.CustomTextField
import ru.nikfirs.android.traveltracker.core.ui.component.CustomTextFieldButton
import ru.nikfirs.android.traveltracker.core.ui.component.RadioButtonRow
import ru.nikfirs.android.traveltracker.core.ui.extension.asString
import ru.nikfirs.android.traveltracker.core.ui.extension.clickableOnce
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun VisaInfoBox(
    visaNumber: String,
    visaType: VisaCategory,
    selectedCountry: String,
    isCountryDropdownExpanded: Boolean,
    startDate: LocalDate,
    expiryDate: LocalDate,
    durationOfStay: String,
    entries: VisaEntries,
    notes: String,
    visaNumberError: CustomString?,
    countryError: CustomString?,
    startDateError: CustomString?,
    expiryDateError: CustomString?,
    durationError: CustomString?,
    updateVisaNumber: (String) -> Unit,
    updateVisaType: (VisaCategory) -> Unit,
    setCountryDropdownExpanded: (Boolean) -> Unit,
    updateCountry: (String) -> Unit,
    updateStartDate: (LocalDate) -> Unit,
    updateExpiryDate: (LocalDate) -> Unit,
    updateDurationOfStay: (String) -> Unit,
    updateEntries: (VisaEntries) -> Unit,
    updateNotes: (String) -> Unit,
    focusManager: FocusManager,
) {
    val locale = Locale.getDefault().language
    // Visa Number Field
    CustomTextField(
        value = visaNumber,
        onValueChange = updateVisaNumber,
        label = stringResource(R.string.visa_number),
        modifier = Modifier.fillMaxWidth(),
        required = true,
        isError = visaNumberError != null,
        supportingText = visaNumberError?.let { it.asString() },
    )

    // Visa Type Selection
    VisaTypeSelection(
        selectedType = visaType,
        onTypeSelected = updateVisaType,
        clearFocus = { focusManager.clearFocus() }
    )

    // Country Selection
    CountryDropdown(
        selectedCountry = selectedCountry,
        expanded = isCountryDropdownExpanded,
        onExpandedChange = setCountryDropdownExpanded,
        onCountrySelected = updateCountry,
        locale = locale,
        isError = countryError != null,
        errorText = countryError?.let { it.asString() }
    )

    // Issue Date
    DatePickerField(
        label = stringResource(R.string.issue_date),
        date = startDate,
        onDateSelected = updateStartDate,
        isError = startDateError != null,
        errorText = startDateError?.asString(),
        clearFocus = { focusManager.clearFocus() },
    )

    // Expiry Date
    DatePickerField(
        label = stringResource(R.string.expiry_date),
        date = expiryDate,
        onDateSelected = updateExpiryDate,
        isError = expiryDateError != null,
        errorText = expiryDateError?.asString(),
        clearFocus = { focusManager.clearFocus() },
    )

    // Duration of Stay
    if (visaType == VisaCategory.TYPE_C) {
        CustomTextField(
            value = durationOfStay,
            onValueChange = updateDurationOfStay,
            label = stringResource(R.string.duration_of_stay),
            required = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = { Text(stringResource(R.string.days)) },
            isError = durationError != null,
            supportingText = durationError?.asString(),
        )

        // Entries Selection
        EntriesSelection(
            selectedEntries = entries,
            onEntriesSelected = updateEntries,
            clearFocus = { focusManager.clearFocus() }
        )
    }

    // Notes Field
    CustomTextField(
        value = notes,
        onValueChange = updateNotes,
        label = stringResource(R.string.notes_optional),
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5,
    )
}

@Composable
private fun VisaTypeSelection(
    selectedType: VisaCategory,
    onTypeSelected: (VisaCategory) -> Unit,
    clearFocus: () -> Unit,
) {
    Column {
        Text(
            text = "${stringResource(R.string.visa_type)}*",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VisaCategory.entries.forEach { type ->
                RadioButtonRow(
                    text = when (type) {
                        VisaCategory.TYPE_C -> stringResource(R.string.visa_type_c_short)
                        VisaCategory.TYPE_D -> stringResource(R.string.visa_type_d_short)
                        VisaCategory.RESIDENCE_PERMIT -> stringResource(R.string.visa_type_residence_short)
                    },
                    selected = selectedType == type,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onTypeSelected(type)
                        clearFocus()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDropdown(
    selectedCountry: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCountrySelected: (String) -> Unit,
    locale: String,
    isError: Boolean = false,
    errorText: String? = null
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(it) }
    ) {
        CustomTextFieldButton(
            text = SchengenCountries.getCountryByCode(selectedCountry)?.getDisplayName(locale)
                ?: selectedCountry,
            required = true,
            label = stringResource(R.string.country),
            trailingIconImage = Icons.Default.KeyboardArrowDown,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
            isError = isError,
            supportingText = errorText,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            SchengenCountries.countries.forEach { country ->
                DropdownMenuItem(
                    text = { Text(country.getDisplayName(locale)) },
                    onClick = {
                        onCountrySelected(country.code)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun DatePickerField(
    label: String,
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    isError: Boolean = false,
    errorText: String? = null,
    clearFocus: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    Box {

        CustomTextFieldButton(
            text = date.format(dateFormatter),
            onClick = {
                showDatePicker = true
                clearFocus()
            },
            required = true,
            label = label,
            trailingIcon = R.drawable.ic_calendar_today,
            isError = isError,
            supportingText = errorText,
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDateSelected = { selectedDate ->
                    onDateSelected(selectedDate)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false },
                initialDate = date
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Red)
                .clickableOnce { showDatePicker = true },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    initialDate: LocalDate
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * 24 * 60 * 60 * 1000
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun EntriesSelection(
    selectedEntries: VisaEntries,
    onEntriesSelected: (VisaEntries) -> Unit,
    clearFocus: () -> Unit,
) {
    Column {
        Text(
            text = "${stringResource(R.string.entries_type)}*",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column {
            VisaEntries.entries.forEach { entries ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large)
                        .background(Color.Transparent, MaterialTheme.shapes.large)
                        .clickableOnce {
                            onEntriesSelected(entries)
                            clearFocus()
                        }
                ) {
                    RadioButtonRow(
                        text = when (entries) {
                            VisaEntries.SINGLE -> stringResource(R.string.entries_single)
                            VisaEntries.DOUBLE -> stringResource(R.string.entries_double)
                            VisaEntries.MULTI -> stringResource(R.string.entries_multi)
                        },
                        selected = selectedEntries == entries,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onEntriesSelected(entries)
                            clearFocus()
                        }
                    )
                }
            }
        }
    }
}