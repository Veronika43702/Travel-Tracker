package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.nikfirs.android.traveltracker.core.data.model.TRANSIT
import ru.nikfirs.android.traveltracker.core.domain.model.SchengenCountries
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.domain.model.VisaEntries
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.component.CustomDateRangePicker
import ru.nikfirs.android.traveltracker.core.ui.component.CustomTextField
import ru.nikfirs.android.traveltracker.core.ui.component.CustomTextFieldButton
import ru.nikfirs.android.traveltracker.core.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.component.FullScreenLoadingIndicator
import ru.nikfirs.android.traveltracker.core.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.component.StatusChip
import ru.nikfirs.android.traveltracker.core.ui.extension.asString
import ru.nikfirs.android.traveltracker.core.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.home.domain.model.TripSegmentUi
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTrip.AddTripContract.State
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddTripScreen(
    navigateBack: () -> Unit,
    navigateToTripSegment: () -> Unit,
    viewModel: AddTripViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val verticalScroll = rememberScrollState()
    LaunchedEffect(viewModel.addTripHolder.segmentList) {
        viewModel.setAction(Action.UpdateSegmentList)
    }
    LaunchedEffectResolver(flow = viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateBack -> navigateBack()
            is Effect.ScrollUp -> scope.launch { verticalScroll.scrollTo(0) }
            is Effect.ShowMessage -> {
                // TODO: Показать snackbar
            }

            is Effect.OpenSegmentEditor -> navigateToTripSegment()
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
        // Visa
        Column {
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
                    supportingText = state.validationErrors.visaError?.asString(),
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
            // Visa info
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
        }

        // Trop Dates
        Text(
            text = stringResource(R.string.trip_dates_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        // Trip Dates
        CustomTextFieldButton(
            text = if (state.startDate != null && state.endDate != null) {
                state.startDate.format(state.dateFormatter) +
                        " - " + state.endDate.format(state.dateFormatter)
            } else "",
            label = stringResource(R.string.calendar_select_dates),
            required = true,
            enabled = state.hasSelectedVisa,
            trailingIcon = R.drawable.ic_calendar_today,
            onClick = {
                focusManager.clearFocus()
                onAction(Action.ShowDatePicker(true))
            },
            isError = state.validationErrors.startDateError != null,
            supportingText = state.validationErrors.startDateError.asString(),
            modifier = Modifier.fillMaxWidth()
        )

        // Duration Info
        if (state.hasSelectedVisa) {
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
                            text = state.validationErrors.daysLimitError.asString() ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Trip purpose
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

        // Trip Segments
        Text(
            text = stringResource(R.string.trip_segments_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Segments
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        if (state.validationErrors.segmentsError != null) {
            Text(
                text = state.validationErrors.segmentsError.asString() ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
            state.segments.forEachIndexed { index, segment ->
                TripSegmentCard(
                    segment = segment,
                    onEdit = {
                        onAction(Action.OpenEditSegmentEditor(index))
                    },
                    onDelete = {
                        onAction(Action.RemoveSegment(index))
                    },
                    dateFormatter = state.dateFormatter,
                )
            }
        }

        // Button Add Segment
        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                onAction(Action.OpenAddSegmentEditor)
            },
            enabled = state.hasSelectedVisa,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_segment))
        }

        // Notes
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

        // Button Save
        CustomButton(
            text = stringResource(R.string.action_save),
            onClick = { onAction(Action.SaveTrip) },
            enabled = state.hasSelectedVisa,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )
    }

    // Date Picker
    if (state.showDatePicker) {
        CustomDateRangePicker(
            dateStart = state.startDate,
            dateEnd = state.endDate,
            onConfirmClick = { startDate, endDate ->
                onAction(Action.UpdateDates(startDate, endDate))
            },
            onCancelClick = { onAction(Action.ShowDatePicker(false)) },
            startDateToChoose = state.selectedVisa?.startDate,
            endDateToChoose = state.selectedVisa?.expiryDate,
            blockedDays = state.blockedDates,
        )
    }

    ErrorDialog(
        message = state.error,
        onDismiss = { onAction(Action.SetError()) }
    )

    FullScreenLoadingIndicator(state.isLoading)
}

@Composable
private fun TripSegmentCard(
    segment: TripSegmentUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    dateFormatter: DateTimeFormatter,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickableOnce { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = segment.color,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = if (segment.country == TRANSIT) {
                            stringResource(R.string.segment_transit_option)
                        } else {
                            SchengenCountries.getCountryByCode(segment.country)?.nameRu
                                ?: segment.country
                        },
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
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "${segment.startDate.format(dateFormatter)} - ${
                            segment.endDate.format(
                                dateFormatter
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.weight(1f))

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

                if (segment.cities.isNotEmpty()) {
                    Text(
                        text = segment.cities.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
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
                showDatePicker = false,
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
                    TripSegmentUi(
                        country = "Germany",
                        startDate = LocalDate.now(),
                        endDate = LocalDate.now().plusDays(3),
                        cities = listOf("Berlin", "Munich"),
                        isExempt = false
                    ),
                    TripSegmentUi(
                        country = "Poland",
                        startDate = LocalDate.now().plusDays(3),
                        endDate = LocalDate.now().plusDays(7),
                        cities = listOf("Warsaw"),
                        color = Color.Magenta
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