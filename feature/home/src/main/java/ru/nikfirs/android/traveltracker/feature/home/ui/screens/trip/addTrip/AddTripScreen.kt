package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.nikfirs.android.traveltracker.core.domain.model.SegmentType
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.domain.model.VisaEntries
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.component.*
import ru.nikfirs.android.traveltracker.core.ui.extension.asString
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.State
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddTripScreen(
    navigateBack: () -> Unit,
    viewModel: AddTripViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val verticalScroll = rememberScrollState()

    LaunchedEffectResolver(flow = viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateBack -> navigateBack()
            is Effect.ScrollUp -> scope.launch { verticalScroll.scrollTo(0) }
            is Effect.ShowMessage -> {
                // TODO: Показать snackbar
            }

            is Effect.OpenSegmentEditor -> {
                // TODO: Открыть редактор сегмента
            }
        }
    }

    Screen(
        topTitle = stringResource(R.string.add_trip_title),
        navigateBack = navigateBack,
    ) {
        AddTripScreenContent(
            state = state,
            onAction = viewModel::setAction,
            verticalScroll = verticalScroll
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    initialDate: LocalDate,
    blockedDates: Set<LocalDate> = emptySet()
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * 24 * 60 * 60 * 1000,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = LocalDate.ofEpochDay(utcTimeMillis / (24 * 60 * 60 * 1000))
                return !blockedDates.contains(date)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTripScreenContent(
    state: State,
    onAction: (Action) -> Unit,
    verticalScroll: ScrollState = rememberScrollState(),
) {
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

        // Выбор визы (перенесен в начало)
        Text(
            text = stringResource(R.string.visa_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        ExposedDropdownMenuBox(
            expanded = state.isVisaDropdownExpanded,
            onExpandedChange = { onAction(Action.SetVisaDropdownExpanded(it)) }
        ) {
            CustomTextFieldButton(
                text = when {
                    state.selectedVisa != null -> {
                        val visa = state.selectedVisa
                        val typeText = when (visa.visaType) {
                            VisaCategory.TYPE_C -> stringResource(R.string.visa_type_c_short)
                            VisaCategory.TYPE_D -> stringResource(R.string.visa_type_d_short)
                            VisaCategory.RESIDENCE_PERMIT -> stringResource(R.string.visa_type_residence_short)
                        }
                        "$typeText (${visa.visaNumber}) ${visa.country}"
                    }

                    state.availableVisas.isEmpty() -> stringResource(R.string.no_available_visas)
                    else -> ""
                },
                required = true,
                label = stringResource(R.string.select_visa),
                trailingIconImage = Icons.Default.KeyboardArrowDown,
                isError = state.validationErrors.visaError != null,
                supportingText = state.validationErrors.visaError?.let { it.asString() },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable)
            )

            ExposedDropdownMenu(
                expanded = state.isVisaDropdownExpanded,
                onDismissRequest = { onAction(Action.SetVisaDropdownExpanded(false)) }
            ) {
                if (state.availableVisas.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.no_available_visas)) },
                        onClick = { },
                        enabled = false
                    )
                } else {
                    state.availableVisas.forEach { visa ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    val typeText = when (visa.visaType) {
                                        VisaCategory.TYPE_C -> stringResource(R.string.visa_type_c_short)
                                        VisaCategory.TYPE_D -> stringResource(R.string.visa_type_d_short)
                                        VisaCategory.RESIDENCE_PERMIT -> stringResource(R.string.visa_type_residence_short)
                                    }
                                    Text("$typeText (${visa.visaNumber}) ${visa.country}")
                                    Text(
                                        text = stringResource(
                                            R.string.visa_validity_period,
                                            visa.startDate.format(state.dateFormatter),
                                            visa.expiryDate.format(state.dateFormatter)
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onAction(Action.UpdateSelectedVisa(visa))
                            }
                        )
                    }
                }
            }
        }

        // Информация о выбранной визе
        state.selectedVisa?.let { visa ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = stringResource(
                        R.string.selected_visa_validity,
                        visa.startDate.format(state.dateFormatter),
                        visa.expiryDate.format(state.dateFormatter)
                    ),
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Даты поездки
        Text(
            text = stringResource(R.string.trip_dates_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Дата начала
            CustomTextFieldButton(
                text = state.startDate.format(state.dateFormatter),
                label = stringResource(R.string.start_date),
                required = true,
                enabled = state.hasSelectedVisa,
                trailingIcon = R.drawable.ic_calendar_today,
                onClick = {
                    focusManager.clearFocus()
                    onAction(Action.ShowStartDatePicker)
                },
                isError = state.validationErrors.startDateError != null,
                supportingText = state.validationErrors.startDateError?.let { it.asString() },
                modifier = Modifier.fillMaxWidth()
            )

            // Дата окончания
            CustomTextFieldButton(
                text = state.endDate.format(state.dateFormatter),
                label = stringResource(R.string.end_date),
                required = true,
                enabled = state.hasSelectedVisa,
                trailingIcon = R.drawable.ic_calendar_today,
                onClick = {
                    focusManager.clearFocus()
                    onAction(Action.ShowEndDatePicker)
                },
                isError = state.validationErrors.endDateError != null,
                supportingText = state.validationErrors.endDateError?.let { it.asString() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Информация о длительности и оставшихся днях
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    state.daysAvailableAtEnd?.isOverLimit == true ->
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)

                    state.daysAvailableAtEnd?.isNearLimit == true ->
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)

                    else -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.trip_duration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.days_count, state.totalDuration),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        if (state.hasExemptSegments) {
                            Text(
                                text = stringResource(
                                    R.string.countable_days_count,
                                    state.countableDuration
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                state.daysAvailableAtStart?.let { startInfo ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.days_available_at_start),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = startInfo.displayText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                state.daysAvailableAtEnd?.let { endInfo ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.days_available_at_end),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = endInfo.displayText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                endInfo.isOverLimit -> MaterialTheme.colorScheme.error
                                endInfo.isNearLimit -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }

                if (state.validationErrors.daysLimitError != null) {
                    Text(
                        text = state.validationErrors.daysLimitError.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Цель поездки
        Text(
            text = stringResource(R.string.trip_purpose_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        ExposedDropdownMenuBox(
            expanded = state.isPurposeDropdownExpanded,
            onExpandedChange = { onAction(Action.SetPurposeDropdownExpanded(it)) }
        ) {
            CustomTextFieldButton(
                text = when (state.purpose) {
                    TripPurpose.TOURISM -> stringResource(R.string.purpose_tourism)
                    TripPurpose.BUSINESS -> stringResource(R.string.purpose_business)
                    TripPurpose.FAMILY -> stringResource(R.string.purpose_family)
                    TripPurpose.MEDICAL -> stringResource(R.string.purpose_medical)
                    TripPurpose.EDUCATION -> stringResource(R.string.purpose_education)
                    TripPurpose.OTHER -> stringResource(R.string.purpose_other)
                },
                required = true,
                enabled = state.hasSelectedVisa,
                label = stringResource(R.string.select_purpose),
                trailingIconImage = Icons.Default.KeyboardArrowDown,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable)
            )

            ExposedDropdownMenu(
                expanded = state.isPurposeDropdownExpanded,
                onDismissRequest = { onAction(Action.SetPurposeDropdownExpanded(false)) }
            ) {
                TripPurpose.entries.forEach { purpose ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                when (purpose) {
                                    TripPurpose.TOURISM -> stringResource(R.string.purpose_tourism)
                                    TripPurpose.BUSINESS -> stringResource(R.string.purpose_business)
                                    TripPurpose.FAMILY -> stringResource(R.string.purpose_family)
                                    TripPurpose.MEDICAL -> stringResource(R.string.purpose_medical)
                                    TripPurpose.EDUCATION -> stringResource(R.string.purpose_education)
                                    TripPurpose.OTHER -> stringResource(R.string.purpose_other)
                                }
                            )
                        },
                        onClick = {
                            onAction(Action.UpdatePurpose(purpose))
                        }
                    )
                }
            }
        }

        // Сегменты поездки
        Text(
            text = stringResource(R.string.trip_segments_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        if (state.validationErrors.segmentsError != null) {
            Text(
                text = state.validationErrors.segmentsError.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Отображение сегментов
        state.segments.forEachIndexed { index, segment ->
            TripSegmentCard(
                segment = segment,
                onEdit = {
                    // TODO: Открыть редактор сегмента
                },
                onDelete = {
                    onAction(Action.RemoveSegment(index))
                }
            )
        }

        // Кнопка добавления сегмента
        OutlinedButton(
            onClick = {
                // TODO: Открыть редактор для добавления сегмента
                focusManager.clearFocus()
            },
            enabled = state.hasSelectedVisa,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_segment))
        }

        // Заметки
        CustomTextField(
            value = state.notes,
            onValueChange = { onAction(Action.UpdateNotes(it)) },
            enabled = state.hasSelectedVisa,
            label = stringResource(R.string.notes_optional),
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Кнопка сохранения
        CustomButton(
            text = stringResource(R.string.action_save),
            onClick = { onAction(Action.SaveTrip) },
            enabled = state.hasSelectedVisa,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )
    }

    // Диалоги и индикаторы загрузки
    ErrorDialog(
        message = state.error,
        onDismiss = { onAction(Action.DismissError) }
    )

    FullScreenLoadingIndicator(state.isLoading)

    // Date Picker для начальной даты
    if (state.showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                onAction(Action.UpdateStartDate(selectedDate))
            },
            onDismiss = { onAction(Action.HideStartDatePicker) },
            initialDate = state.startDate,
            blockedDates = state.blockedDatesForStart
        )
    }

    // Date Picker для конечной даты
    if (state.showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                onAction(Action.UpdateEndDate(selectedDate))
            },
            onDismiss = { onAction(Action.HideEndDatePicker) },
            initialDate = state.endDate,
            blockedDates = state.blockedDatesForEnd
        )
    }
}

@Composable
private fun TripSegmentCard(
    segment: AddTripContract.TripSegmentUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (segment.isExempt) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = segment.country,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (segment.isExempt) {
                        StatusChip(
                            text = stringResource(R.string.exempt_badge),
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (segment.type == SegmentType.TRANSIT) {
                        StatusChip(
                            text = stringResource(R.string.transit),
                            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_horiz),
                            contentDescription = stringResource(R.string.action_edit)
                        )
                    }
                }
            }

            Text(
                text = "${segment.startDate.format(dateFormatter)} - ${
                    segment.endDate.format(
                        dateFormatter
                    )
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (segment.cities.isNotEmpty()) {
                    Text(
                        text = segment.cities.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                StatusChip(
                    text = stringResource(R.string.days_count, segment.duration),
                    backgroundColor = if (segment.isExempt) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = if (segment.isExempt) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTripScreenPreview() {
    AppTheme {
        AddTripScreenContent(
            state = State(
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(7),
                selectedVisa = Visa(
                    id = 1,
                    visaNumber = "C123456789",
                    visaType = VisaCategory.TYPE_C,
                    country = "DE",
                    startDate = LocalDate.now().minusMonths(3),
                    expiryDate = LocalDate.now().plusMonths(3),
                    durationOfStay = 90,
                    entries = VisaEntries.MULTI
                ),
                availableVisas = listOf(
                    Visa(
                        id = 1,
                        visaNumber = "C123456789",
                        visaType = VisaCategory.TYPE_C,
                        country = "DE",
                        startDate = LocalDate.now().minusMonths(3),
                        expiryDate = LocalDate.now().plusMonths(3),
                        durationOfStay = 90,
                        entries = VisaEntries.MULTI
                    )
                ),
                segments = listOf(
                    AddTripContract.TripSegmentUi(
                        country = "Germany",
                        startDate = LocalDate.now(),
                        endDate = LocalDate.now().plusDays(3),
                        cities = listOf("Berlin", "Munich"),
                        isExempt = false
                    ),
                    AddTripContract.TripSegmentUi(
                        country = "Poland",
                        startDate = LocalDate.now().plusDays(3),
                        endDate = LocalDate.now().plusDays(7),
                        cities = listOf("Warsaw")
                    )
                ),
                daysAvailableAtStart = AddTripContract.DaysAvailableInfo(
                    used = 60,
                    total = 90,
                    remaining = 30
                ),
                daysAvailableAtEnd = AddTripContract.DaysAvailableInfo(
                    used = 64,
                    total = 90,
                    remaining = 26
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, locale = "EN")
@Composable
private fun AddTripScreenNoVisaPreview() {
    AppTheme {
        AddTripScreenContent(
            state = State(
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(14),
                availableVisas = emptyList(),
                daysAvailableAtStart = AddTripContract.DaysAvailableInfo(
                    used = 0,
                    total = 90,
                    remaining = 90
                ),
                daysAvailableAtEnd = AddTripContract.DaysAvailableInfo(
                    used = 0,
                    total = 90,
                    remaining = 90
                )
            ),
            onAction = {}
        )
    }
}