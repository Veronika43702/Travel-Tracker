package ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.data.model.TRANSIT
import ru.nikfirs.android.traveltracker.core.domain.model.SchengenCountries
import ru.nikfirs.android.traveltracker.core.ui.R
import ru.nikfirs.android.traveltracker.core.ui.component.EditAndDeleteRow
import ru.nikfirs.android.traveltracker.core.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.component.StatusChip
import ru.nikfirs.android.traveltracker.core.ui.component.SwipeableCard
import ru.nikfirs.android.traveltracker.core.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.core.ui.theme.card
import ru.nikfirs.android.traveltracker.feature.home.domain.model.TripSegmentUi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SwipeableTripSegmentCard(
    segment: TripSegmentUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"),
) {
    SwipeableCard(
        primaryContent = { onPrimaryClick ->
            TripSegmentCard(
                segment = segment,
                onClick = { onPrimaryClick?.let { it() } ?: onEdit() },
                dateFormatter = dateFormatter,
            )
        },
        secondaryContent = { setDefaultState ->
            EditAndDeleteRow(
                onEditIconClick = {
                    setDefaultState()
                    onEdit()
                },
                onDeleteIconClick = {
                    setDefaultState()
                    onDelete()
                },
            )
        }
    )
}

@Composable
fun TripSegmentCard(
    segment: TripSegmentUi,
    onClick: () -> Unit,
    dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"),
) {
    Card(
        modifier = Modifier
            .clip(MaterialTheme.shapes.card)
            .fillMaxWidth()
            .clickableOnce { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = segment.color,
                                shape = CircleShape
                            )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (segment.country == TRANSIT) {
                                stringResource(R.string.segment_transit_option)
                            } else {
                                SchengenCountries.getCountryByCode(segment.country)?.nameRu
                                    ?: segment.country
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (segment.isExempt) {
                            Text(
                                text = " (${stringResource(R.string.exempt_badge).lowercase()})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    StatusChip(
                        text = pluralStringResource(R.plurals.days_count, segment.duration.toInt(), segment.duration),
                        backgroundColor = if (segment.isExempt) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        contentColor = if (segment.isExempt) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    Text(
                        text = "${segment.startDate.format(dateFormatter)} - ${
                            segment.endDate.format(
                                dateFormatter
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )


                }

                if (segment.cities.isNotEmpty()) {
                    Text(
                        text = segment.cities.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        //   modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@LightRUScreenPreview
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SwipeableTripSegmentCard(
                segment = TripSegmentUi(
                    country = "Germany",
                    startDate = LocalDate.now(),
                    endDate = LocalDate.now().plusDays(3),
                    cities = listOf("Berlin", "Munich"),
                    isExempt = false,
                ),
                onEdit = {},
                onDelete = {},
            )
            TripSegmentCard(
                segment = TripSegmentUi(
                    country = "Germany",
                    startDate = LocalDate.now(),
                    endDate = LocalDate.now().plusDays(3),
                    cities = listOf("Berlin", "Munich"),
                    isExempt = false,
                ),
                onClick = {},
            )

            TripSegmentCard(
                segment = TripSegmentUi(
                    country = "Poland long lone name ",
                    startDate = LocalDate.now().plusDays(3),
                    endDate = LocalDate.now().plusDays(7),
                    // cities = listOf("Warsaw"),
                    color = Color.Magenta,
                    isExempt = true
                ),
                onClick = {},
            )
        }
    }
}