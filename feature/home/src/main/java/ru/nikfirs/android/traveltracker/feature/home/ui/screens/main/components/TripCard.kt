package ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.domain.model.SegmentType
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.component.EditAndDeleteRow
import ru.nikfirs.android.traveltracker.core.ui.component.StatusChip
import ru.nikfirs.android.traveltracker.core.ui.component.SwipeableCard
import ru.nikfirs.android.traveltracker.core.ui.component.TravelCard
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.HomeContract.Action
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SwipeableTripCard(
    trip: Trip,
    isExempt: Boolean,
    onAction: (Action) -> Unit,
) {
    SwipeableCard(
        primaryContent = { onPrimaryClick ->
            TripCard(
                trip = trip,
                isExempt = isExempt,
                onClick = {
                    onPrimaryClick?.let { it() }
                        ?: onAction(Action.NavigateToEditTrip(trip))
                }
            )
        },
        secondaryContent = { setDefaultState ->
            EditAndDeleteRow(
                onEditIconClick = {
                    setDefaultState()
                    onAction(Action.NavigateToEditTrip(trip))

                },
                onDeleteIconClick = {
                    onAction(Action.ShowDeleteTripDialog(trip))
                },
            )
        }
    )
}

@Composable
fun TripCard(
    trip: Trip,
    isExempt: Boolean,
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

                if (isExempt) {
                    StatusChip(
                        text = stringResource(R.string.trip_exempt),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) {
        // Страны
        CountriesRow(trip = trip)

        Spacer(modifier = Modifier.height(4.dp))

        // Даты
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

@Composable
private fun CountriesRow(
    trip: Trip,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val countries = trip.countries
        val displayCountries = countries.take(2)

        displayCountries.forEachIndexed { index, country ->
            if (index > 0) {
                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = country,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        if (countries.size > 2) {
            Icon(
                painter = painterResource(R.drawable.ic_more_horiz),
                contentDescription = stringResource(R.string.more_countries),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
                    segments = listOf(
                        TripSegment(
                            country = "Germany",
                            startDate = LocalDate.now().minusDays(5),
                            endDate = LocalDate.now(),
                            type = SegmentType.STAY
                        ),
                        TripSegment(
                            country = "Poland",
                            startDate = LocalDate.now(),
                            endDate = LocalDate.now().plusDays(5),
                            type = SegmentType.STAY
                        )
                    ),
                    purpose = TripPurpose.TOURISM,
                    isPlanned = false
                ),
                isExempt = false,
                onClick = {}
            )

            TripCard(
                trip = Trip(
                    id = 2,
                    startDate = LocalDate.now().plusDays(30),
                    endDate = LocalDate.now().plusDays(37),
                    segments = listOf(
                        TripSegment(
                            country = "France",
                            startDate = LocalDate.now().plusDays(30),
                            endDate = LocalDate.now().plusDays(32),
                            type = SegmentType.STAY,
                            cities = listOf("Paris", "Lyon")
                        ),
                        TripSegment(
                            country = "Spain",
                            startDate = LocalDate.now().plusDays(32),
                            endDate = LocalDate.now().plusDays(35),
                            type = SegmentType.STAY,
                            cities = listOf("Barcelona")
                        ),
                        TripSegment(
                            country = "Italy",
                            startDate = LocalDate.now().plusDays(35),
                            endDate = LocalDate.now().plusDays(37),
                            type = SegmentType.STAY,
                            cities = listOf("Milan")
                        )
                    ),
                    purpose = TripPurpose.BUSINESS,
                    isPlanned = true
                ),
                isExempt = true,
                onClick = {}
            )
        }
    }
}