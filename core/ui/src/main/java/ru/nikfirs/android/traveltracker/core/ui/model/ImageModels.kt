package ru.nikfirs.android.traveltracker.core.ui.model

import androidx.compose.ui.graphics.vector.ImageVector

sealed class IconType {
    data class DrawableRes(val resId: Int) : IconType()
    data class VectorIcon(val imageVector: ImageVector) : IconType()
}