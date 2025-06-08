package ru.nikfirs.android.traveltracker.core.domain.model

import android.content.Context
import androidx.annotation.StringRes
import ru.nikfirs.android.traveltracker.core.domain.R

sealed class CustomString {

    data class Text(val value: String?) : CustomString()
    class Resource(@StringRes val resId: Int, vararg val args: Any) : CustomString()

    companion object {

        fun text(text: String?): CustomString = Text(text)

        fun resource(stringRes: Int, vararg args: Any): CustomString = Resource(stringRes, args)

        fun internal(): CustomString = Resource(R.string.error_unknown)

    }
}

fun CustomString?.asString(context: Context?): String {
    context ?: return "-"
    val internal = context.getString(R.string.error_unknown)
    this ?: return internal
    return when (this) {
        is CustomString.Text -> value ?: internal
        is CustomString.Resource -> context.getString(resId, *args)
    }
}