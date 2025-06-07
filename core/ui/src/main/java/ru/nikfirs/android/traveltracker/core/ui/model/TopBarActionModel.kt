package ru.nikfirs.android.traveltracker.core.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class TopBarActionModel(
    val icon: IconType? = null,
    val title: String? = null,
    val onClick: () -> Unit,
)
