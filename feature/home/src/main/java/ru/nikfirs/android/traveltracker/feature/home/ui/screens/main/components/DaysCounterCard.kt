package ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.domain.MAX_STAY_DAYS
import ru.nikfirs.android.traveltracker.core.domain.model.DaysCalculation
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.card
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.cardElevation
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.counter
import ru.nikfirs.android.traveltracker.feature.home.R
import java.time.LocalDate

@Composable
fun DaysCounterCard(
    daysCalculation: DaysCalculation,
    currentVisa: Visa?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DaysCounter(
            daysUsed = daysCalculation.totalDaysUsed,
            showWarning = daysCalculation.isNearLimit,
            isOverLimit = daysCalculation.isOverLimit
        )

        currentVisa?.let { visa ->
            if (visa.daysUntilExpiry in 1..30) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.visa_day_counter_warning,
                            visa.daysUntilExpiry.toInt(),
                            visa.daysUntilExpiry
                        ),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun DaysCounter(
    daysUsed: Int,
    modifier: Modifier = Modifier,
    maxDays: Int = MAX_STAY_DAYS,
    showWarning: Boolean = false,
    isOverLimit: Boolean = false
) {
    val progress = (daysUsed.toFloat() / maxDays).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progress"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isOverLimit -> MaterialTheme.colorScheme.errorContainer
            showWarning -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.primaryContainer
        },
        label = "backgroundColor"
    )

    val progressColor by animateColorAsState(
        targetValue = when {
            isOverLimit -> MaterialTheme.colorScheme.error
            showWarning -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        },
        label = "progressColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = cardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.visa_days_counter_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(id = R.string.visa_days_counter_period),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = stringResource(
                        id = R.string.visa_days_counter_format,
                        daysUsed,
                        maxDays
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.counter)
                    .background(backgroundColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(progressColor)
                )
            }

            // Warning or status text
            when {
                isOverLimit -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.visa_days_limit_exceeded),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                    )
                }

                showWarning -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val remaining = maxDays - daysUsed
                        if (remaining > 0) {
                            Text(
                                text = pluralStringResource(
                                    id = R.plurals.visa_days_remaining,
                                    remaining,
                                    remaining
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.visa_days_limit_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                else -> {
                    val remaining = maxDays - daysUsed
                    if (remaining > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pluralStringResource(
                                id = R.plurals.visa_days_remaining,
                                remaining,
                                remaining
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DaysCounterPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DaysCounter(daysUsed = 30)
            DaysCounter(daysUsed = 75, showWarning = true)
            DaysCounter(daysUsed = 95, isOverLimit = true)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DaysCounterCardPreview() {
    AppTheme {
        DaysCounterCard(
            daysCalculation = DaysCalculation(
                totalDaysUsed = 45,
                remainingDays = 45,
                periodStart = LocalDate.now().minusDays(179),
                periodEnd = LocalDate.now(),
            ),
            currentVisa = null,
        )
    }
}