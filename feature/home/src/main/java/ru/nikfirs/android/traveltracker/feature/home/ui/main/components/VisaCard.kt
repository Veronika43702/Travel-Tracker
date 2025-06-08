package ru.nikfirs.android.traveltracker.feature.home.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import ru.nikfirs.android.traveltracker.core.domain.model.VisaType
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
                        if (visa.isExpired) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_badge),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (visa.isExpired) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        },
        trailingContent = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatusChip(
                    text = when (visa.entries) {
                        VisaType.SINGLE -> stringResource(R.string.visa_entries_single)
                        VisaType.DOUBLE -> stringResource(R.string.visa_entries_double)
                        VisaType.MULTI -> stringResource(R.string.visa_entries_multi)
                    },
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )

                if (visa.isExpired) {
                    StatusChip(
                        text = stringResource(R.string.visa_expired),
                        backgroundColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                } else if (visa.daysUntilExpiry <= 30) {
                    StatusChip(
                        text = stringResource(R.string.visa_expires_in_days, visa.daysUntilExpiry),
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }
    ) {
        Text(
            text = visa.visaNumber,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "${stringResource(R.string.visa_type_label)}: ${visa.visaType}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(
                    R.string.visa_valid_until,
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
                    visaType = "C",
                    issueDate = LocalDate.now().minusMonths(6),
                    expiryDate = LocalDate.now().plusMonths(6),
                    entries = VisaType.MULTI
                ),
                onClick = {}
            )

            VisaCard(
                visa = Visa(
                    id = 2,
                    visaNumber = "C987654321",
                    visaType = "C",
                    issueDate = LocalDate.now().minusYears(1),
                    expiryDate = LocalDate.now().minusDays(10),
                    entries = VisaType.SINGLE
                ),
                onClick = {}
            )

            VisaCard(
                visa = Visa(
                    id = 3,
                    visaNumber = "C555555555",
                    visaType = "C",
                    issueDate = LocalDate.now().minusMonths(3),
                    expiryDate = LocalDate.now().plusDays(25),
                    entries = VisaType.DOUBLE
                ),
                onClick = {}
            )
        }
    }
}