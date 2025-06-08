package ru.nikfirs.android.traveltracker.core.ui.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ru.nikfirs.android.traveltracker.core.domain.R as domainR
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString

@Composable
fun CustomString?.asString(): String {
    val context = LocalContext.current
    val internal = context.getString(domainR.string.error_unknown)
    return when (this) {
        is CustomString.Text -> value ?: internal
        is CustomString.Resource -> context.getString(resId, *args)
        else -> internal
    }
}