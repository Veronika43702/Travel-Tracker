package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.utils

import androidx.compose.ui.graphics.Color

val segment_colors = listOf(
    Color(0xFFFF9800), // Orange
    Color(0xFF9C27B0), // Purple
    Color(0xFF00BCD4), // Cyan
    Color(0xFFF44336), // Red
    Color(0xFF607D8B), // Blue Grey
    Color(0xFFF527FF), // Pink
    Color(0xFF8BC34A), // Light Green
    Color(0xFF2196F3), // Blue
)

fun getTripSegmentColorByIndex(index: Int): Color {
    return segment_colors[index % segment_colors.size]
}