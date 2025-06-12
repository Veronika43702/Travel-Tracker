package ru.nikfirs.android.traveltracker.feature.home.ui.visa.addVisa

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.nikfirs.android.traveltracker.core.domain.model.SchengenCountries
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.domain.model.VisaEntries
import ru.nikfirs.android.traveltracker.core.domain.model.asString
import ru.nikfirs.android.traveltracker.core.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.component.CustomTextField
import ru.nikfirs.android.traveltracker.core.ui.component.CustomTextFieldButton
import ru.nikfirs.android.traveltracker.core.ui.component.DarkENScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.component.RadioButtonRow
import ru.nikfirs.android.traveltracker.core.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.extension.asString
import ru.nikfirs.android.traveltracker.core.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.home.ui.visa.addVisa.AddVisaContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.visa.addVisa.AddVisaContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.visa.addVisa.AddVisaContract.State
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@Composable
fun AddVisaScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddVisaViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val verticalScroll = rememberScrollState()
    LaunchedEffectResolver(flow = viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateBack -> onNavigateBack()
            is Effect.ShowMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = effect.message.asString(context)
                    )
                }
            }

            Effect.ScrollUp -> scope.launch { verticalScroll.scrollTo(0) }
        }
    }

    Screen(
        topTitle = stringResource(uiR.string.add_visa_title),
        navigateBack = onNavigateBack,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        AddVisaScreenContent(
            state = state,
            onAction = viewModel::setAction,
            verticalScroll = verticalScroll
        )
    }

    ErrorDialog(
        message = state.error,
        onDismiss = { viewModel.setAction(Action.DismissError) }
    )
}

@Composable
private fun AddVisaScreenContent(
    state: State,
    onAction: (Action) -> Unit,
    verticalScroll: ScrollState = rememberScrollState(),
) {
    val locale = Locale.getDefault().language
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(verticalScroll)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Show loading indicator
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Visa Number Field
        CustomTextField(
            value = state.visaNumber,
            onValueChange = { onAction(Action.UpdateVisaNumber(it)) },
            label = stringResource(uiR.string.visa_number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            required = true,
            isError = state.validationErrors.visaNumberError != null,
            supportingText = state.validationErrors.visaNumberError?.let { it.asString() },
        )

        // Visa Type Selection
        VisaTypeSelection(
            selectedType = state.visaType,
            onTypeSelected = { onAction(Action.UpdateVisaType(it)) },
            enabled = !state.isLoading,
            clearFocus = { focusManager.clearFocus() }
        )

        // Country Selection
        CountryDropdown(
            selectedCountry = state.selectedCountry,
            expanded = state.isCountryDropdownExpanded,
            onExpandedChange = {
                onAction(Action.SetCountryDropdownExpanded(it))
            },
            onCountrySelected = { onAction(Action.UpdateCountry(it)) },
            locale = locale,
            enabled = !state.isLoading,
            isError = state.validationErrors.countryError != null,
            errorText = state.validationErrors.countryError?.let { it.asString() }
        )

        // Issue Date
        DatePickerField(
            label = stringResource(uiR.string.issue_date),
            date = state.startDate,
            onDateSelected = { onAction(Action.UpdateStartDate(it)) },
            enabled = !state.isLoading,
            isError = state.validationErrors.issueDateError != null,
            errorText = state.validationErrors.issueDateError?.asString(),
            clearFocus = { focusManager.clearFocus() },
        )

        // Expiry Date
        DatePickerField(
            label = stringResource(uiR.string.expiry_date),
            date = state.expiryDate,
            onDateSelected = { onAction(Action.UpdateExpiryDate(it)) },
            enabled = !state.isLoading,
            isError = state.validationErrors.expiryDateError != null,
            errorText = state.validationErrors.expiryDateError?.asString(),
            clearFocus = { focusManager.clearFocus() },
        )

        // Duration of Stay
        if (state.visaType == VisaCategory.TYPE_C) {
            CustomTextField(
                value = state.durationOfStay,
                onValueChange = { onAction(Action.UpdateDurationOfStay(it)) },
                label = stringResource(uiR.string.duration_of_stay),
                required = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text(stringResource(uiR.string.days)) },
                enabled = !state.isLoading,
                isError = state.validationErrors.durationError != null,
                supportingText = state.validationErrors.durationError?.asString(),
            )

            // Entries Selection
            EntriesSelection(
                selectedEntries = state.entries,
                onEntriesSelected = { onAction(Action.UpdateEntries(it)) },
                enabled = !state.isLoading,
                clearFocus = { focusManager.clearFocus() }
            )
        }

        // Notes Field
        CustomTextField(
            value = state.notes,
            onValueChange = { onAction(Action.UpdateNotes(it)) },
            label = stringResource(uiR.string.notes_optional),
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            enabled = !state.isLoading,
        )

        Spacer(Modifier.weight(1f))
        CustomButton(
            text = stringResource(uiR.string.action_save),
            onClick = { onAction(Action.SaveVisa) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun VisaTypeSelection(
    selectedType: VisaCategory,
    onTypeSelected: (VisaCategory) -> Unit,
    clearFocus: () -> Unit,
    enabled: Boolean = true,
) {
    Column {
        Text(
            text = "${stringResource(uiR.string.visa_type)}*",
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
                        VisaCategory.TYPE_C -> stringResource(uiR.string.visa_type_c_short)
                        VisaCategory.TYPE_D -> stringResource(uiR.string.visa_type_d_short)
                        VisaCategory.RESIDENCE_PERMIT -> stringResource(uiR.string.visa_type_residence_short)
                    },
                    selected = selectedType == type,
                    enabled = enabled,
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
    enabled: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (enabled) {
                onExpandedChange(it)
            }
        }
    ) {
        CustomTextFieldButton(
            text = SchengenCountries.getCountryByCode(selectedCountry)?.getDisplayName(locale)
                ?: selectedCountry,
            enabled = enabled,
            required = true,
            label = stringResource(uiR.string.country),
            trailingIconImage = Icons.Default.KeyboardArrowDown,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled),
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
    enabled: Boolean = true,
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
            enabled = enabled,
            required = true,
            label = label,
            trailingIcon = uiR.drawable.ic_calendar_today,
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

    DatePickerDialog(
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
                Text(stringResource(uiR.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(uiR.string.action_cancel))
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
    enabled: Boolean = true,
    clearFocus: () -> Unit,
) {
    Column {
        Text(
            text = "${stringResource(uiR.string.entries_type)}*",
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
                        .clickableOnce(enabled) {
                            onEntriesSelected(entries)
                            clearFocus()
                        }
                ) {
                    RadioButtonRow(
                        text = when (entries) {
                            VisaEntries.SINGLE -> stringResource(uiR.string.entries_single)
                            VisaEntries.DOUBLE -> stringResource(uiR.string.entries_double)
                            VisaEntries.MULTI -> stringResource(uiR.string.entries_multi)
                        },
                        selected = selectedEntries == entries,
                        enabled = enabled,
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

@LightRUScreenPreview
@DarkENScreenPreview
@Composable
private fun AddVisaScreenPreview1() {
    AppTheme {
        AddVisaScreenContent(
            state = State(visaType = VisaCategory.TYPE_C),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddVisaScreenPreview2() {
    AppTheme {
        AddVisaScreenContent(
            state = State(visaType = VisaCategory.TYPE_D),
            onAction = {},
        )
    }
}