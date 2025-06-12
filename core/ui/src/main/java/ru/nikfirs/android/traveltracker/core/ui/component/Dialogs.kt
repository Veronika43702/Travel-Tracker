package ru.nikfirs.android.traveltracker.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.extension.asString
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.core.ui.theme.dialog

@Composable
fun ErrorDialog(
    message: CustomString?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    message ?: return
    val text = message.asString()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = modifier.clip(MaterialTheme.shapes.dialog),
            shape = MaterialTheme.shapes.dialog,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    text = stringResource(id = R.string.error_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onRetry != null) {
                        CustomButton(
                            text = stringResource(id = R.string.action_cancel),
                            onClick = onDismiss,
                            secondaryBtn = true,
                            modifier = Modifier.weight(1f),
                            smallButton = true,
                        )

                        CustomButton(
                            text = stringResource(id = R.string.error_retry),
                            onClick = onRetry,
                            modifier = Modifier.weight(1f),
                            smallButton = true,
                        )
                    } else {
                        CustomButton(
                            text = stringResource(id = R.string.action_cancel),
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            smallButton = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DialogTwoRowButton(
    message: CustomString?,
    onRightBtn: () -> Unit,
    modifier: Modifier = Modifier,
    onLeftBtn: (() -> Unit)? = null,
    leftBtnText: String = "",
    rightBtnText: String = "",
    title: String? = null,
    onDismiss: () -> Unit = {},
) {
    message ?: return
    val text = message.asString()
    val leftText = leftBtnText.ifBlank { stringResource(id = R.string.action_cancel) }
    val rightText = rightBtnText.ifBlank { stringResource(id = R.string.action_continue) }
    val leftAction = onLeftBtn ?: onDismiss
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier.clip(MaterialTheme.shapes.dialog),
            shape = MaterialTheme.shapes.dialog,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!title.isNullOrBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomButton(
                        text = leftText,
                        onClick = leftAction,
                        secondaryBtn = true,
                        modifier = Modifier.weight(1f),
                        smallButton = true,
                    )

                    CustomButton(
                        text = rightText,
                        onClick = onRightBtn,
                        modifier = Modifier.weight(1f),
                        smallButton = true,
                    )
                }
            }
        }
    }
}

@Composable
fun DialogTwoColumnButton(
    message: CustomString?,
    onRightBtn: () -> Unit,
    modifier: Modifier = Modifier,
    onLeftBtn: (() -> Unit)? = null,
    leftBtnText: String = "",
    rightBtnText: String = "",
    title: String? = null,
    onDismiss: () -> Unit = {},
) {
    message ?: return
    val text = message.asString()
    val leftText = leftBtnText.ifBlank { stringResource(id = R.string.action_cancel) }
    val rightText = rightBtnText.ifBlank { stringResource(id = R.string.action_continue) }
    val leftAction = onLeftBtn ?: onDismiss
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier.clip(MaterialTheme.shapes.dialog),
            shape = MaterialTheme.shapes.dialog,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!title.isNullOrBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomButton(
                        text = rightText,
                        onClick = onRightBtn,
                        modifier = Modifier.fillMaxWidth(),
                        smallButton = true,
                    )

                    CustomButton(
                        text = leftText,
                        onClick = leftAction,
                        secondaryBtn = true,
                        modifier = Modifier.fillMaxWidth(),
                        smallButton = true,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ErrorDialogPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ErrorDialog(
                message = CustomString.text("Не удалось загрузить данные"),
                onDismiss = {},
                onRetry = {}
            )
        }
    }
}

@Preview
@Composable
private fun DialogTwoColumnreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            DialogTwoColumnButton(
                title = "Are you sure?",
                message = CustomString.text("Are you sure you want to delete visa?"),
                onRightBtn = {}
            )
        }
    }
}

@Preview
@Composable
private fun DialogTwoRowPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            DialogTwoRowButton(
                title = "Are you sure?",
                message = CustomString.text("Are you sure you want to delete visa?"),
                onRightBtn = {}
            )
        }
    }
}