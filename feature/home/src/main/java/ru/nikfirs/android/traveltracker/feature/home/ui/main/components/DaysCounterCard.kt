package ru.nikfirs.android.traveltracker.feature.home.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.domain.model.DaysCalculation
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.component.DaysCounter
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import java.time.LocalDate

@Composable
fun DaysCounterCard(
    daysCalculation: DaysCalculation,
    currentVisa: Visa?,
    exemptCountries: Set<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DaysCounter(
            daysUsed = daysCalculation.totalDaysUsed,
            maxDays = DaysCalculation.MAX_STAY_DAYS,
            showWarning = daysCalculation.isNearLimit,
            isOverLimit = daysCalculation.isOverLimit
        )

        if (exemptCountries.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = stringResource(
                            R.string.exempt_countries_info,
                            exemptCountries.joinToString(", ")
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        currentVisa?.let { visa ->
            if (visa.daysUntilExpiry in 1..30) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = stringResource(
                            R.string.visa_expiring_soon,
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
                exemptCountries = setOf("Germany")
            ),
            currentVisa = null,
            exemptCountries = setOf("Germany", "Poland")
        )
    }
}