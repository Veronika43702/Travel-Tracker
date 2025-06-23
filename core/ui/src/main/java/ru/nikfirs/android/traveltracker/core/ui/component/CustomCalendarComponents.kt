package ru.nikfirs.android.traveltracker.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.model.CustomIndication
import ru.nikfirs.android.traveltracker.core.ui.model.DateRangeSelection
import ru.nikfirs.android.traveltracker.core.ui.model.DayCalculation
import ru.nikfirs.android.traveltracker.core.ui.model.ExistingRange
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

/**
 * Header for date picker containing:
 * - selected date range data;
 * - icon "Close" to clear selection.
 * @param selectedRange [range][DateRangeSelection] selected by user.
 * @param onClear clears [selectedRange] (both startDate and endDate = null)
 * @param modifier modifier for Row with dates and clear icon
 */
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

/**
 * Calendar month grid with month name.
 * @param month month for view
 * @param availableDateRange range of available dates for selection.
 * Dates out of range are not available for click.
 * @param onDateClick action on date. Click is able when date is within [availableDateRange]
 * @param existingSegments ranges (segments, trips, visa validity period)
 * with color data for view to user.
 * @param smallCells when true cell is fixed by width
 * @param selectedRange [range][DateRangeSelection] selected by user (used in picker).
 * @param dateList list of [dates][DayCalculation] to show information about day limit change.
 * @param showDots parameter to show dots (isUsed, isIncreased) from [dateList] at left and right bottom
 * @param showRemainingDays parameter to show remaining day count at right top
 */
@Composable
internal fun MonthCalendar(
    month: YearMonth,
    availableDateRange: ClosedRange<LocalDate>?,
    onDateClick: (LocalDate) -> Unit,
    smallCells: Boolean = true,
    existingSegments: List<ExistingRange> = emptyList(),
    selectedRange: DateRangeSelection = DateRangeSelection(),
    dateList: List<DayCalculation> = emptyList(),
    showDots: Boolean = false,
    showRemainingDays: Boolean = false,
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
            smallCells = smallCells,
            dateList = dateList,
            showDots = showDots,
            showRemainingDays = showRemainingDays,
        )
    }
}

/**
 * Calendar header with days of week
 */
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

/**
 * Calendar month grid. Height of month grid calculated by day count in current [month].
 * @param month month for view
 * @param availableDateRange range of available dates for selection.
 * Dates out of range are not available for click.
 * @param onDateClick action on date. Click is able when date is within [availableDateRange]
 * @param existingSegments ranges (segments, trips, visa validity period)
 * with color data for view to user.
 * @param smallCells when true cell is fixed by width
 * @param selectedRange [range][DateRangeSelection] selected by user (used in picker).
 * @param dateList list of [dates][DayCalculation] to show information about day limit change.
 * @param showDots parameter to show dots (isUsed, isIncreased) from [dateList] at left and right bottom
 * @param showRemainingDays parameter to show remaining day count at right top
 */
@Composable
internal fun CalendarMonthGrid(
    month: YearMonth,
    availableDateRange: ClosedRange<LocalDate>?,
    existingSegments: List<ExistingRange>,
    onDateClick: (LocalDate) -> Unit,
    smallCells: Boolean,
    selectedRange: DateRangeSelection,
    dateList: List<DayCalculation> = emptyList(),
    showDots: Boolean = true,
    showRemainingDays: Boolean = true,
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

    val rows = remember(month) { calculateWeekRowsForMonth(month) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        userScrollEnabled = false,
        verticalArrangement = Arrangement.spacedBy(getVerticalPadding(smallCells)),
        modifier = Modifier
            .then(
                if (smallCells) {
                    Modifier
                        .width(pickerCellSize * 7 + pickerHorizontalPadding * 6)
                        .height(pickerCellSize * rows + pickerVerticalPadding * (rows - 1))
                } else {
                    Modifier
                        .fillMaxWidth()
                        .height(calendarCellSize * rows + calendarVerticalPadding * (rows - 1))
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
                        if (availableDateRange?.let { date in it } != false) {
                            onDateClick(date)
                        }
                    },
                    smallCells = smallCells,
                    dateList = dateList,
                    showDots = showDots,
                    showRemainingDays = showRemainingDays
                )
            } else {
                // Empty cell for dates from previous/next month
                Box(modifier = Modifier.size(40.dp))
            }
        }
    }
}

