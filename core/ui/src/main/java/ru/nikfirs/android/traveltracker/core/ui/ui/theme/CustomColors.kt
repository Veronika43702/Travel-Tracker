package ru.nikfirs.android.traveltracker.core.ui.ui.theme

import androidx.compose.ui.graphics.Color

data class CustomColors(
    val navigationBarColor: Color, // example
    val calendarDay: Color,
    val brightText: Color,
    val contrastText: Color,
)

val LightCustomColors = CustomColors(
    navigationBarColor = Color(0xFFFFFFFF),
    calendarDay = CalendarDay,
    brightText = TextBright,
    contrastText = DarkTextBright,
)

val DarkCustomColors = CustomColors(
    navigationBarColor = Color(0xFF000000),
    calendarDay = DarkCalendarDay,
    brightText = DarkTextBright,
    contrastText = TextBright,
)