package ru.nikfirs.android.traveltracker.core.ui.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.row

@Composable
fun RadioButtonRow(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    subText: String? = null,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(MaterialTheme.shapes.row)
            .background(Color.Transparent, MaterialTheme.shapes.row)
            .clickableOnce(enabled) { onClick() },
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled
        )

        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )

            subText?.let { subtext ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
        }
    }
}