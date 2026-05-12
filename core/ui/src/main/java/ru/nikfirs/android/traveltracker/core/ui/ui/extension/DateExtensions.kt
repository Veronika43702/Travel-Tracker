package ru.nikfirs.android.traveltracker.core.ui.ui.extension

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LocalDate?.localDateToEpochMilli(): Long? {
    this ?: return null
    val zonedDateTime = this.atStartOfDay(ZoneId.of("GMT"))
    return zonedDateTime.toInstant().toEpochMilli()
}

fun Long?.epochMilliToLocalDate(): LocalDate? {
    this ?: return null
    val instant = Instant.ofEpochMilli(this)
    return instant.atZone(ZoneId.systemDefault()).toLocalDate()
}

fun Long.formatDate(pattern: String): String? {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).format(this)
    } catch (e: Exception) {
        null
    }
}

fun Long?.toMonthDayFormat(): String? {
    return try {
        val locale = Locale.getDefault()
        val isRussian = locale.language.startsWith("ru")

        val dateFormatter = if (isRussian) {
            "d MMM"
        } else {
            "MMM d"
        }
        SimpleDateFormat(dateFormatter, Locale.getDefault()).format(this)
    } catch (e: Exception) {
        null
    }
}

fun LocalDate?.toMonthDayFormat(): String? {
    val locale = Locale.getDefault()
    val isRussian = locale.language.startsWith("ru")

    val dateFormatter = if (isRussian) {
        DateTimeFormatter.ofPattern("d MMM", locale)
    } else {
        DateTimeFormatter.ofPattern("MMM d", locale)
    }

    return this?.format(dateFormatter)
}