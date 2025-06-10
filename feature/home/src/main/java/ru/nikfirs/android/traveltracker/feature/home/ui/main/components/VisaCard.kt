package ru.nikfirs.android.traveltracker.feature.home.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaEntries
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.component.StatusChip
import ru.nikfirs.android.traveltracker.core.ui.component.TravelCard
import ru.nikfirs.android.traveltracker.core.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun VisaCard(
    visa: Visa,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    TravelCard(
        modifier = modifier,
        onClick = onClick,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            visa.isExpired -> MaterialTheme.colorScheme.errorContainer
                            visa.visaType == VisaCategory.RESIDENCE_PERMIT -> MaterialTheme.colorScheme.secondaryContainer
                            visa.visaType == VisaCategory.TYPE_D -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_badge),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = when {
                        visa.isExpired -> MaterialTheme.colorScheme.onErrorContainer
                        visa.visaType == VisaCategory.RESIDENCE_PERMIT -> MaterialTheme.colorScheme.onSecondaryContainer
                        visa.visaType == VisaCategory.TYPE_D -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        },
        trailingContent = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Тип визы
                StatusChip(
                    text = when (visa.visaType) {
                        VisaCategory.TYPE_C -> stringResource(R.string.visa_type_c_short)
                        VisaCategory.TYPE_D -> stringResource(R.string.visa_type_d_short)
                        VisaCategory.RESIDENCE_PERMIT -> stringResource(R.string.visa_type_residence_short)
                    },
                    backgroundColor = when (visa.visaType) {
                        VisaCategory.TYPE_C -> MaterialTheme.colorScheme.primaryContainer
                        VisaCategory.TYPE_D -> MaterialTheme.colorScheme.tertiaryContainer
                        VisaCategory.RESIDENCE_PERMIT -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    contentColor = when (visa.visaType) {
                        VisaCategory.TYPE_C -> MaterialTheme.colorScheme.onPrimaryContainer
                        VisaCategory.TYPE_D -> MaterialTheme.colorScheme.onTertiaryContainer
                        VisaCategory.RESIDENCE_PERMIT -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )

                // Тип въездов для визы C
                if (visa.visaType == VisaCategory.TYPE_C) {
                    StatusChip(
                        text = when (visa.entries) {
                            VisaEntries.SINGLE -> stringResource(R.string.visa_entries_single)
                            VisaEntries.DOUBLE -> stringResource(R.string.visa_entries_double)
                            VisaEntries.MULTI -> stringResource(R.string.visa_entries_multi)
                        },
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Статус
                when {
                    visa.isExpired -> StatusChip(
                        text = stringResource(R.string.visa_expired),
                        backgroundColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                    visa.daysUntilExpiry <= 30 -> StatusChip(
                        text = stringResource(R.string.visa_expires_in_days, visa.daysUntilExpiry),
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Номер визы и страна
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = visa.visaNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                visa.country?.let { country ->
                    Text(
                        text = "• $country",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Срок действия
            Text(
                text = stringResource(
                    R.string.visa_valid_period,
                    visa.issueDate.format(dateFormatter),
                    visa.expiryDate.format(dateFormatter)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = if (visa.isExpired) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VisaCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VisaCard(
                visa = Visa(
                    id = 1,
                    visaNumber = "C123456789",
                    visaType = VisaCategory.TYPE_C,
                    issueDate = LocalDate.now().minusMonths(6),
                    expiryDate = LocalDate.now().plusMonths(6),
                    entries = VisaEntries.MULTI,
                    durationOfStay = 1,
                ),
                onClick = {}
            )

            VisaCard(
                visa = Visa(
                    id = 2,
                    visaNumber = "D987654321",
                    visaType = VisaCategory.TYPE_D,
                    country = "Germany",
                    issueDate = LocalDate.now().minusMonths(3),
                    expiryDate = LocalDate.now().plusMonths(9),
                    entries = VisaEntries.MULTI,
                    durationOfStay = 1,
                ),
                onClick = {}
            )

            VisaCard(
                visa = Visa(
                    id = 3,
                    visaNumber = "RP555555555",
                    visaType = VisaCategory.RESIDENCE_PERMIT,
                    country = "Poland",
                    issueDate = LocalDate.now().minusYears(1),
                    expiryDate = LocalDate.now().plusYears(1),
                    entries = VisaEntries.MULTI,
                    durationOfStay = 1,
                ),
                onClick = {}
            )

            VisaCard(
                visa = Visa(
                    id = 4,
                    visaNumber = "C111111111",
                    visaType = VisaCategory.TYPE_C,
                    issueDate = LocalDate.now().minusYears(1),
                    expiryDate = LocalDate.now().minusDays(10),
                    entries = VisaEntries.SINGLE,
                    durationOfStay = 1,
                ),
                onClick = {}
            )
        }
    }
}