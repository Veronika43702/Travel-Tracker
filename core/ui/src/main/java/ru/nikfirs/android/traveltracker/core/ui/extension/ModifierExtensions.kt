package ru.nikfirs.android.traveltracker.core.ui.extension

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.handler.MultipleEventHandler
import ru.nikfirs.android.traveltracker.core.ui.handler.setDuration
import ru.nikfirs.android.traveltracker.core.ui.model.CustomIndication

fun Modifier.clickableOnce(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    indication: CustomIndication? = null,
    onClick: () -> Unit,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "clickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["indication"] = indication
        properties["onClick"] = onClick
    }
) {
    val multipleEventHandler = remember { MultipleEventHandler.setDuration() }
    this.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        onClick = { multipleEventHandler.processEvent { onClick() } },
        role = role,
        indication = indication?.let { indication.value }
            ?: LocalIndication.current,
        interactionSource = remember { MutableInteractionSource() }
    )
}