package ru.nikfirs.android.traveltracker.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.model.DayCalculation
import ru.nikfirs.android.traveltracker.core.ui.model.ExistingRange
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CustomCalendar(
    existingRangeList: List<ExistingRange> = emptyList(),
    availableDateRange: ClosedRange<LocalDate>? = null,
    currentMonth: YearMonth = YearMonth.now(),
    monthsToShow: Int = 12,
    dateList: List<DayCalculation> = emptyList(),
    showDots: Boolean = false,
    showRemainingDays: Boolean = false,
) {
    // Calculate months to display
    val startMonth = availableDateRange?.start?.let {
        YearMonth.of(it.year, it.month)
    } ?: currentMonth.minusMonths(6)

    val endMonth = availableDateRange?.endInclusive?.let {
        YearMonth.of(it.year, it.month)
    } ?: currentMonth.plusMonths(monthsToShow.toLong())

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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Days of week header
        DaysOfWeekHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .align(Alignment.CenterHorizontally)
        )

        // Scrollable calendar content
        LazyColumn(
            state = listState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(monthsToDisplay) { month ->
                MonthCalendar(
                    month = month,
                    existingSegments = existingRangeList,
                    availableDateRange = availableDateRange,
                    onDateClick = {},
                    smallCells = false,
                    dateList = dateList,
                    showDots = showDots,
                    showRemainingDays = showRemainingDays,
                )
                HorizontalDivider()
            }
        }
    }
}

// Preview
@LightRUScreenPreview
@DarkENScreenPreview
@Composable
private fun CustomCalendarRangePickerPreview() {
    val now = LocalDate.now()
    AppTheme {
        CustomCalendar(
            showRemainingDays = true,
            showDots = true,
            existingRangeList = listOf(
                ExistingRange(
                    startDate = now.plusDays(5),
                    endDate = now.plusDays(8),
                    color = Color.Green
                ),
                ExistingRange(
                    startDate = now.plusDays(10),
                    endDate = now.plusDays(14),
                    color = Color.Magenta
                )
            ),
            availableDateRange = now.minusMonths(2)..now.plusDays(60),
            monthsToShow = 6,
            dateList = listOf(
                DayCalculation(
                    date = now.minusDays(1),
                    remaining = 90,
                    isIncreased = true,
                ),
                DayCalculation(
                    date = now.plusDays(1),
                    remaining = 1,
                    isUsed = true,
                ),
                DayCalculation(
                    date = now.plusDays(2),
                    remaining = -2,
                    isUsed = true,
                ),
                DayCalculation(
                    date = now.plusDays(5),
                    remaining = 87,
                    isUsed = true,
                ),
                DayCalculation(
                    date = now.plusDays(6),
                    remaining = 88,
                    isIncreased = true,
                    isUsed = true,
                ),
                DayCalculation(
                    date = now.plusDays(7),
                    remaining = 88,
                    isIncreased = true,
                    isUsed = true,
                )
            )
        )
    }
}