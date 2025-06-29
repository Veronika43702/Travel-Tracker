package ru.nikfirs.android.traveltracker.feature.home.ui.model

import ru.nikfirs.android.traveltracker.core.domain.model.Visa

data class VisaUi(
    val visa: Visa,
    val durationLeft: Int,
)