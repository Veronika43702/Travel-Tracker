package ru.nikfirs.android.traveltracker.feature.home.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.component.StatusChip
import ru.nikfirs.android.traveltracker.core.ui.component.TravelCard
import ru.nikfirs.android.traveltracker.core.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TripCard(
    trip: Trip,
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
                            trip.isOngoing -> MaterialTheme.colorScheme.secondaryContainer
                            trip.isFuture -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = when (trip.purpose) {
                        TripPurpose.TOURISM -> painterResource(R.drawable.ic_luggage)
                        TripPurpose.BUSINESS -> painterResource(R.drawable.ic_work)
                        TripPurpose.FAMILY -> painterResource(R.drawable.ic_family_restroom)
                        TripPurpose.MEDICAL -> painterResource(R.drawable.ic_local_hospital)
                        TripPurpose.EDUCATION -> painterResource(R.drawable.ic_school)
                        TripPurpose.OTHER -> painterResource(R.drawable.ic_travel_explore)
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = when {
                        trip.isOngoing -> MaterialTheme.colorScheme.onSecondaryContainer
                        trip.isFuture -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
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
                    text = stringResource(R.string.trip_duration_days, trip.duration.toInt()),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                when {
                    trip.isOngoing -> StatusChip(
                        text = stringResource(R.string.trip_ongoing),
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )

                    trip.isFuture -> StatusChip(
                        text = stringResource(R.string.trip_planned),
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = buildString {
                    append(trip.country)
                    trip.city?.let { append(", $it") }
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(
                R.string.trip_dates,
                trip.startDate.format(dateFormatter),
                trip.endDate.format(dateFormatter)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TripCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TripCard(
                trip = Trip(
                    id = 1,
                    startDate = LocalDate.now().minusDays(5),
                    endDate = LocalDate.now().plusDays(5),
                    country = "Germany",
                    city = "Berlin",
                    purpose = TripPurpose.TOURISM,
                    isPlanned = false
                ),
                onClick = {}
            )

            TripCard(
                trip = Trip(
                    id = 2,
                    startDate = LocalDate.now().plusDays(30),
                    endDate = LocalDate.now().plusDays(37),
                    country = "France",
                    city = "Paris",
                    purpose = TripPurpose.BUSINESS,
                    isPlanned = true
                ),
                onClick = {}
            )

            TripCard(
                trip = Trip(
                    id = 3,
                    startDate = LocalDate.now().minusMonths(2),
                    endDate = LocalDate.now().minusMonths(2).plusDays(14),
                    country = "Italy",
                    city = "Rome",
                    purpose = TripPurpose.FAMILY,
                    isPlanned = false
                ),
                onClick = {}
            )
        }
    }
}