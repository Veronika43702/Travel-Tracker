package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.nikfirs.android.traveltracker.core.domain.model.SchengenCountries
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.component.CustomTextField
import ru.nikfirs.android.traveltracker.core.ui.component.CustomTextFieldButton
import ru.nikfirs.android.traveltracker.core.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.extension.asString
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentContract.State
import java.time.LocalDate
import java.util.Locale

@Composable
fun AddTripSegmentScreen(
    navigateBack: () -> Unit,
    viewModel: AddTripSegmentViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val verticalScroll = rememberScrollState()

    LaunchedEffectResolver(flow = viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateBack -> navigateBack()
        }
    }

    Screen(
        topTitle = stringResource(
            if (state.isEditMode) R.string.edit_segment_title
            else R.string.add_segment_title
        ),
        navigateBack = navigateBack,
    ) {
        AddTripSegmentContent(
            state = state,
            onAction = viewModel::setAction,
            verticalScroll = verticalScroll
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTripSegmentContent(
    state: State,
    onAction: (Action) -> Unit,
    verticalScroll: ScrollState = rememberScrollState(),
) {
    val focusManager = LocalFocusManager.current
    val locale = Locale.getDefault().language

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

        // Выбор страны
        ExposedDropdownMenuBox(
            expanded = state.isCountryDropdownExpanded,
            onExpandedChange = { onAction(Action.SetCountryDropdownExpanded(it)) }
        ) {
            CustomTextFieldButton(
                text = when {
                    state.country == "TRANSIT" -> stringResource(R.string.segment_transit_option)
                    state.country.isNotBlank() -> {
                        SchengenCountries.getCountryByCode(state.country)?.getDisplayName(locale)
                            ?: state.country
                    }
                    else -> ""
                },
                required = true,
                label = stringResource(R.string.segment_country),
                trailingIconImage = Icons.Default.KeyboardArrowDown,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable),
                isError = state.validationErrors.countryError != null,
                supportingText = state.validationErrors.countryError?.asString(),
            )

            ExposedDropdownMenu(
                expanded = state.isCountryDropdownExpanded,
                onDismissRequest = { onAction(Action.SetCountryDropdownExpanded(false)) }
            ) {
                // Опция "Транзит"
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.segment_transit_option)) },
                    onClick = {
                        onAction(Action.UpdateCountry("TRANSIT"))
                    }
                )

                HorizontalDivider()

                // Страны Шенгена
                SchengenCountries.countries.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country.getDisplayName(locale)) },
                        onClick = {
                            onAction(Action.UpdateCountry(country.code))
                        }
                    )
                }
            }
        }

        // Даты
        Text(
            text = stringResource(R.string.segment_dates),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Дата начала
            CustomTextFieldButton(
                text = state.startDate.format(state.dateFormatter),
                label = stringResource(R.string.start_date),
                required = true,
                trailingIcon = R.drawable.ic_calendar_today,
                onClick = {
                    focusManager.clearFocus()
                    onAction(Action.ShowStartDatePicker)
                },
                isError = state.validationErrors.startDateError != null,
                supportingText = state.validationErrors.startDateError?.asString(),
                modifier = Modifier.fillMaxWidth()
            )

            // Дата окончания
            CustomTextFieldButton(
                text = state.endDate.format(state.dateFormatter),
                label = stringResource(R.string.end_date),
                required = true,
                trailingIcon = R.drawable.ic_calendar_today,
                onClick = {
                    focusManager.clearFocus()
                    onAction(Action.ShowEndDatePicker)
                },
                isError = state.validationErrors.endDateError != null,
                supportingText = state.validationErrors.endDateError?.asString(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Показать ошибку конфликта дат
        if (state.validationErrors.datesRangeError != null) {
            Text(
                text = state.validationErrors.datesRangeError.asString() ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Информация о длительности
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = stringResource(R.string.segment_duration, state.duration),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        // Города (если не транзит)
        if (state.country != "TRANSIT") {
            CustomTextField(
                value = state.cities,
                onValueChange = { onAction(Action.UpdateCities(it)) },
                label = stringResource(R.string.segment_cities),
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Кнопки действий
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            CustomButton(
                text = stringResource(R.string.action_save),
                onClick = { onAction(Action.SaveSegment) },
                modifier = Modifier.fillMaxWidth()
            )

            if (state.isEditMode) {
                CustomButton(
                    text = stringResource(R.string.action_delete),
                    onClick = { onAction(Action.DeleteSegment) },
                    secondaryBtn = true,
                    contentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Date Picker для начальной даты
    if (state.showStartDatePicker) {
        SegmentDatePickerDialog(
            onDateSelected = { selectedDate ->
                onAction(Action.UpdateStartDate(selectedDate))
            },
            onDismiss = { onAction(Action.HideStartDatePicker) },
            initialDate = state.startDate,
            selectableRange = state.tripStartDate..state.tripEndDate,
            selectedSegmentDays = state.selectedSegmentDays
        )
    }

    // Date Picker для конечной даты
    if (state.showEndDatePicker) {
        SegmentDatePickerDialog(
            onDateSelected = { selectedDate ->
                onAction(Action.UpdateEndDate(selectedDate))
            },
            onDismiss = { onAction(Action.HideEndDatePicker) },
            initialDate = state.endDate,
            selectableRange = state.tripStartDate..state.tripEndDate,
            selectedSegmentDays = state.selectedSegmentDays
        )
    }

    ErrorDialog(
        message = state.error,
        onDismiss = { onAction(Action.SetError()) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentDatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    initialDate: LocalDate,
    selectableRange: ClosedRange<LocalDate>,
    selectedSegmentDays: Set<LocalDate>
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * 24 * 60 * 60 * 1000,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = LocalDate.ofEpochDay(utcTimeMillis / (24 * 60 * 60 * 1000))
                return date in selectableRange //&& !selectedSegmentDays.contains(date)
            }
        }
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

@Preview(showBackground = true)
@Composable
private fun AddTripSegmentScreenPreview() {
    AppTheme {
        AddTripSegmentContent(
            state = State(
                tripStartDate = LocalDate.now(),
                tripEndDate = LocalDate.now().plusDays(7),
                country = "DE",
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(3),
                cities = "Berlin, Munich"
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, locale = "EN")
@Composable
private fun EditTripSegmentScreenPreview() {
    AppTheme {
        AddTripSegmentContent(
            state = State(
                tripStartDate = LocalDate.now(),
                tripEndDate = LocalDate.now().plusDays(14),
                country = "TRANSIT",
                startDate = LocalDate.now().plusDays(3),
                endDate = LocalDate.now().plusDays(4),
                isEditMode = true
            ),
            onAction = {}
        )
    }
}