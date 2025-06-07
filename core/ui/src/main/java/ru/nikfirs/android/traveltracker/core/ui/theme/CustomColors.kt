package ru.nikfirs.android.traveltracker.core.ui.theme

import androidx.compose.ui.graphics.Color

data class CustomColors(
    val navigationBarColor: Color, // example
)

val LightCustomColors = CustomColors(
    navigationBarColor = Color(0xFFFFFFFF),
)

val DarkCustomColors = CustomColors(
    navigationBarColor = Color(0xFF000000),
)