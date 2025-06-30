package ru.nikfirs.android.traveltracker.feature.settings.domain

import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun setLanguage(language: String)
    fun getLanguage(): Flow<String>
}