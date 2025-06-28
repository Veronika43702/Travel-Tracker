package ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.SchengenCountries
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.domain.model.VisaEntries
import ru.nikfirs.android.traveltracker.core.ui.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.ui.component.DarkENScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.DialogTwoRowButton
import ru.nikfirs.android.traveltracker.core.ui.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.ui.component.FullScreenLoadingIndicator
import ru.nikfirs.android.traveltracker.core.ui.ui.component.InfoDataBox
import ru.nikfirs.android.traveltracker.core.ui.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.home.R
import ru.nikfirs.android.traveltracker.feature.home.ui.utils.VisaAction
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails.VisaDetailsContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails.VisaDetailsContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.visaDetails.VisaDetailsContract.State
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@Composable
fun VisaDetailsScreen(
    visaId: Long,
    isEditable: Boolean,
    navigateToEdit: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: VisaDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(visaId) {
        viewModel.setAction(Action.LoadData(visaId))
    }
    LaunchedEffectResolver(flow = viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateBack -> navigateBack()
        }
    }
    Screen(
        topTitle = stringResource(R.string.home_visa_details_title),
        navigateBack = navigateBack,
    ) {
        AddVisaScreenContent(
            state = state,
            onAction = viewModel::setAction,
            isEditable = isEditable,
            navigateToEdit = navigateToEdit,
            navigateBack = navigateBack
        )
    }
}

@Composable
private fun AddVisaScreenContent(
    state: State,
    onAction: (Action) -> Unit,
    isEditable: Boolean,
    navigateToEdit: () -> Unit,
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
        VisaInfoBox(state.visa, state.daysLeft, state.dateFormatter)
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
                    modifier = Modifier
                        .fillMaxWidth()
                )
                if (state.visa?.isActive == true && !state.visa.isExpired) {
                    CustomButton(
                        text = stringResource(uiR.string.action_annul),
                        onClick = { onAction(Action.ShowAnnulDialog) },
                        secondaryBtn = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
        onRightBtn = {
            when (state.action) {
                VisaAction.ANNUL -> onAction(Action.Annul)
                VisaAction.DELETE -> onAction(Action.Delete)
                null -> {}
            }
        },
        onDismiss = { onAction(Action.HideDialog) }
    )

    ErrorDialog(
        message = state.error,
        onDismiss = {
            onAction(Action.SetError())
            if (state.visa == null) {
                navigateBack()
            }
        }
    )

    FullScreenLoadingIndicator(state.isLoading)
}

@Composable
fun VisaInfoBox(
    visa: Visa?,
    daysLeft: Int?,
    dateFormatter: DateTimeFormatter,
) {
    visa ?: return
    val locale = java.util.Locale.getDefault().language
    if (!visa.isActive) {
        Text(
            text = stringResource(R.string.home_visa_details_annulled),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
    InfoDataBox(
        header = stringResource(R.string.home_visa_number),
        data = visa.visaNumber,
    )
    InfoDataBox(
        header = stringResource(R.string.home_visa_type),
        data = when (visa.visaType) {
            VisaCategory.TYPE_C -> stringResource(uiR.string.visa_type_c)
            VisaCategory.TYPE_D -> stringResource(uiR.string.visa_type_d)
            VisaCategory.RESIDENCE_PERMIT -> stringResource(uiR.string.visa_type_residence_permit)
        },
    )
    InfoDataBox(
        header = stringResource(R.string.home_visa_country),
        data = SchengenCountries.getCountryByCode(visa.country)
            ?.getDisplayNameWithCode(locale) ?: visa.country,
    )
    InfoDataBox(
        header = stringResource(R.string.home_visa_issue_date),
        data = visa.startDate.format(dateFormatter),
    )
    InfoDataBox(
        header = stringResource(R.string.home_visa_expiry_date),
        data = visa.expiryDate.format(dateFormatter),
    )

    if (visa.visaType == VisaCategory.TYPE_C) {
        InfoDataBox(
            header = stringResource(R.string.home_visa_visa_duration),
            data = visa.durationOfStay.toString(),
        )
        daysLeft?.let {
            InfoDataBox(
                header = stringResource(R.string.home_visa_details_duration_left),
                data = it.toString(),
            )
        }
        InfoDataBox(
            header = stringResource(R.string.home_visa_entries_type),
            data = when (visa.entries) {
                VisaEntries.SINGLE -> stringResource(R.string.home_visa_entries_single)
                VisaEntries.DOUBLE -> stringResource(R.string.home_visa_entries_double)
                VisaEntries.MULTI -> stringResource(R.string.home_visa_entries_multi)
            },
        )
    }
    if (visa.notes.isNotBlank()) {
        InfoDataBox(
            header = stringResource(R.string.home_notes),
            data = visa.notes,
        )
    }
}

@LightRUScreenPreview
@DarkENScreenPreview
@Composable
private fun AddVisaScreenPreview1() {
    AppTheme {
        AddVisaScreenContent(
            state = State(
                visa = Visa(
                    visaNumber = "123",
                    country = "CZ",
                    startDate = LocalDate.now(),
                    expiryDate = LocalDate.now(),
                    visaType = VisaCategory.TYPE_C,
                    durationOfStay = 30,
                    notes = "notes",
                    isActive = false,
                )
            ),
            onAction = {},
            isEditable = false,
            navigateToEdit = {},
            navigateBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddVisaScreenPreview2() {
    AppTheme {
        AddVisaScreenContent(
            state = State(
                visa = Visa(
                    visaNumber = "123",
                    startDate = LocalDate.now(),
                    expiryDate = LocalDate.now(),
                    visaType = VisaCategory.TYPE_D,
                    durationOfStay = 365,
                    notes = "notes",
                ),
                dialogText = CustomString.resource(R.string.home_visa_dialog_annul)
            ),
            onAction = {},
            isEditable = true,
            navigateToEdit = {},
            navigateBack = {},
        )
    }
}