package ru.nikfirs.android.traveltracker.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.theme.textField

@Composable
fun CustomTextField(
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    readOnly: Boolean = false,
    required: Boolean = false,
    label: String = "",
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    @DrawableRes leadingIcon: Int? = null,
    @DrawableRes trailingIcon: Int? = null,
    trailingIconImage: ImageVector? = null,
    leadingIconImage: ImageVector? = null,
    onLeadingIconClick: (() -> Unit)? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.textField,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        enabled = enabled,
        label = { Text(if (required) "$label*" else label) },
        trailingIcon = trailingIcon?.let {
            {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickableOnce(onTrailingIconClick != null) {
                            onTrailingIconClick?.invoke()
                        }
                )
            }
        } ?: trailingIconImage?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickableOnce(onTrailingIconClick != null) {
                            onTrailingIconClick?.invoke()
                        }
                )
            }
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickableOnce(onLeadingIconClick != null) {
                            onLeadingIconClick?.invoke()
                        }
                )
            }
        } ?: leadingIconImage?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickableOnce(onLeadingIconClick != null) {
                            onLeadingIconClick?.invoke()
                        }
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        isError = isError,
        supportingText = supportingText?.let { { Text(text = it) } },
        prefix = prefix,
        suffix = suffix,
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource,
        shape = shape,
    )
}

@Composable
fun CustomTextFieldButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    required: Boolean = false,
    label: String = "",
    @DrawableRes leadingIcon: Int? = null,
    @DrawableRes trailingIcon: Int? = null,
    trailingIconImage: ImageVector? = null,
    leadingIconImage: ImageVector? = null,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource, text) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                onClick()
            }
        }
    }
    CustomTextField(
        value = text,
        onValueChange = {},
        enabled = enabled,
        readOnly = true,
        required = required,
        label = label,
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        trailingIconImage = trailingIconImage,
        leadingIconImage = leadingIconImage,
        onTrailingIconClick = onClick,
        onLeadingIconClick = onClick,
        modifier = modifier,
        isError = isError,
        supportingText = supportingText,
        interactionSource = interactionSource,
    )
}