/**
 * Calendar day
 * @param date current day for formatting
 * @param isSelected day is within user selected range
 * @param isRangeStart day is the start of user selected range
 * @param isRangeEnd day is the end of user selected range
 * @param isEndSelected end day is selected in user selected range
 * @param isAvailable day available for click
 * @param existingSegments ranges (segments, trips, visa validity period)
 * with color data for view to user.
 * @param onClick action on day click. Click is able when date is [isAvailable]
 * @param smallCells when true cell is fixed by width
 * @param dateList list of [dates][DayCalculation] to show information about day limit change.
 * @param showDots parameter to show dots (isUsed, isIncreased) from [dateList] at left and right bottom
 * @param showRemainingDays parameter to show remaining day count at right top
 */
@Composable
internal fun CalendarRangeDay(
    date: LocalDate,
    isSelected: Boolean,
    isRangeStart: Boolean,
    isRangeEnd: Boolean,
    isEndSelected: Boolean,
    isAvailable: Boolean,
    existingSegments: List<ExistingRange>,
    onClick: () -> Unit,
    smallCells: Boolean,
    dateList: List<DayCalculation> = emptyList(),
    showDots: Boolean = true,
    showRemainingDays: Boolean = true,
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
                if (smallCells) {
                    Modifier
                        .height(pickerCellSize)
                        .width(pickerCellSize + pickerHorizontalPadding)
                } else {
                    Modifier.height(calendarCellSize)
                }
            )
            .alpha(alpha)
            .clickableOnce(
                enabled = isAvailable,
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
                modifier = Modifier.height(pickerCellSize)
            )
        }

        // Background for selected range
        SelectedRangeBackground(
            isSelected = isSelected,
            isRangeStart = isRangeStart,
            isRangeEnd = isRangeEnd,
            isEndSelected = isEndSelected,
        )

        // Today indicator
        if (isToday) {
            Box(
                modifier = Modifier
                    .size(pickerCellSize)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        }

        // Background for date info (remaining days, dots)
        DateInfo(date, dateList, showDots, showRemainingDays)

        // Date text
        Box(
            modifier = Modifier
                .size(pickerCellSize)
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
    }
}

/**
 * Background for ranges. Start and End has rounded shape
 * (half of circle + rectangular from other end), other days rectangular for full width
 * @param date current day for formatting
 * @param segments ranges (segments, trips, visa validity period)
 * with color data for view to user.
 */
@Composable
private fun ExistingSegmentBackground(
    date: LocalDate,
    segments: List<ExistingRange>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var width by remember { mutableIntStateOf(0) }
    val colorWidth = remember(width) {
        pickerCellSize + (with(density) { width.toDp() } - pickerCellSize) / 2
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { width = it.size.width }
    ) {
        segments.forEach { segment ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .then(
                        when (date) {
                            segment.startDate -> Modifier
                                .width(colorWidth)
                                .align(Alignment.CenterEnd)

                            segment.endDate -> Modifier
                                .width(colorWidth)
                                .align(Alignment.CenterStart)

                            else -> Modifier.fillMaxWidth()
                        }
                    )
                    .background(
                        color = segment.color.copy(alpha = 0.1f),
                        shape = when {
                            segment.startDate == segment.endDate -> MaterialTheme.shapes.calendarCircle
                            date == segment.startDate -> MaterialTheme.shapes.calendarStart
                            date == segment.endDate -> MaterialTheme.shapes.calendarEnd
                            else -> RoundedCornerShape(0.dp)
                        }
                    )

            )
        }
    }
}

