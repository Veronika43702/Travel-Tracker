package ru.nikfirs.android.traveltracker.core.domain.model

import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Enum class representing available date formats
 */
enum class AppDateFormatModel(
    val pattern: String,
    val displayKey: String
) {
    DD_MM_YYYY_DOTS("dd.MM.yyyy", "dd_mm_yyyy_dots"),
    DD_MM_YYYY_SLASHES("dd/MM/yyyy", "dd_mm_yyyy_slashes"),
    MM_DD_YYYY("MM/dd/yyyy", "mm_dd_yyyy"),
    YYYY_MM_DD("yyyy-MM-dd", "yyyy_mm_dd"),
    DD_MMM_YYYY("dd MMM yyyy", "dd_mmm_yyyy");

    /**
     * Get DateTimeFormatter for this format
     */
    fun getFormatter(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    }

    companion object {
        /**
         * Get date format by string value, returns DD_MM_YYYY if not found
         */
        fun fromString(value: String?): AppDateFormatModel {
            return when (value?.uppercase()) {
                "DD_MM_YYYY_DOTS" -> DD_MM_YYYY_DOTS
                "DD_MM_YYYY_SLASHES" -> DD_MM_YYYY_SLASHES
                "MM_DD_YYYY" -> MM_DD_YYYY
                "YYYY_MM_DD" -> YYYY_MM_DD
                "DD_MMM_YYYY" -> DD_MMM_YYYY
                else -> DD_MM_YYYY_DOTS // default value
            }
        }

        /**
         * Get default date format for current locale
         */
        fun getDefault(): AppDateFormatModel {
            val locale = Locale.getDefault()
            return when {
                locale.language.startsWith("ru") -> DD_MM_YYYY_DOTS
                locale.country.equals("US", ignoreCase = true) -> MM_DD_YYYY
                locale.country.equals("GB", ignoreCase = true) -> DD_MM_YYYY_SLASHES
                locale.country.equals("AU", ignoreCase = true) -> DD_MM_YYYY_SLASHES
                else -> DD_MM_YYYY_DOTS
            }
        }
    }
}