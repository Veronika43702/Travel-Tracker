package ru.nikfirs.android.traveltracker.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.theme.button

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
    iconImage: ImageVector? = null,
    secondaryBtn: Boolean = false,
    contentColor: Color = Color.Unspecified,
    smallButton: Boolean = false,
    enabled: Boolean = true,
) {
    val colors =
        if (secondaryBtn) {
            ButtonDefaults.textButtonColors(
                contentColor = contentColor,
            )
        } else {
            ButtonDefaults.buttonColors(
                contentColor = contentColor,
            )
        }
    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.button,
        colors = colors,
        modifier = modifier,
        enabled = enabled,
        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp)
    ) {
        iconRes?.let {
            Icon(painter = painterResource(it), contentDescription = null)
        } ?: iconImage?.let { Icon(it, contentDescription = null) }
        if (iconRes != null || iconImage != null) {
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text,
            style = if (smallButton) {
                LocalTextStyle.current
            } else MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = if (smallButton) 8.dp else 14.5.dp)
        )
    }
}