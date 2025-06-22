package ru.nikfirs.android.traveltracker.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.extension.epochMilliToLocalDate
import ru.nikfirs.android.traveltracker.core.ui.extension.localDateToEpochMilli
import ru.nikfirs.android.traveltracker.core.ui.extension.toMonthDayFormat
import ru.nikfirs.android.traveltracker.core.ui.model.BlockDateModel
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePicker(
    onConfirmClick: (LocalDate, LocalDate) -> Unit,
    onCancelClick: () -> Unit,
    dateStart: LocalDate? = null,
    dateEnd: LocalDate? = null,
    yearRange: Pair<Int, Int> = Pair(0, 1),
    startDateToChoose: LocalDate? = null,
    endDateToChoose: LocalDate? = null,
    blockedDays: Set<LocalDate> = emptySet(),
    blockedPeriod: Set<BlockDateModel> = emptySet(),
    onStartChooseClick: (LocalDate?) -> Unit = {},
) {
    val blockDaysFromPeriod = remember(blockedPeriod) {
        val dates: MutableSet<LocalDate> = mutableSetOf()
        blockedPeriod.forEach { period ->
            var day = period.startDate
            while (!day.isAfter(period.endDate)) {
                dates.add(day)
                day = day.plusDays(1)
            }
        }
        dates
    }
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = dateStart.localDateToEpochMilli(),
        initialSelectedEndDateMillis = dateEnd.localDateToEpochMilli(),
        initialDisplayedMonthMillis = dateStart.localDateToEpochMilli()
            ?: startDateToChoose.localDateToEpochMilli(),
        yearRange = IntRange(
            startDateToChoose?.year ?: (LocalDate.now().year - yearRange.first),
            endDateToChoose?.year ?: (LocalDate.now().year + yearRange.second)
        ),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return !(startDateToChoose?.let {
                    utcTimeMillis.epochMilliToLocalDate()?.isBefore(startDateToChoose)
                } ?: false)
                        && !(endDateToChoose?.let {
                    utcTimeMillis.epochMilliToLocalDate()?.isAfter(endDateToChoose)
                } ?: false)
                        && !blockedDays.contains(utcTimeMillis.epochMilliToLocalDate())
                        && !blockDaysFromPeriod.contains(utcTimeMillis.epochMilliToLocalDate())
            }

            override fun isSelectableYear(year: Int): Boolean {
                return (year >= (startDateToChoose?.year
                    ?: (LocalDate.now().year - yearRange.first))
                        && year <= (endDateToChoose?.year
                    ?: (LocalDate.now().year + yearRange.second)))
            }
        }
    )
    var previousStartDate by remember { mutableStateOf(dateStart.localDateToEpochMilli()) }
    var previousEndDate by remember { mutableStateOf(dateEnd.localDateToEpochMilli()) }
    var isRangeComplete by remember {
        mutableStateOf(
            state.selectedStartDateMillis != null
                    && state.selectedEndDateMillis != null
        )
    }
    LaunchedEffect(state.selectedStartDateMillis) {
        state.selectedStartDateMillis?.let { startDate ->
            if (!isRangeComplete) {
                if (startDate < (previousStartDate ?: 0L)) {
                    state.setSelection(startDate, previousStartDate)
                    isRangeComplete = true
                }
                isRangeComplete = state.selectedEndDateMillis != null
                previousStartDate = startDate
            } else {
                isRangeComplete = false
                previousStartDate = startDate
            }
        }
        onStartChooseClick(state.selectedStartDateMillis.epochMilliToLocalDate())
    }
    LaunchedEffect(state.selectedEndDateMillis) {
        if (state.selectedEndDateMillis != null) {
            isRangeComplete = true
        } else if (previousEndDate != null && previousEndDate == state.selectedStartDateMillis) {
            isRangeComplete = false
        }
        previousEndDate = state.selectedEndDateMillis
    }
    DatePickerDialog(
        onDismissRequest = onCancelClick,
        confirmButton = {
            CustomButton(
                text = stringResource(R.string.action_save),
                smallButton = true,
                enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null,
                onClick = {
                    val startDate = state.selectedStartDateMillis.epochMilliToLocalDate()
                    val endDate = state.selectedEndDateMillis.epochMilliToLocalDate()
                    if (startDate != null && endDate != null) {
                        onConfirmClick(startDate, endDate)
                    }
                },
                modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 8.dp)
            )
        },
        dismissButton = {
            CustomButton(
                text = stringResource(R.string.action_cancel),
                onClick = onCancelClick,
                secondaryBtn = true,
                smallButton = true,
                modifier = Modifier.padding(top = 8.dp)

            )
        }
    ) {
        DateRangePicker(
            state = state,
            title = null,
            headline = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = colorScheme.primary)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (state.selectedStartDateMillis == null && state.selectedEndDateMillis == null) {
                            stringResource(R.string.calendar_select_dates)
                        } else {
                            if (state.selectedStartDateMillis != null && state.selectedEndDateMillis == null) {
                                state.selectedStartDateMillis?.toMonthDayFormat() + " -"
                            } else {
                                (state.selectedStartDateMillis?.toMonthDayFormat() + " - " + state.selectedEndDateMillis?.toMonthDayFormat())
                            }
                        },
                        color = colorScheme.onPrimary,
                    )
                    if (state.selectedStartDateMillis != null) {
                        IconButton(
                            onClick = { state.setSelection(null, null) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.calendar_clear_selection),
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            },
            showModeToggle = false,
            modifier = Modifier.weight(1f),
        )
    }
}


@LightRUScreenPreview
@DarkENScreenPreview
@Composable
private fun CustomCalendarPreview() {
    AppTheme {
        CustomDateRangePicker(
            startDateToChoose = LocalDate.of(2025, 6, 15),
            endDateToChoose = LocalDate.of(2025, 7, 17),
            blockedDays = setOf(
                LocalDate.now().minusDays(3)
            ),
            dateStart = LocalDate.now().plusDays(3),
            dateEnd = LocalDate.now().plusDays(20),
            onConfirmClick = { _, _ -> },
            onCancelClick = {},
            onStartChooseClick = {},
        )
    }
}