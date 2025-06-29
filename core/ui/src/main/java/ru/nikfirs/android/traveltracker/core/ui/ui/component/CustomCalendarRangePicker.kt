package ru.nikfirs.android.traveltracker.core.ui.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.ui.model.BlockDateModel
import ru.nikfirs.android.traveltracker.core.ui.ui.model.DateRangeSelection
import ru.nikfirs.android.traveltracker.core.ui.ui.model.ExistingRange
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCalendarRangePicker(
    selectedRange: DateRangeSelection = DateRangeSelection(),
    existingRangeList: List<ExistingRange> = emptyList(),
    availableDateRange: ClosedRange<LocalDate>? = null,
    onDateRangeSelected: (DateRangeSelection) -> Unit = {},
    currentMonth: YearMonth = YearMonth.now(),
    onCancelClick: () -> Unit = {},
    onConfirmClick: (LocalDate, LocalDate) -> Unit = { _, _ -> },
    blockedDays: Set<LocalDate> = emptySet(),
    blockedPeriod: Set<BlockDateModel> = emptySet(),
) {
    var tempSelection by remember { mutableStateOf(selectedRange) }

    // Calculate months to display
    val startMonth = availableDateRange?.start?.let {
        YearMonth.of(it.year, it.month)
    } ?: currentMonth.minusMonths(6)

    val endMonth = availableDateRange?.endInclusive?.let {
        YearMonth.of(it.year, it.month)
    } ?: currentMonth.plusMonths(12)

    val monthsToDisplay = generateSequence(startMonth) { it.plusMonths(1) }
        .takeWhile { !it.isAfter(endMonth) }
        .toList()

    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        val index = monthsToDisplay.indexOf(currentMonth)
        if (index >= 0) {
            listState.scrollToItem(index)
        }
    }

    DatePickerDialog(
        onDismissRequest = onCancelClick,
        confirmButton = {
            CustomButton(
                text = stringResource(R.string.action_save),
                smallButton = true,
                enabled = tempSelection.startDate != null && tempSelection.endDate != null,
                onClick = {
                    val startDate = tempSelection.startDate
                    val endDate = tempSelection.endDate
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date range display header
            DateRangePickerHeader(
                selectedRange = tempSelection,
                onClear = {
                    tempSelection = DateRangeSelection()
                    onDateRangeSelected(DateRangeSelection())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Days of week header
            DaysOfWeekHeader(
                modifier = Modifier
                    .width(pickerCellSize * 7 + pickerHorizontalPadding * 6)
                    .padding(horizontal = 12.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Scrollable calendar content
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(pickerCellSize * 7 + pickerHorizontalPadding * 6)
                    .height(pickerCellSize * 9 + 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(monthsToDisplay) { month ->
                    MonthCalendar(
                        month = month,
                        selectedRange = tempSelection,
                        existingSegments = existingRangeList,
                        availableDateRange = availableDateRange,
                        onDateClick = { date ->
                            val newSelection = when {
                                tempSelection.startDate == null -> {
                                    DateRangeSelection(startDate = date)
                                }

                                tempSelection.endDate == null -> {
                                    val startDate = tempSelection.startDate
                                    val (finalStart, finalEnd) = if (date.isBefore(startDate)) {
                                        Pair(date, startDate)
                                    } else {
                                        Pair(startDate, date)
                                    }
                                    DateRangeSelection(
                                        startDate = finalStart,
                                        endDate = finalEnd
                                    )
                                }

                                else -> {
                                    DateRangeSelection(startDate = date)
                                }
                            }
                            tempSelection = newSelection
                            onDateRangeSelected(newSelection)
                        },
                        smallCells = true,
                        blockedDays = blockedDays,
                        blockedPeriod = blockedPeriod,
                    )
                }
            }
        }
    }
}

// Preview
@LightRUScreenPreview
@DarkENPreview
@Composable
private fun CustomCalendarRangePickerPreview() {
    AppTheme {
        CustomCalendarRangePicker(
            selectedRange = DateRangeSelection(
                startDate = LocalDate.now().plusDays(0),
                endDate = LocalDate.now().plusDays(3)
            ),
            existingRangeList = listOf(
                ExistingRange(
                    startDate = LocalDate.now().plusDays(5),
                    endDate = LocalDate.now().plusDays(8),
                    color = Color.Yellow
                ),
                ExistingRange(
                    startDate = LocalDate.now().plusDays(10),
                    endDate = LocalDate.now().plusDays(14),
                    color = Color.Magenta
                )
            ),
            availableDateRange = LocalDate.now()..LocalDate.now().plusDays(60),
        )
    }
}