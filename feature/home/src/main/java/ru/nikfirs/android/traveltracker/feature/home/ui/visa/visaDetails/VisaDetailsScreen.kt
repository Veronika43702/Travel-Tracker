package ru.nikfirs.android.traveltracker.feature.home.ui.visa.visaDetails

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
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.domain.model.VisaEntries
import ru.nikfirs.android.traveltracker.core.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.component.DarkENScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.component.DialogTwoRowButton
import ru.nikfirs.android.traveltracker.core.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.component.FullScreenLoadingIndicator
import ru.nikfirs.android.traveltracker.core.ui.component.InfoDataBox
import ru.nikfirs.android.traveltracker.core.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.home.ui.utils.VisaAction
import ru.nikfirs.android.traveltracker.feature.home.ui.visa.visaDetails.VisaDetailsContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.visa.visaDetails.VisaDetailsContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.visa.visaDetails.VisaDetailsContract.State
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@Composable
fun VisaDetailsScreen(
    visaId: Long,
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
        topTitle = stringResource(uiR.string.visa_details_title),
        navigateBack = navigateBack,
    ) {
        AddVisaScreenContent(
            state = state,
            onAction = viewModel::setAction,
            navigateToEdit = navigateToEdit,
            navigateBack = navigateBack
        )
    }
}

@Composable
private fun AddVisaScreenContent(
    state: State,
    onAction: (Action) -> Unit,
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
        VisaInfoBox(state.visa, state.dateFormatter)
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
            if (state.visa?.isActive == true) {
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
    dateFormatter: DateTimeFormatter,
) {
    visa ?: return
    if (!visa.isActive) {
        Text(
            text = stringResource(uiR.string.visa_details_annulled),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
    InfoDataBox(
        header = stringResource(uiR.string.visa_number),
        data = visa.visaNumber,
    )
    InfoDataBox(
        header = stringResource(uiR.string.visa_type),
        data = when (visa.visaType) {
            VisaCategory.TYPE_C -> stringResource(uiR.string.visa_type_c_short)
            VisaCategory.TYPE_D -> stringResource(uiR.string.visa_type_d_short)
            VisaCategory.RESIDENCE_PERMIT -> stringResource(uiR.string.visa_type_residence_short)
        },
    )
    InfoDataBox(
        header = stringResource(uiR.string.country),
        data = visa.country,
    )
    InfoDataBox(
        header = stringResource(uiR.string.issue_date),
        data = visa.startDate.format(dateFormatter),
    )
    InfoDataBox(
        header = stringResource(uiR.string.expiry_date),
        data = visa.expiryDate.format(dateFormatter),
    )

    if (visa.visaType == VisaCategory.TYPE_C) {
        InfoDataBox(
            header = stringResource(uiR.string.duration_of_stay),
            data = visa.durationOfStay.toString(),
        )
        InfoDataBox(
            header = stringResource(uiR.string.entries_type),
            data = when (visa.entries) {
                VisaEntries.SINGLE -> stringResource(uiR.string.entries_single)
                VisaEntries.DOUBLE -> stringResource(uiR.string.entries_double)
                VisaEntries.MULTI -> stringResource(uiR.string.entries_multi)
            },
        )
    }
    if (visa.notes.isNotBlank()) {
        InfoDataBox(
            header = stringResource(uiR.string.notes),
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
                    startDate = LocalDate.now(),
                    expiryDate = LocalDate.now(),
                    visaType = VisaCategory.TYPE_C,
                    durationOfStay = 30,
                    notes = "notes",
                    isActive = false,
                )
            ),
            onAction = {},
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
                dialogText = CustomString.resource(uiR.string.visa_annul_dialog)
            ),
            onAction = {},
            navigateToEdit = {},
            navigateBack = {},
        )
    }
}