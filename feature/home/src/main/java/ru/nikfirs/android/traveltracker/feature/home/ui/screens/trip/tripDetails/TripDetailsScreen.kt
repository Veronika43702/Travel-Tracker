package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.tripDetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.nikfirs.android.traveltracker.core.data.model.TRANSIT
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.model.TripPurpose
import ru.nikfirs.android.traveltracker.core.domain.model.TripSegment
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.ui.component.DarkENScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.DialogTwoRowButton
import ru.nikfirs.android.traveltracker.core.ui.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.ui.component.FullScreenLoadingIndicator
import ru.nikfirs.android.traveltracker.core.ui.ui.component.InfoDataBox
import ru.nikfirs.android.traveltracker.core.ui.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.ui.component.StatusChip
import ru.nikfirs.android.traveltracker.core.ui.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.home.R
import ru.nikfirs.android.traveltracker.feature.home.ui.model.TripSegmentUi
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.components.TripSegmentCard
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.tripDetails.TripDetailsContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.tripDetails.TripDetailsContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.tripDetails.TripDetailsContract.State
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@Composable
fun TripDetailsScreen(
    tripId: Long,
    isEditable: Boolean,
    navigateToEdit: () -> Unit,
    navigateToVisaDetails: (Long) -> Unit,
    navigateBack: () -> Unit,
    viewModel: TripDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(tripId) {
        viewModel.setAction(Action.LoadData(tripId))
    }
    LaunchedEffectResolver(flow = viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateBack -> navigateBack()
        }
    }

    Screen(
        topTitle = stringResource(R.string.home_trip_details_title),
        navigateBack = navigateBack,
    ) {
        TripDetailsContent(
            state = state,
            onAction = viewModel::setAction,
            isEditable = isEditable,
            navigateToEdit = navigateToEdit,
            navigateToVisaDetails = navigateToVisaDetails,
            navigateBack = navigateBack
        )
    }
}

@Composable
private fun TripDetailsContent(
    state: State,
    onAction: (Action) -> Unit,
    isEditable: Boolean,
    navigateToEdit: () -> Unit,
    navigateToVisaDetails: (Long) -> Unit,
    navigateBack: () -> Unit,
    verticalScroll: ScrollState = rememberScrollState(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(verticalScroll),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        state.trip?.let { trip ->
            // Trip Status
            TripStatusSection(trip = trip)

            // Main Trip Info
            TripInfoSection(
                trip = trip,
                visa = state.visa,
                dateFormatter = state.dateFormatter,
                onVisaClick = { navigateToVisaDetails(it) },
            )

            // Trip Segments
            TripSegmentsSection(
                segments = state.segmentsForView,
                dateFormatter = state.dateFormatter,
                isExpand = state.expandSegments,
                onArrowClick = { onAction(Action.ChangeExpandSegment) }
            )
        }

        // Action Buttons
        if (isEditable) {
            Spacer(Modifier.weight(1f))
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                CustomButton(
                    text = stringResource(uiR.string.action_edit),
                    onClick = navigateToEdit,
                    modifier = Modifier.fillMaxWidth()
                )

                CustomButton(
                    text = stringResource(uiR.string.action_delete),
                    onClick = { onAction(Action.ShowDeleteDialog) },
                    secondaryBtn = true,
                    contentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    DialogTwoRowButton(
        message = state.dialogText,
        onRightBtn = { onAction(Action.Delete) },
        onDismiss = { onAction(Action.HideDialog) }
    )

    ErrorDialog(
        message = state.error,
        onDismiss = {
            onAction(Action.SetError())
            if (state.trip == null) {
                navigateBack()
            }
        }
    )

    FullScreenLoadingIndicator(state.isLoading)
}

@Composable
private fun TripStatusSection(
    trip: Trip
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        StatusChip(
            text = when {
                trip.isOngoing -> stringResource(R.string.trip_status_ongoing_details)
                trip.isFuture -> stringResource(R.string.trip_status_planned_details)
                else -> stringResource(R.string.trip_status_completed_details)
            },
            backgroundColor = when {
                trip.isOngoing -> MaterialTheme.colorScheme.secondaryContainer
                trip.isFuture -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                trip.isOngoing -> MaterialTheme.colorScheme.onSecondaryContainer
                trip.isFuture -> MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun TripInfoSection(
    trip: Trip,
    visa: Visa?,
    dateFormatter: DateTimeFormatter,
    onVisaClick: (Long) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Trip Dates
        InfoDataBox(
            header = stringResource(R.string.trip_dates_section),
            data = "${trip.startDate?.format(dateFormatter)} - ${trip.endDate?.format(dateFormatter)}"
        )

        // Duration
        InfoDataBox(
            header = stringResource(R.string.home_trip_details_duration),
            data = if (trip.hasExemptSegment) {
                stringResource(
                    R.string.home_trip_details_days_total_counted,
                    trip.duration.toInt(),
                    trip.countableDays
                )
            } else {
                stringResource(R.string.home_trip_details_days_total_only, trip.duration.toInt())
            }
        )

        // Purpose
        InfoDataBox(
            header = stringResource(R.string.trip_purpose_section),
            data = when (trip.purpose) {
                TripPurpose.TOURISM -> stringResource(uiR.string.purpose_tourism)
                TripPurpose.BUSINESS -> stringResource(uiR.string.purpose_business)
                TripPurpose.FAMILY -> stringResource(uiR.string.purpose_family)
                TripPurpose.MEDICAL -> stringResource(uiR.string.purpose_medical)
                TripPurpose.EDUCATION -> stringResource(uiR.string.purpose_education)
                TripPurpose.OTHER -> stringResource(uiR.string.purpose_other)
            }
        )

        // Linked Visa (if exists)
        visa?.id?.let { visaId ->
            val typeText = when (visa.visaType) {
                VisaCategory.TYPE_C -> stringResource(ru.nikfirs.android.traveltracker.core.ui.R.string.visa_type_c_short)
                VisaCategory.TYPE_D -> stringResource(ru.nikfirs.android.traveltracker.core.ui.R.string.visa_type_d_short)
                VisaCategory.RESIDENCE_PERMIT -> stringResource(ru.nikfirs.android.traveltracker.core.ui.R.string.visa_type_residence_short)
            }
            InfoDataBox(
                header = stringResource(uiR.string.visa_section),
                data = "$typeText (${visa.visaNumber}) ${visa.country}",
                dataColor = MaterialTheme.colorScheme.primary,
                onDataClick = { onVisaClick(visaId) }
            )
        }

        // Notes (if exists)
        if (!trip.notes.isNullOrBlank()) {
            InfoDataBox(
                header = stringResource(uiR.string.notes),
                data = trip.notes ?: ""
            )
        }
    }
}

@Composable
private fun TripSegmentsSection(
    segments: List<TripSegmentUi>,
    dateFormatter: DateTimeFormatter,
    isExpand: Boolean,
    onArrowClick: () -> Unit,
) {
    if (segments.isEmpty()) return

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpand) 180f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "arrow_rotation"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.trip_segments_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickableOnce { onArrowClick() }
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle)
                )
            }

        }
        AnimatedVisibility(
            visible = isExpand,
            enter = expandVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ),
            exit = shrinkVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                segments.forEach { segment ->
                    TripSegmentCard(
                        segment = segment,
                        dateFormatter = dateFormatter,
                        onClick = {},
                    )
                }
            }
        }
    }
}

