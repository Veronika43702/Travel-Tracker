package ru.nikfirs.android.traveltracker.feature.calendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.nikfirs.android.traveltracker.core.data.model.TRANSIT
import ru.nikfirs.android.traveltracker.core.domain.model.SchengenCountries
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.domain.model.VisaEntries
import ru.nikfirs.android.traveltracker.core.ui.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.VisaCalendar
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.button
import ru.nikfirs.android.traveltracker.feature.calendar.R
import ru.nikfirs.android.traveltracker.feature.calendar.ui.model.DateDataModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@Composable
fun DayInformationCard(
    date: LocalDate,
    dateInfo: DateDataModel,
    onClose: () -> Unit,
    onTripClick: () -> Unit,
    onVisaClick: () -> Unit,
    modifier: Modifier = Modifier,
    formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy"),
) {
    val locale = Locale.getDefault().language
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        // Background overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .clickable(enabled = false) { /* Prevent dismiss on card click */ },
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .padding(bottom = 4.dp)

                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = date.format(formatter),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Header with close button
                        Icon(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.button)
                                .clickableOnce { onClose() }
                                .padding(4.dp),
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.calendar_day_info_close_description),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Scrollable content
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dateInfo.remainingDays?.let { remainingDays ->
                            Text(
                                text = stringResource(
                                    R.string.calendar_day_info_remaining_days,
                                    remainingDays
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Day restored information
                        if (dateInfo.isIncreased == true) {
                            InfoSection {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Green indicator dot
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary)
                                    )

                                    Text(
                                        text = stringResource(R.string.calendar_day_info_day_restored_description),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Trip information
                        dateInfo.trip?.let { trip ->
                            InfoSection(onClick = { onTripClick() }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Red indicator dot
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error)
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        trip.primaryCountry?.let { country ->
                                            Text(
                                                text = if (country == TRANSIT) {
                                                    stringResource(R.string.calendar_trip_segment_transit_option)
                                                } else {
                                                    SchengenCountries.getCountryByCode(country)
                                                        ?.getDisplayName(locale) ?: country
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        trip.startDate?.let { startDate ->
                                            trip.endDate?.let { endDate ->
                                                Text(
                                                    text =
                                                    startDate.format(formatter)
                                                            + " — " +
                                                            endDate.format(formatter),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Visa information
                        dateInfo.visa?.let { visa ->
                            InfoSection(onClick = { onVisaClick() }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Visa indicator dot
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(VisaCalendar)
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = when (visa.visaType) {
                                                VisaCategory.TYPE_C -> stringResource(uiR.string.visa_type_c)
                                                VisaCategory.TYPE_D -> stringResource(uiR.string.visa_type_d)
                                                VisaCategory.RESIDENCE_PERMIT -> stringResource(uiR.string.visa_type_residence_permit)
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Text(
                                            text =
                                            visa.startDate.format(formatter) + " — " +
                                                    visa.expiryDate.format(formatter),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoSection(
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickableOnce { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.padding(12.dp)
        ) {
            content()
        }
    }
}

// Preview для тестирования
@LightRUScreenPreview
@Composable
private fun DayInformationCardPreview() {
    AppTheme {
        // Примерный DateDataModel для превью
        val sampleDateInfo = DateDataModel(
            remainingDays = 45,
            isIncreased = true,
            trip = Trip(
                id = 1,
                startDate = LocalDate.now().minusDays(10),
                endDate = LocalDate.now().minusDays(3),
                segments = listOf(
                    TripSegment(
                        country = "ES",
                        startDate = LocalDate.now().minusDays(10),
                        endDate = LocalDate.now().minusDays(3),
                        cities = listOf("Madrid"),
                        isExempt = true
                    )
                ),
                purpose = TripPurpose.TOURISM,
            ),
            visa = Visa(
                id = 2,
                visaNumber = "D987654321",
                visaType = VisaCategory.TYPE_D,
                country = "Germany",
                startDate = LocalDate.now().minusMonths(3),
                expiryDate = LocalDate.now().plusMonths(9),
                entries = VisaEntries.MULTI,
                durationOfStay = 1,
            )
        )

        DayInformationCard(
            date = LocalDate.now(),
            dateInfo = sampleDateInfo,
            onClose = {},
            onTripClick = {},
            onVisaClick = {},
        )
    }
}