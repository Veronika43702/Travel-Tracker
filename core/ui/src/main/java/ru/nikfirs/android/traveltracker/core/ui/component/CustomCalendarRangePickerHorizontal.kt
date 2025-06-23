package ru.nikfirs.android.traveltracker.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.model.CustomIndication
import ru.nikfirs.android.traveltracker.core.ui.model.DateRangeSelection
import ru.nikfirs.android.traveltracker.core.ui.model.ExistingRange
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.core.ui.theme.calendarCircle
import ru.nikfirs.android.traveltracker.core.ui.theme.calendarEnd
import ru.nikfirs.android.traveltracker.core.ui.theme.calendarStart
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun CustomDateRangePickerHorizontal(
    modifier: Modifier = Modifier,
    selectedRange: DateRangeSelection = DateRangeSelection(),
    existingRangeList: List<ExistingRange> = emptyList(),
    availableDateRange: ClosedRange<LocalDate>? = null,
    onDateRangeSelected: (DateRangeSelection) -> Unit = {},
    currentMonth: YearMonth = YearMonth.now(),
    onCancelClick: () -> Unit = {},
    onConfirmClick: (LocalDate, LocalDate) -> Unit = { _, _ -> },
) {
    var displayedMonth by remember { mutableStateOf(currentMonth) }
    var tempSelection by remember { mutableStateOf(selectedRange) }

    Dialog(
        onDismissRequest = onCancelClick,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date range display header
                DateRangeHeader(
                    selectedRange = tempSelection,
                    onClear = {
                        tempSelection = DateRangeSelection()
                        onDateRangeSelected(DateRangeSelection())
                    }
                )

                // Header with month navigation
                CalendarHeader(
                    currentMonth = displayedMonth,
                    onPreviousMonth = { displayedMonth = displayedMonth.minusMonths(1) },
                    onNextMonth = { displayedMonth = displayedMonth.plusMonths(1) }
                )

                // Days of week header
                DaysOfWeekHeader()

                // Calendar grid
                Column {
                    CalendarGrid(
                        month = displayedMonth,
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
                                    DateRangeSelection(startDate = finalStart, endDate = finalEnd)
                                }

                                else -> {
                                    DateRangeSelection(startDate = date)
                                }
                            }
                            tempSelection = newSelection
                            onDateRangeSelected(newSelection)
                        }
                    )
                }

                CalendarButtons(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 24.dp, bottom = 12.dp),
                    enabledConfirm = tempSelection.startDate != null && tempSelection.endDate != null,
                    onConfirmClick = {
                        val startDate = tempSelection.startDate
                        val endDate = tempSelection.endDate
                        if (startDate != null && endDate != null) {
                            onConfirmClick(startDate, endDate)
                        }
                    },
                    onCancelClick = onCancelClick,
                )
            }
        }
    }
}

@Composable
private fun DateRangeHeader(
    selectedRange: DateRangeSelection,
    onClear: () -> Unit
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
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
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
    existingSegments: List<ExistingRange>,
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
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp), // 6 weeks * 40dp per week
    ) {
        items(dates) { date ->
            CalendarDay(
                date = date,
                isCurrentMonth = date.month == month.month,
                isSelected = isDateInRange(date, selectedRange),
                isRangeStart = selectedRange.startDate == date,
                isRangeEnd = selectedRange.endDate == date,
                isEndSelected = selectedRange.endDate != null,
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
    isEndSelected: Boolean,
    existingSegments: List<ExistingRange>,
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
            .clickableOnce(
                enabled = isCurrentMonth && isAvailable,
                indication = CustomIndication(
                    ripple(bounded = false, radius = 28.dp)
                ),
            ) { onClick() },
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
                            isRangeStart && (isRangeEnd || !isEndSelected) -> MaterialTheme.shapes.calendarCircle
                            isRangeStart -> MaterialTheme.shapes.calendarStart
                            isRangeEnd -> MaterialTheme.shapes.calendarEnd
                            else -> RoundedCornerShape(0.dp)
                        }
                    ),
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
                    ),
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
    segments: List<ExistingRange>,
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
                            date == segment.endDate -> MaterialTheme.shapes.calendarEnd
                            else -> RoundedCornerShape(0.dp)
                        }
                    ),
            )
        }
    }
}

// Preview
@LightRUScreenPreview
@DarkENPreview
@Composable
private fun CustomCalendarPreview() {
    AppTheme {
        CustomDateRangePickerHorizontal(
            selectedRange = DateRangeSelection(
                startDate = LocalDate.now().plusDays(3),
                endDate = LocalDate.now().plusDays(5)
            ),
            existingRangeList = listOf(
                ExistingRange(
                    startDate = LocalDate.now(),
                    endDate = LocalDate.now().plusDays(3),
                    color = Color.Green
                ),
                ExistingRange(
                    startDate = LocalDate.now().plusDays(5),
                    endDate = LocalDate.now().plusDays(7),
                    color = Color.Magenta
                )
            ),
            availableDateRange = LocalDate.now()..LocalDate.now().plusDays(14),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomCalendarEmptyPreview() {
    AppTheme {
        CustomDateRangePickerHorizontal(
            availableDateRange = LocalDate.now()..LocalDate.now().plusDays(30),
            modifier = Modifier.padding(16.dp)
        )
    }
}