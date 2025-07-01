package ru.nikfirs.android.traveltracker.core.domain.model

/**
 * Enum class representing available app themes
 */
enum class AppThemeModel {

    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        /**
         * Get theme by string value, returns SYSTEM if not found
         */
        fun fromString(value: String?): AppThemeModel {
            return when (value?.uppercase()) {
                "LIGHT" -> LIGHT
                "DARK" -> DARK
                "SYSTEM" -> SYSTEM
                else -> SYSTEM // default value
            }
        }
    }
}