/**
 * Background for user selected range. Start and End are bright circles.
 * + half transparent and rectangular from other end, other days rectangular for full width
 * @param isSelected day is within user selected range
 * @param isRangeStart day is the start of user selected range
 * @param isRangeEnd day is the end of user selected range
 * @param isEndSelected end day is selected in user selected range
 */
@Composable
private fun SelectedRangeBackground(
    isSelected: Boolean,
    isRangeStart: Boolean,
    isRangeEnd: Boolean,
    isEndSelected: Boolean,
) {
    val density = LocalDensity.current
    var width by remember { mutableIntStateOf(0) }
    val colorWidth = remember(width) { with(density) { width.toDp() } / 2 }
    // Background for current selection
    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                width = it.size.width
            },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .height(pickerCellSize)
                    .then(
                        when {
                            (isRangeStart && !isEndSelected) -> Modifier.width(0.dp)
                            isRangeStart -> Modifier
                                .width(colorWidth)
                                .align(Alignment.CenterEnd)

                            isRangeEnd -> Modifier
                                .width(colorWidth)
                                .align(Alignment.CenterStart)

                            else -> Modifier.fillMaxWidth()
                        }
                    )
                    .background(
                        color = LocalCustomColors.current.calendarDay,
                        shape = RoundedCornerShape(0.dp)
                    )
            )
        }
    }
    // Selected Start or End Date
    if (isRangeStart || isRangeEnd) {
        Box(
            modifier = Modifier
                .size(pickerCellSize)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )
    }
}

/**
 * Additional data on dates with information about remaining days and changes.
 * @param date current day for formatting
 * @param dateList list of [dates][DayCalculation] to show information about day limit change.
 * @param showDots parameter to show dots (isUsed, isIncreased) from [dateList] at left and right bottom
 * @param showRemainingDays parameter to show remaining day count at right top
 */
@Composable
private fun DateInfo(
    date: LocalDate,
    dateList: List<DayCalculation> = emptyList(),
    showDots: Boolean = true,
    showRemainingDays: Boolean = true,
) {
    Box(Modifier.fillMaxSize()) {
        val specificDate = dateList.find { date == it.date }
        specificDate ?: return
        if (showRemainingDays) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(calendarLabelSize)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .padding(calendarPaddingInForLabels),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = specificDate.remaining.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (showDots) {
            if (specificDate.isIncreased) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = calendarPaddingOutForLabels,
                            bottom = calendarPaddingOutForLabels
                        )
                        .size(calendarLabelSmallSize)
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
                        .padding(
                            start = calendarPaddingOutForLabels,
                            bottom = calendarPaddingOutForLabels
                        )
                        .size(calendarLabelSmallSize)
                        .background(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )
            }
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

private fun getVerticalPadding(isPicker: Boolean): Dp {
    return if (isPicker) pickerVerticalPadding else calendarVerticalPadding
}

private fun calculateWeekRowsForMonth(month: YearMonth): Int {
    val firstDayOfMonth = month.atDay(1)
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value + 6) % 7 // Mon=0, Sun=6

    val totalDays = month.lengthOfMonth()
    val totalCells = firstDayOfWeek + totalDays
    return (totalCells + 6) / 7
}

// Date Picker
val pickerCellSize = 40.dp
val pickerHorizontalPadding = 8.dp
val pickerVerticalPadding = 8.dp

// Calendar
val calendarCellSize = 60.dp
val calendarVerticalPadding = 2.dp
val calendarPaddingInForLabels = 2.dp
val calendarPaddingOutForLabels = 4.dp
val calendarLabelSmallSize = 12.dp
val calendarLabelSize = 20.dp

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
                //endDate = null
                endDate = now.plusDays(7)
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
            smallCells = false,
        )
    }
}