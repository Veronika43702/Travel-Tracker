package ru.nikfirs.android.traveltracker.core.ui.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.ui.model.DayCalculation
import ru.nikfirs.android.traveltracker.core.ui.ui.model.ExistingRange
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CustomCalendar(
    existingRangeList: List<ExistingRange> = emptyList(),
    availableDateRange: ClosedRange<LocalDate>? = null,
    currentMonth: YearMonth = YearMonth.now(),
    dateList: List<DayCalculation> = emptyList(),
    showDots: Boolean = false,
    showRemainingDays: Boolean = false,
    onDateClick: (LocalDate) -> Unit,
    onCalculationInProgress: ((Boolean) -> Unit)? = null,
) {
    val (startMonth, monthCount, initialIndex) = remember(
        availableDateRange,
        currentMonth
    ) {
        val start = availableDateRange?.start?.let {
            YearMonth.of(it.year, it.month)
        } ?: currentMonth.minusMonths(6)

        val end = availableDateRange?.endInclusive?.let {
            YearMonth.of(it.year, it.month)
        } ?: currentMonth.plusMonths(12)

        val count = calculateMonthCount(start, end)
        val index = findCurrentMonthIndex(start, end, currentMonth)

        onCalculationInProgress?.invoke(false)
        CalendarData(start, count, index)
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex
    )

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
            items(
                count = monthCount,
                key = { index -> getMonthByIndex(startMonth, index).toString() }
            ) { index ->
                val month = getMonthByIndex(startMonth, index)

                MonthCalendar(
                    month = month,
                    existingSegments = existingRangeList,
                    availableDateRange = availableDateRange,
                    onDateClick = onDateClick,
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

private data class CalendarData(
    val startMonth: YearMonth,
    val monthCount: Int,
    val currentMonthIndex: Int
)

private fun calculateMonthCount(start: YearMonth, end: YearMonth): Int {
    var count = 0
    var current = start
    while (!current.isAfter(end)) {
        count++
        current = current.plusMonths(1)
    }
    return count
}

private fun findCurrentMonthIndex(start: YearMonth, end: YearMonth, currentMonth: YearMonth): Int {
    var current = start
    var index = 0
    while (!current.isAfter(end)) {
        if (current == currentMonth) return index
        current = current.plusMonths(1)
        index++
    }
    return 0
}

private fun getMonthByIndex(startMonth: YearMonth, index: Int): YearMonth {
    return startMonth.plusMonths(index.toLong())
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
            ),
            onDateClick = {},
        )
    }
}