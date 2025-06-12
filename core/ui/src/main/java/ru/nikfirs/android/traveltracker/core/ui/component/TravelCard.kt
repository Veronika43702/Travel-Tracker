package ru.nikfirs.android.traveltracker.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.core.ui.theme.card
import ru.nikfirs.android.traveltracker.core.ui.theme.chip

@Composable
fun TravelCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leadingContent: @Composable (BoxScope.() -> Unit)? = null,
    trailingContent: @Composable (BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.card)
            .then(
                if (onClick != null) {
                    Modifier.clickableOnce { onClick() }
                } else Modifier
            ),
        shape = MaterialTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingContent?.let { leading ->
                Box(
                    modifier = Modifier.padding(end = 16.dp),
                    content = leading
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                content = content
            )

            trailingContent?.let { trailing ->
                Box(
                    modifier = Modifier.padding(start = 16.dp),
                    content = trailing
                )
            }
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.chip)
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TravelCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TravelCard(
                onClick = {},
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )
                },
                trailingContent = {
                    StatusChip(
                        text = "Active",
                        backgroundColor = MaterialTheme.colorScheme.secondary
                    )
                }
            ) {
                Text("Card Title", style = MaterialTheme.typography.titleMedium)
                Text("Card subtitle", style = MaterialTheme.typography.bodyMedium)
            }

            TravelCard(
                onClick = {}
            ) {
                Text("Simple card without icons", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}