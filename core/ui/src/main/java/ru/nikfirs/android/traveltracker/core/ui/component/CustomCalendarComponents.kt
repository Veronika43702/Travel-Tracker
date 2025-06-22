package ru.nikfirs.android.traveltracker.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.model.CustomIndication
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.core.ui.theme.LocalCustomColors
import ru.nikfirs.android.traveltracker.core.ui.theme.calendarCircle
import ru.nikfirs.android.traveltracker.core.ui.theme.calendarDay
import ru.nikfirs.android.traveltracker.core.ui.theme.calendarEnd
import ru.nikfirs.android.traveltracker.core.ui.theme.calendarStart
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
internal fun DateRangePickerHeader(
    selectedRange: DateRangeSelection,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.getDefault()
    val isRussian = locale.language.startsWith("ru")

    val dateFormatter = if (isRussian) {
        DateTimeFormatter.ofPattern("d MMM", locale)
    } else {
        DateTimeFormatter.ofPattern("MMM d", locale)
    }

    val displayText = when {
        selectedRange.startDate == null -> {
            stringResource(R.string.calendar_select_dates)
        }

        selectedRange.endDate == null -> {
            "${selectedRange.startDate.format(dateFormatter)} -"
        }

        else -> {
            "${selectedRange.startDate.format(dateFormatter)} - ${
                selectedRange.endDate.format(
                    dateFormatter
                )
            }"
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.weight(1f)
        )
        if (selectedRange.startDate != null) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.calendar_clear_selection),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
internal fun MonthCalendar(
    month: YearMonth,
    selectedRange: DateRangeSelection,
    existingSegments: List<ExistingRange>,
    availableDateRange: ClosedRange<LocalDate>?,
    onDateClick: (LocalDate) -> Unit,
    isDatePicker: Boolean,
    dateList: List<DayCalculation> = emptyList(),
) {
    Column {
        // Month header
        Text(
            text = month.format(
                DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, bottom = 8.dp),
            textAlign = TextAlign.Start
        )

        // Calendar grid for the month
        CalendarMonthGrid(
            month = month,
            selectedRange = selectedRange,
            existingSegments = existingSegments,
            availableDateRange = availableDateRange,
            onDateClick = onDateClick,
            isDatePicker = isDatePicker,
            dateList = dateList,
        )
    }
}

