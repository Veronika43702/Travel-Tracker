package ru.nikfirs.android.traveltracker.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.core.ui.theme.calendar
import ru.nikfirs.android.traveltracker.core.ui.theme.calendarCircle
import ru.nikfirs.android.traveltracker.core.ui.theme.calendarEnd
import ru.nikfirs.android.traveltracker.core.ui.theme.calendarStart
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

data class TripSegmentDisplay(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val color: Color,
    val isSelected: Boolean = false
)

data class DateRangeSelection(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
) {
    val isComplete: Boolean get() = startDate != null && endDate != null
    val isPartial: Boolean get() = startDate != null && endDate == null
}

@Composable
fun CustomCalendar(
    modifier: Modifier = Modifier,
    selectedRange: DateRangeSelection = DateRangeSelection(),
    existingSegments: List<TripSegmentDisplay> = emptyList(),
    availableDateRange: ClosedRange<LocalDate>? = null,
    onDateRangeSelected: (DateRangeSelection) -> Unit = {},
    onRangeComplete: (LocalDate, LocalDate) -> Unit = {_, _ ->},
    currentMonth: YearMonth = YearMonth.now()
) {
    var displayedMonth by remember { mutableStateOf(currentMonth) }
    var tempSelection by remember { mutableStateOf(selectedRange) }

    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // Header with month navigation
        CalendarHeader(
            currentMonth = displayedMonth,
            onPreviousMonth = { displayedMonth = displayedMonth.minusMonths(1) },
            onNextMonth = { displayedMonth = displayedMonth.plusMonths(1) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Days of week header
        DaysOfWeekHeader()

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid
        CalendarGrid(
            month = displayedMonth,
            selectedRange = tempSelection,
            existingSegments = existingSegments,
            availableDateRange = availableDateRange,
            onDateClick = { date ->
                val newSelection = when {
                    tempSelection.startDate == null -> {
                        DateRangeSelection(startDate = date)
                    }
                    tempSelection.endDate == null -> {
                        val startDate = tempSelection.startDate!!
                        if (date.isBefore(startDate)) {
                            DateRangeSelection(startDate = date)
                        } else {
                            onRangeComplete(startDate, date)
                            DateRangeSelection(startDate = startDate, endDate = date)
                        }
                    }
                    else -> {
                        DateRangeSelection(startDate = date)
                    }
                }
                tempSelection = newSelection
                onDateRangeSelected(newSelection)
            }
        )

        if (tempSelection.isPartial) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.calendar_select_end_date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.calendar_previous_month),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = currentMonth.format(
                DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.calendar_next_month),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DaysOfWeekHeader() {
    val weekFields = WeekFields.of(Locale.getDefault())
    val firstDayOfWeek = weekFields.firstDayOfWeek

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (i in 0..6) {
            val dayOfWeek = firstDayOfWeek.plus(i.toLong())
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    selectedRange: DateRangeSelection,
    existingSegments: List<TripSegmentDisplay>,
    availableDateRange: ClosedRange<LocalDate>?,
    onDateClick: (LocalDate) -> Unit
) {
    val weekFields = WeekFields.of(Locale.getDefault())
    val firstDayOfWeek = weekFields.firstDayOfWeek
    val firstDateOfMonth = month.atDay(1)
    val lastDateOfMonth = month.atEndOfMonth()

    // Calculate start of the week for the first day of month
    val startDate = firstDateOfMonth.with(firstDayOfWeek)
    val endDate = lastDateOfMonth.with(weekFields.dayOfWeek(), 7)

    val dates = generateSequence(startDate) { it.plusDays(1) }
        .takeWhile { !it.isAfter(endDate) }
        .toList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        userScrollEnabled = false,
        modifier = Modifier.height(240.dp) // 6 weeks * 40dp per week
    ) {
        items(dates) { date ->
            CalendarDay(
                date = date,
                isCurrentMonth = date.month == month.month,
                isSelected = isDateInRange(date, selectedRange),
                isRangeStart = selectedRange.startDate == date,
                isRangeEnd = selectedRange.endDate == date,
                existingSegments = existingSegments.filter {
                    date >= it.startDate && date <= it.endDate
                },
                isAvailable = availableDateRange?.let { date in it } ?: true,
                onClick = { if (availableDateRange?.let { date in it } != false) onDateClick(date) }
            )
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isRangeStart: Boolean,
    isRangeEnd: Boolean,
    existingSegments: List<TripSegmentDisplay>,
    isAvailable: Boolean,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (isCurrentMonth && isAvailable) 1f else 0.3f,
        label = "alpha"
    )

    val today = LocalDate.now()
    val isToday = date == today

    Box(
        modifier = Modifier
            .size(40.dp)
            .alpha(alpha)
            .clickableOnce(enabled = isCurrentMonth && isAvailable) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Background for existing segments
        if (existingSegments.isNotEmpty() && isCurrentMonth) {
            ExistingSegmentBackground(
                date = date,
                segments = existingSegments,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Background for current selection
        if (isSelected && isCurrentMonth) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = when {
                            isRangeStart && isRangeEnd -> MaterialTheme.shapes.calendarCircle
                            isRangeStart -> MaterialTheme.shapes.calendarStart
                            isRangeEnd -> MaterialTheme.shapes.calendarEnd
                            else -> RoundedCornerShape(0.dp)
                        }
                    )
            )
        }

        // Today indicator
        if (isToday && isCurrentMonth) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
        }

        // Date text
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = when {
                !isAvailable -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ExistingSegmentBackground(
    date: LocalDate,
    segments: List<TripSegmentDisplay>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        segments.forEachIndexed { index, segment ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = segment.color.copy(alpha = 0.1f / (index + 1)),
                        shape = when {
                            segment.startDate == segment.endDate -> MaterialTheme.shapes.calendarCircle
                            date == segment.startDate -> MaterialTheme.shapes.calendarStart
                            date == segment.endDate-> MaterialTheme.shapes.calendarEnd
                            else -> RoundedCornerShape(0.dp)
                        }
                    )
            )
        }
    }
}

private fun isDateInRange(date: LocalDate, range: DateRangeSelection): Boolean {
    val start = range.startDate ?: return false
    val end = range.endDate ?: return date == start
    return date in start..end
}

// Preview
@LightRUScreenPreview
@DarkENScreenPreview
@Composable
private fun CustomCalendarPreview() {
    AppTheme {
        CustomCalendar(
            selectedRange = DateRangeSelection(
                startDate = LocalDate.now().plusDays(3),
                endDate = LocalDate.now().plusDays(5)
            ),
            existingSegments = listOf(
                TripSegmentDisplay(
                    id = "1",
                    startDate = LocalDate.now(),
                    endDate = LocalDate.now().plusDays(3),
                    color = Color.Green
                ),
                TripSegmentDisplay(
                    id = "2",
                    startDate = LocalDate.now().plusDays(5),
                    endDate = LocalDate.now().plusDays(7),
                    color = Color.Green
                )
            ),
            availableDateRange = LocalDate.now()..LocalDate.now().plusDays(14),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomCalendarEmptyPreview() {
    AppTheme {
        CustomCalendar(
            availableDateRange = LocalDate.now()..LocalDate.now().plusDays(30),
            modifier = Modifier.padding(16.dp)
        )
    }
}