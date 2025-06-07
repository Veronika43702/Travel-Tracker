package ru.nikfirs.android.traveltracker.core.ui.model

import androidx.compose.runtime.Immutable
import ru.nikfirs.android.traveltracker.core.ui.navigation.IconType

@Immutable
data class TopBarActionModel(
    val icon: IconType,
    val title: String = "",
    val onClick: () -> Unit,
)
