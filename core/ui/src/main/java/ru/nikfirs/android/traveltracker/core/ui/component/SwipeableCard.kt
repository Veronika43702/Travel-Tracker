package ru.nikfirs.android.traveltracker.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.nikfirs.android.traveltracker.core.ui.theme.card
import kotlin.math.roundToInt


private enum class SwipeState {
    Default,
    Swiped
}

@Composable
fun SwipeableCard(
    modifier: Modifier = Modifier,
    primaryContent: @Composable (onPrimaryClick: (() -> Unit)?) -> Unit,
    secondaryContent: @Composable (setDefaultState: () -> Unit) -> Unit
) {
    val density = LocalDensity.current
    var secondaryContentWidth by remember { mutableFloatStateOf(0f) }
    val anchors = DraggableAnchors {
        SwipeState.Default at 0f
        SwipeState.Swiped at -secondaryContentWidth
    }
    val scope = rememberCoroutineScope()
    val state = remember { AnchoredDraggableState(SwipeState.Default, anchors) }
    LaunchedEffect(secondaryContentWidth) {
        if (secondaryContentWidth > 0f) {
            state.updateAnchors(
                DraggableAnchors {
                    SwipeState.Default at 0f
                    SwipeState.Swiped at -secondaryContentWidth
                }
            )
        }
    }
    fun setDefaultState() {
        if (state.currentValue != SwipeState.Default) {
            scope.launch {
                state.animateTo(SwipeState.Default)
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.card)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Horizontal,
                reverseDirection = false
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .wrapContentWidth()
                .onGloballyPositioned { coordinates ->
                    secondaryContentWidth =
                        with(density) { coordinates.size.width.toDp().toPx() }
                }
        ) {
            secondaryContent(::setDefaultState)
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(x = state.offset.roundToInt(), y = 0) }
                .fillMaxWidth()
        ) {
            primaryContent(
                if (state.currentValue != SwipeState.Default) {
                    { setDefaultState() }
                } else null
            )
        }
    }
}

@Composable
fun EditAndDeleteRow(
    onEditIconClick: () -> Unit,
    onDeleteIconClick: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onEditIconClick) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
        IconButton(onClick = onDeleteIconClick) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}