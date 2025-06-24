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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.nikfirs.android.traveltracker.core.data.model.TRANSIT
import ru.nikfirs.android.traveltracker.core.domain.model.SchengenCountries
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomCalendarRangePicker
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomTextField
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomTextFieldButton
import ru.nikfirs.android.traveltracker.core.ui.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.ui.extension.asString
import ru.nikfirs.android.traveltracker.core.ui.ui.model.DateRangeSelection
import ru.nikfirs.android.traveltracker.core.ui.ui.model.ExistingRange
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.home.ui.model.TripSegmentUi
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentContract.State
import java.time.LocalDate
import java.time.YearMonth
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

        // Country
        ExposedDropdownMenuBox(
            expanded = state.isCountryDropdownExpanded,
            onExpandedChange = { onAction(Action.SetCountryDropdownExpanded(it)) }
        ) {
            CustomTextFieldButton(
                text = when {
                    state.country == TRANSIT -> stringResource(R.string.segment_transit_option)
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
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.segment_transit_option)) },
                    onClick = { onAction(Action.UpdateCountry(TRANSIT)) }
                )
                HorizontalDivider()
                SchengenCountries.countries.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country.getDisplayName(locale)) },
                        onClick = { onAction(Action.UpdateCountry(country.code)) }
                    )
                }
            }
        }

        // Date Range
        Text(
            text = stringResource(R.string.segment_dates),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        CustomTextFieldButton(
            text = if (state.startDate != null && state.endDate != null) {
                state.startDate.format(state.dateFormatter) +
                        " - " + state.endDate.format(state.dateFormatter)
            } else "",
            label = stringResource(R.string.calendar_select_dates),
            required = true,
            trailingIcon = R.drawable.ic_calendar_today,
            onClick = {
                focusManager.clearFocus()
                onAction(Action.ShowDatePicker(true))
            },
            modifier = Modifier.fillMaxWidth(),
            isError = state.validationErrors.datesError != null,
            supportingText = state.validationErrors.datesError.asString()
        )

        // Duration
        if (state.duration > 0) {
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
        }

        // Cities
        CustomTextField(
            value = state.cities,
            onValueChange = { onAction(Action.UpdateCities(it)) },
            label = stringResource(R.string.add_segment_cities),
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 4,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            CustomButton(
                text = stringResource(R.string.action_save),
                onClick = { onAction(Action.SaveSegment) },
                enabled = state.selectedDateRange.isComplete && state.country.isNotBlank(),
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

    if (state.showCalendar) {
        CustomCalendarRangePicker(
            selectedRange = state.selectedDateRange,
            existingRangeList = state.segmentList.map {
                ExistingRange(
                    startDate = it.startDate,
                    endDate = it.endDate,
                    color = it.color,
                )
            },
            availableDateRange = state.availableDateRange,
            onDateRangeSelected = { dateRange ->
                onAction(Action.UpdateDateRange(dateRange))
            },
            currentMonth = state.selectedDateRange.startDate?.let {
                YearMonth.of(it.year, it.month)
            } ?: YearMonth.of(state.tripStartDate.year, state.tripStartDate.month),
            onCancelClick = { onAction(Action.ShowDatePicker(false)) },
            onConfirmClick = { startDate, endDate ->
                onAction(Action.ShowDatePicker(false))
                onAction(Action.OnDateRangeComplete(startDate, endDate))
            },
        )
    }

    ErrorDialog(
        message = state.error,
        onDismiss = { onAction(Action.SetError()) }
    )
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
                selectedDateRange = DateRangeSelection(
                    startDate = LocalDate.now(),
                    endDate = LocalDate.now().plusDays(3)
                ),
                cities = "Berlin, Munich",
                segmentList = listOf(
                    TripSegmentUi(
                        startDate = LocalDate.now().plusDays(4),
                        endDate = LocalDate.now().plusDays(6),
                        country = "",
                        color = Color.Blue,
                        isExempt = false
                    )
                ),
                showCalendar = false,
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
                country = TRANSIT,
                selectedDateRange = DateRangeSelection(
                    startDate = LocalDate.now().plusDays(2),
                    endDate = LocalDate.now().plusDays(4)
                ),
                isEditMode = true,
                segmentList = listOf(
                    TripSegmentUi(
                        startDate = LocalDate.now(),
                        endDate = LocalDate.now().plusDays(2),
                        country = "",
                        color = Color.Green,
                        isExempt = false
                    ),
                    TripSegmentUi(
                        startDate = LocalDate.now().plusDays(5),
                        endDate = LocalDate.now().plusDays(7),
                        country = "",
                        isExempt = false
                    )
                ),
                showCalendar = false,
            ),
            onAction = {}
        )
    }
}