@LightRUScreenPreview
@DarkENScreenPreview
@Composable
private fun TripDetailsScreenPreview() {
    AppTheme {
        TripDetailsContent(
            state = State(
                expandSegments = true,
                trip = Trip(
                    id = 1,
                    visaId = 1,
                    startDate = LocalDate.now().plusDays(5),
                    endDate = LocalDate.now().plusDays(12),
                    purpose = TripPurpose.TOURISM,
                    notes = "Summer vacation trip to Europe"
                ),
                visa = Visa(
                    id = 1,
                    visaNumber = "123",
                    country = "DE",
                    startDate = LocalDate.now(),
                    expiryDate = LocalDate.now(),
                    durationOfStay = 8,
                    isActive = true,
                ),
                segmentsForView = listOf(
                    TripSegmentUi(
                        country = "DE",
                        startDate = LocalDate.now().plusDays(5),
                        endDate = LocalDate.now().plusDays(8),
                        cities = listOf("Berlin", "Munich"),
                        isExempt = false,
                        color = Color.Green
                    ),
                    TripSegmentUi(
                        country = "FR",
                        startDate = LocalDate.now().plusDays(8),
                        endDate = LocalDate.now().plusDays(12),
                        cities = listOf("Paris"),
                        isExempt = true,
                        color = Color.Magenta
                    ),
                ),
            ),
            isEditable = true,
            onAction = {},
            navigateToEdit = {},
            navigateBack = {},
            navigateToVisaDetails = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TripDetailsOngoingPreview() {
    AppTheme {
        TripDetailsContent(
            state = State(
                trip = Trip(
                    id = 2,
                    startDate = LocalDate.now().minusDays(3),
                    endDate = LocalDate.now().plusDays(4),
                    segments = listOf(
                        TripSegment(
                            country = TRANSIT,
                            startDate = LocalDate.now().minusDays(3),
                            endDate = LocalDate.now().minusDays(2),
                            isExempt = true
                        ),
                        TripSegment(
                            country = "ES",
                            startDate = LocalDate.now().minusDays(2),
                            endDate = LocalDate.now().plusDays(4),
                            cities = listOf("Madrid", "Barcelona"),
                            isExempt = false
                        )
                    ),
                    purpose = TripPurpose.BUSINESS,
                )
            ),
            isEditable = false,
            onAction = {},
            navigateToEdit = {},
            navigateBack = {},
            navigateToVisaDetails = {},
        )
    }
}