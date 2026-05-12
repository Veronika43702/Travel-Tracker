package ru.nikfirs.android.traveltracker.core.ui.ui.component

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    showBackground = true,
    widthDp = 380,
    heightDp = 780,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    apiLevel = 35,
    locale = "RU"
)
annotation class LightRUScreenPreview

@Preview(
    widthDp = 360,
    heightDp = 780,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    apiLevel = 35,
    locale = "EN"
)
annotation class DarkENScreenPreview



@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    apiLevel = 35,
    locale = "RU"
)
annotation class LightRUPreview

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    apiLevel = 35,
    locale = "EN"
)
annotation class DarkENPreview