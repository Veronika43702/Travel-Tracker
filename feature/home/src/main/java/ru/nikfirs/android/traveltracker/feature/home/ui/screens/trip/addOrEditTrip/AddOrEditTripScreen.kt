package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addOrEditTrip

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.domain.model.VisaEntries
import ru.nikfirs.android.traveltracker.core.ui.R as uiR
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomCalendarRangePicker
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomOutlinedButton
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomTextField
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomTextFieldButton
import ru.nikfirs.android.traveltracker.core.ui.ui.component.DialogTwoRowButton
import ru.nikfirs.android.traveltracker.core.ui.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.ui.component.FullScreenLoadingIndicator
import ru.nikfirs.android.traveltracker.core.ui.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.ui.extension.asString
import ru.nikfirs.android.traveltracker.core.ui.ui.model.DateRangeSelection
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.components.SwipeableTripSegmentCard
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addOrEditTrip.AddOrEditTripContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addOrEditTrip.AddOrEditTripContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addOrEditTrip.AddOrEditTripContract.State
import ru.nikfirs.android.traveltracker.feature.home.R
import ru.nikfirs.android.traveltracker.feature.home.ui.model.TripSegmentUi
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun AddOrEditTripScreen(
    tripId: Long?,
    navigateBack: () -> Unit,
    navigateToTripSegment: () -> Unit,
    viewModel: AddOrEditTripViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val verticalScroll = rememberScrollState()
    LaunchedEffect(tripId) {
        viewModel.setAction(Action.LoadData(tripId))
    }
    LaunchedEffect(viewModel.addTripHolder.segmentList) {
        viewModel.setAction(Action.UpdateSegmentList)
    }
    LaunchedEffectResolver(flow = viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateBack -> {
                viewModel.addTripHolder.clear()
                navigateBack()
            }

            is Effect.ScrollUp -> scope.launch { verticalScroll.scrollTo(0) }
            is Effect.ShowMessage -> {
                // TODO: Показать snackbar
            }

            is Effect.OpenSegmentEditor -> navigateToTripSegment()
        }
    }
    BackHandler {
        viewModel.addTripHolder.clear()
        navigateBack()
    }
    Screen(
        topTitle = if (tripId == null) {
            stringResource(R.string.home_trip_add_title)
        } else {
            stringResource(R.string.home_trip_edit_title)
        },
        navigateBack = {
            viewModel.addTripHolder.clear()
            navigateBack()
        },
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
                text = stringResource(R.string.home_trip_visa_section),
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
                                VisaCategory.TYPE_C -> stringResource(uiR.string.visa_type_c)
                                VisaCategory.TYPE_D -> stringResource(uiR.string.visa_type_d)
                                VisaCategory.RESIDENCE_PERMIT -> stringResource(uiR.string.visa_type_residence_permit)
                            }
                            "$typeText (${visa.visaNumber}) ${visa.country}"
                        }

                        state.availableVisas.isEmpty() -> stringResource(R.string.home_visa_no_visas)
                        else -> ""
                    },
                    required = true,
                    label = stringResource(R.string.home_trip_select_visa),
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
                            text = { Text(stringResource(R.string.home_visa_no_visas)) },
                            onClick = { },
                            enabled = false
                        )
                    } else {
                        state.availableVisas.forEach { visa ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        val typeText = when (visa.visaType) {
                                            VisaCategory.TYPE_C -> stringResource(uiR.string.visa_type_c)
                                            VisaCategory.TYPE_D -> stringResource(uiR.string.visa_type_d)
                                            VisaCategory.RESIDENCE_PERMIT -> stringResource(uiR.string.visa_type_residence_permit)
                                        }
                                        Text("$typeText (${visa.visaNumber}) ${visa.country}")
                                        Text(
                                            text = stringResource(
                                                R.string.home_visa_validity_period,
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
                            R.string.home_trip_selected_visa_validity,
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
            text = stringResource(R.string.home_trip_dates_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        // Trip Dates
        CustomTextFieldButton(
            text = if (state.startDate != null && state.endDate != null) {
                state.startDate.format(state.dateFormatter) +
                        " - " + state.endDate.format(state.dateFormatter)
            } else "",
            label = stringResource(uiR.string.calendar_select_dates),
            required = true,
            enabled = state.hasSelectedVisa,
            trailingIcon = uiR.drawable.ic_calendar_today,
            onClick = {
                focusManager.clearFocus()
                onAction(Action.ShowDatePicker(true))
            },
            isError = state.validationErrors.startDateError != null,
            supportingText = state.validationErrors.startDateError.asString(),
            modifier = Modifier.fillMaxWidth()
        )

        // Duration Info
        if (state.hasSelectedVisa && state.hasSelectedDates) {
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
                            text = stringResource(R.string.home_trip_duration),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = pluralStringResource(
                                    R.plurals.days_count,
                                    state.totalDuration.toInt(),
                                    state.totalDuration
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            if (state.hasExemptSegments) {
                                Text(
                                    text = stringResource(
                                        R.string.home_trip_countable_days,
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
                                text = stringResource(R.string.home_trip_days_available_at_start),
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
                                text = stringResource(R.string.home_trip_days_available_at_end),
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
            text = stringResource(R.string.home_trip_purpose_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        ExposedDropdownMenuBox(
            expanded = state.isPurposeDropdownExpanded,
            onExpandedChange = { onAction(Action.SetPurposeDropdownExpanded(it)) }
        ) {
            CustomTextFieldButton(
                text = when (state.purpose) {
                    TripPurpose.TOURISM -> stringResource(R.string.home_trip_purpose_tourism)
                    TripPurpose.BUSINESS -> stringResource(R.string.home_trip_purpose_business)
                    TripPurpose.FAMILY -> stringResource(R.string.home_trip_purpose_family)
                    TripPurpose.MEDICAL -> stringResource(R.string.home_trip_purpose_medical)
                    TripPurpose.EDUCATION -> stringResource(R.string.home_trip_purpose_education)
                    TripPurpose.OTHER -> stringResource(R.string.home_trip_purpose_other)
                },
                required = true,
                enabled = state.hasSelectedVisa,
                label = stringResource(R.string.home_trip_purpose_select),
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
                                    TripPurpose.TOURISM -> stringResource(R.string.home_trip_purpose_tourism)
                                    TripPurpose.BUSINESS -> stringResource(R.string.home_trip_purpose_business)
                                    TripPurpose.FAMILY -> stringResource(R.string.home_trip_purpose_family)
                                    TripPurpose.MEDICAL -> stringResource(R.string.home_trip_purpose_medical)
                                    TripPurpose.EDUCATION -> stringResource(R.string.home_trip_purpose_education)
                                    TripPurpose.OTHER -> stringResource(R.string.home_trip_purpose_other)
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
            text = stringResource(R.string.home_trip_segments_section),
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
            state.segments.forEach { segment ->
                SwipeableTripSegmentCard(
                    segment = segment,
                    onEdit = { onAction(Action.OpenEditSegmentEditor(segment)) },
                    onDelete = { onAction(Action.DeleteSegment(segment)) },
                    dateFormatter = state.dateFormatter,
                )
            }

            // Button Add Segment
            CustomOutlinedButton(
                text = stringResource(R.string.home_trip_add_segment),
                onClick = {
                    focusManager.clearFocus()
                    onAction(Action.OpenAddSegmentEditor)
                },
                enabled = state.hasSelectedVisa && state.hasSelectedDates,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (state.segments.isNotEmpty()) 16.dp else 0.dp),
                iconImage = Icons.Default.Add,
            )
        }

        // Notes
        CustomTextField(
            value = state.notes,
            onValueChange = { onAction(Action.UpdateNotes(it)) },
            enabled = state.hasSelectedVisa,
            label = stringResource(R.string.home_notes),
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Button Save
        CustomButton(
            text = stringResource(uiR.string.action_save),
            onClick = { onAction(Action.SaveTrip) },
            enabled = state.hasSelectedVisa,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )
    }

    // Date Picker
    if (state.showDatePicker) {
        CustomCalendarRangePicker(
            selectedRange = DateRangeSelection(state.startDate, state.endDate),
            availableDateRange = state.selectedVisa?.expiryDate?.let {
                state.selectedVisa.startDate..it
            },
            onDateRangeSelected = { range ->
                if (range.startDate != null && range.endDate == null)
                    onAction(Action.CalculateBlockDaysByStartDate(range.startDate))
            },
            onConfirmClick = { startDate, endDate ->
                onAction(Action.UpdateDates(startDate, endDate))
            },
            onCancelClick = { onAction(Action.ShowDatePicker(false)) },
            currentMonth = state.startDate?.let {
                YearMonth.of(it.year, it.month)
            } ?: YearMonth.now(),
            blockedPeriod = state.blockedPeriods,
        )
    }

    ErrorDialog(
        message = state.error,
        onDismiss = { onAction(Action.SetError()) }
    )

    DialogTwoRowButton(
        message = state.warningTextDaysOutSegments,
        onRightBtn = {
            onAction(Action.SaveTripWithTransit)
            onAction(Action.SetWarning(null))
        },
        onDismiss = { onAction(Action.SetWarning(null)) }
    )

    FullScreenLoadingIndicator(state.isLoading)
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
                        // cities = listOf("Warsaw"),
                       // color = Color.Magenta,
                        isExempt = true
                    )
                ),
                daysAvailableAtStart = AddOrEditTripContract.DaysAvailableInfo(
                    used = 60,
                    remaining = 30
                ),
                daysAvailableAtEnd = AddOrEditTripContract.DaysAvailableInfo(
                    used = 64,
                    remaining = 26,
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
                daysAvailableAtStart = AddOrEditTripContract.DaysAvailableInfo(
                    used = 0,
                    remaining = 90
                ),
                daysAvailableAtEnd = AddOrEditTripContract.DaysAvailableInfo(
                    used = 0,
                    remaining = 90
                )
            ),
            onAction = {}
        )
    }
}