@Composable
internal fun DaysOfWeekHeader(
    modifier: Modifier = Modifier
) {
    val weekFields = WeekFields.of(Locale.getDefault())
    val firstDayOfWeek = weekFields.firstDayOfWeek

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        for (i in 0..6) {
            val dayOfWeek = firstDayOfWeek.plus(i.toLong())
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
internal fun CalendarMonthGrid(
    month: YearMonth,
    selectedRange: DateRangeSelection,
    existingSegments: List<ExistingRange>,
    availableDateRange: ClosedRange<LocalDate>?,
    onDateClick: (LocalDate) -> Unit,
    isDatePicker: Boolean,
    dateList: List<DayCalculation> = emptyList(),
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
        verticalArrangement = Arrangement.spacedBy(getVerticalPadding(isDatePicker)),
        modifier = Modifier
            .then(
                if (isDatePicker) {
                    Modifier
                        .width(cellSize * 7 + horizontalPadding * 6)
                        .height(cellSize * 6 + verticalPickerPadding * 6) // 6 weeks * 40dp per week + spacing
                } else {
                    Modifier
                        .fillMaxWidth()
                        .height(cellLargeSize * 6 + verticalPadding * 6)
                }
            )
    ) {
        items(dates) { date ->
            if (date.month == month.month) {
                // Show only dates that belong to current month
                CalendarRangeDay(
                    date = date,
                    isSelected = isDateInRange(date, selectedRange),
                    isRangeStart = selectedRange.startDate == date,
                    isRangeEnd = selectedRange.endDate == date,
                    isEndSelected = selectedRange.endDate != null,
                    existingSegments = existingSegments.filter {
                        date >= it.startDate && date <= it.endDate
                    },
                    isAvailable = availableDateRange?.let { date in it } ?: true,
                    onClick = {
                        if (availableDateRange?.let { date in it } != false) onDateClick(
                            date
                        )
                    },
                    isDatePicker = isDatePicker,
                    dateList = dateList,
                )
            } else {
                // Empty cell for dates from previous/next month
                Box(modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
internal fun CalendarRangeDay(
    date: LocalDate,
    isSelected: Boolean,
    isRangeStart: Boolean,
    isRangeEnd: Boolean,
    isEndSelected: Boolean,
    existingSegments: List<ExistingRange>,
    isAvailable: Boolean,
    onClick: () -> Unit,
    isDatePicker: Boolean,
    dateList: List<DayCalculation> = emptyList(),
) {
    val today = LocalDate.now()
    val isToday = date == today

    val alpha by animateFloatAsState(
        targetValue = if (isAvailable || isToday) 1f else 0.3f,
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .then(
                if (isDatePicker) {
                    Modifier
                        .height(cellSize)
                        .width(cellSize + horizontalPadding)
                } else {
                    Modifier.height(cellLargeSize)
                }
            )
            .alpha(alpha)
            .clickableOnce(
                enabled = isAvailable && isDatePicker,
                indication = CustomIndication(
                    ripple(bounded = false, radius = 28.dp)
                ),
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Background for existing segments
        if (existingSegments.isNotEmpty()) {
            ExistingSegmentBackground(
                date = date,
                segments = existingSegments,
                modifier = Modifier.height(cellSize)
            )
        }

        // Background for current selection
        if (isSelected) {
            Box(
                modifier = Modifier
                    .then(
                        if (isDatePicker) {
                            Modifier
                                .height(cellSize)
                                .width(
                                    when {
                                        isRangeStart && (isRangeEnd || !isEndSelected) -> cellSize
                                        isRangeStart -> cellSize + horizontalPadding
                                        isRangeEnd -> cellSize + horizontalPadding
                                        else -> cellSize + horizontalPadding
                                    }
                                )
                        } else {
                            Modifier.fillMaxSize()
                        }
                    )
                    .padding(
                        start = if (isRangeStart && isEndSelected) cellSize / 2 else 0.dp,
                        end = if (isRangeEnd) cellSize / 2 else 0.dp,
                    )
                    .background(
                        color = LocalCustomColors.current.calendarDay,
                        shape = when {
                            isRangeStart && (isRangeEnd || !isEndSelected) -> MaterialTheme.shapes.calendarCircle
                            else -> RoundedCornerShape(0.dp)
                        }
                    )
            )
        }

        // Today indicator
        if (isToday) {
            Box(
                modifier = Modifier
                    .size(cellSize)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        }

        // Selected Start or End Date
        if (isRangeStart || isRangeEnd) {
            Box(
                modifier = Modifier
                    .size(cellSize)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        }

        // Date text
        Box(
            modifier = Modifier
                .size(cellSize)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.calendarDay,
                color = when {
                    isRangeStart || isRangeEnd -> LocalCustomColors.current.contrastText
                    isToday -> MaterialTheme.colorScheme.primary
                    !isAvailable -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    else -> LocalCustomColors.current.brightText
                },
                textAlign = TextAlign.Center,
            )
        }

        if (!isDatePicker) {
            val specificDate = dateList.find { date == it.date }
            specificDate ?: return
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(labelSize)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .padding(paddingInForLabels),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = specificDate.remaining.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
            }
            if (specificDate.isIncreased) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = paddingOutForLabels, bottom = paddingOutForLabels)
                        .size(labelSmallSize)
                        .background(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )
            }
            if (specificDate.isUsed) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = paddingOutForLabels, bottom = paddingOutForLabels)
                        .size(labelSmallSize)
                        .background(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun ExistingSegmentBackground(
    date: LocalDate,
    segments: List<ExistingRange>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        segments.forEach { segment ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (date == segment.startDate) horizontalPadding / 6 else 0.dp,
                        end = if (date == segment.endDate) horizontalPadding / 6 else 0.dp,
                    )
                    .background(
                        color = segment.color.copy(alpha = 0.1f),
                        shape = when {
                            segment.startDate == segment.endDate -> MaterialTheme.shapes.calendarCircle
                            date == segment.startDate -> MaterialTheme.shapes.calendarStart
                            date == segment.endDate -> MaterialTheme.shapes.calendarEnd
                            else -> RoundedCornerShape(0.dp)
                        }
                    ),
            )
        }
    }
}

internal fun isDateInRange(
    date: LocalDate,
    range: DateRangeSelection
): Boolean {
    val start = range.startDate ?: return false
    val end = range.endDate ?: return date == start
    return date in start..end
}

data class ExistingRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val color: Color,
    val id: Long? = null,
)

data class DateRangeSelection(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
) {
    val isComplete: Boolean get() = startDate != null && endDate != null
}

val cellSize = 40.dp
val horizontalPadding = 8.dp
val verticalPickerPadding = 8.dp

fun getVerticalPadding(isPicker: Boolean): Dp {
    return if (isPicker) verticalPickerPadding else verticalPadding
}

val cellLargeSize = 60.dp
val verticalPadding = 2.dp
val paddingInForLabels = 2.dp
val paddingOutForLabels = 4.dp
val labelSmallSize = 12.dp
val labelSize = 20.dp

// Preview
@LightRUPreview
@DarkENPreview
@Composable
private fun DateRangePickerHeaderPreview() {
    AppTheme {
        DateRangePickerHeader(
            selectedRange = DateRangeSelection(
                startDate = LocalDate.now().plusDays(0),
                endDate = LocalDate.now().plusDays(3)
            ),
            onClear = {},
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@LightRUPreview
@DarkENPreview
@Composable
private fun MonthCalendarPreview() {
    val now = LocalDate.now()
    AppTheme {
        MonthCalendar(
            selectedRange = DateRangeSelection(
                startDate = now.plusDays(5),
                endDate = now.plusDays(8)
            ),
            existingSegments = listOf(
                ExistingRange(
                    startDate = now.minusDays(4),
                    endDate = now.minusDays(1),
                    color = Color.Yellow
                ),
                ExistingRange(
                    startDate = now.plusDays(2),
                    endDate = now.plusDays(4),
                    color = Color.Magenta
                )
            ),
            availableDateRange = now.minusDays(5)..now.plusDays(60),
            month = YearMonth.of(now.year, now.month),
            onDateClick = {},
            isDatePicker = true,
        )
    }
}