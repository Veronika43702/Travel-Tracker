package ru.nikfirs.android.traveltracker.feature.home

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
sealed class HomeRoute {

    @Serializable
    data object Example

}