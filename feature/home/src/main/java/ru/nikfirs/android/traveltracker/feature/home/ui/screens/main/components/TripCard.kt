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
import ru.nikfirs.android.traveltracker.core.data.model.TRANSIT
import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.model.SchengenCountries
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import ru.nikfirs.android.traveltracker.core.ui.R as uiR
import ru.nikfirs.android.traveltracker.core.ui.ui.component.EditAndDeleteRow
import ru.nikfirs.android.traveltracker.core.ui.ui.component.StatusChip
import ru.nikfirs.android.traveltracker.core.ui.ui.component.SwipeableCard
import ru.nikfirs.android.traveltracker.core.ui.ui.component.TravelCard
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.home.R
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.HomeContract.Action
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SwipeableTripCard(
    trip: Trip,
    isExempt: Boolean,
    countableDuration: Int,
    dateFormatter: DateTimeFormatter,
    onAction: (Action) -> Unit,
) {
    SwipeableCard(
        primaryContent = { onPrimaryClick ->
            TripCard(
                trip = trip,
                isExempt = isExempt,
                countableDuration = countableDuration,
                dateFormatter = dateFormatter,
                onClick = {
                    onPrimaryClick?.let { it() }
                        ?: onAction(Action.NavigateToTripDetails(trip.id))
                }
            )
        },
        secondaryContent = { setDefaultState ->
            EditAndDeleteRow(
                onEditIconClick = {
                    setDefaultState()
                    onAction(Action.NavigateToEditTrip(trip.id))

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
    countableDuration: Int,
    onClick: () -> Unit,
    dateFormatter: DateTimeFormatter,
    modifier: Modifier = Modifier
) {
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
                        TripPurpose.TOURISM -> painterResource(uiR.drawable.ic_luggage)
                        TripPurpose.BUSINESS -> painterResource(uiR.drawable.ic_work)
                        TripPurpose.FAMILY -> painterResource(uiR.drawable.ic_family_restroom)
                        TripPurpose.MEDICAL -> painterResource(uiR.drawable.ic_local_hospital)
                        TripPurpose.EDUCATION -> painterResource(uiR.drawable.ic_school)
                        TripPurpose.OTHER -> painterResource(uiR.drawable.ic_travel_explore)
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
                    text = stringResource(R.string.trip_card_duration_days, trip.duration.toInt()),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                if (isExempt) {
                    StatusChip(
                        text = stringResource(
                            R.string.home_trip_countable_days,
                            countableDuration
                        ),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(Modifier.height(24.dp))
                }

                when {
                    trip.isOngoing -> StatusChip(
                        text = stringResource(R.string.trip_card_status_ongoing),
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )

                    trip.isFuture -> StatusChip(
                        text = stringResource(R.string.trip_card_status_planned),
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        },
        warning = if (trip.hasOverLimitDay) {
            {
                StatusChip(
                    text = stringResource(R.string.home_trip_over_limit_warning),
                    backgroundColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            }
        } else null,
    ) {
        CountriesRow(trip = trip)

        Spacer(modifier = Modifier.height(4.dp))

        if (trip.startDate != null && trip.endDate != null) {
            Text(
                text = stringResource(
                    R.string.trip_card_dates,
                    trip.startDate?.format(dateFormatter) ?: "",
                    trip.endDate?.format(dateFormatter) ?: "",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CountriesRow(
    trip: Trip,
    modifier: Modifier = Modifier
) {
    val locale = Locale.getDefault().language
    val countries = trip.countries.map { country ->
        when {
            country == TRANSIT -> stringResource(R.string.home_trip_segment_transit_option)
            country.isNotBlank() -> {
                SchengenCountries.getCountryByCode(country)
                    ?.getDisplayName(locale)
                    ?: country
            }

            else -> ""
        }
    }

    val primaryCountry = when {
        trip.primaryCountry == TRANSIT -> stringResource(R.string.home_trip_segment_transit_option)
        trip.primaryCountry?.isNotBlank() == true -> {
            trip.primaryCountry?.let {
                SchengenCountries.getCountryByCode(it)
                    ?.getDisplayName(locale) ?: trip.primaryCountry
            }
        }

        else -> null
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = (primaryCountry ?: countries.firstOrNull() ?: "") +
                    (if (countries.size >= 2) {
                        ", " + (countries.firstOrNull { it != primaryCountry }
                            ?: "")
                    } else "")
                    + if (countries.size > 2) "..." else "",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
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
                    segments = listOf(
                        TripSegment(
                            country = "DE",
                            startDate = LocalDate.now().minusDays(5),
                            endDate = LocalDate.now(),
                            isExempt = false
                        ),
                        TripSegment(
                            country = "Poland",
                            startDate = LocalDate.now(),
                            endDate = LocalDate.now().plusDays(5),
                            isExempt = false
                        )
                    ),
                    purpose = TripPurpose.TOURISM,
                    hasOverLimitDay = true,
                ),
                isExempt = true,
                countableDuration = 0,
                onClick = {},
                dateFormatter = AppDateFormatModel.getDefault().getFormatter(),
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
                            cities = listOf("Paris", "Lyon"),
                            isExempt = false
                        ),
                        TripSegment(
                            country = "Spain",
                            startDate = LocalDate.now().plusDays(32),
                            endDate = LocalDate.now().plusDays(35),
                            cities = listOf("Barcelona"),
                            isExempt = true
                        ),
                        TripSegment(
                            country = "Italy",
                            startDate = LocalDate.now().plusDays(35),
                            endDate = LocalDate.now().plusDays(37),
                            cities = listOf("Milan"),
                            isExempt = false
                        )
                    ),
                    purpose = TripPurpose.BUSINESS,
                ),
                isExempt = true,
                countableDuration = 3,
                onClick = {},
                dateFormatter = AppDateFormatModel.getDefault().getFormatter(),
            )
        }
    }
}