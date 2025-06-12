package ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.addVisa

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.ui.component.CustomButton
import ru.nikfirs.android.traveltracker.core.ui.component.DarkENScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.component.ErrorDialog
import ru.nikfirs.android.traveltracker.core.ui.component.FullScreenLoadingIndicator
import ru.nikfirs.android.traveltracker.core.ui.component.LightRUScreenPreview
import ru.nikfirs.android.traveltracker.core.ui.component.Screen
import ru.nikfirs.android.traveltracker.core.ui.mvi.LaunchedEffectResolver
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme
import ru.nikfirs.android.traveltracker.feature.home.ui.components.VisaInfoBox
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.addVisa.AddVisaContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.addVisa.AddVisaContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.addVisa.AddVisaContract.State
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@Composable
fun AddVisaScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddVisaViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val verticalScroll = rememberScrollState()
    LaunchedEffectResolver(flow = viewModel.effect) { effect ->
        when (effect) {
            is Effect.NavigateBack -> onNavigateBack()
            Effect.ScrollUp -> scope.launch { verticalScroll.scrollTo(0) }
        }
    }

    Screen(
        topTitle = stringResource(uiR.string.add_visa_title),
        navigateBack = onNavigateBack,
    ) {
        AddVisaScreenContent(
            state = state,
            onAction = viewModel::setAction,
            verticalScroll = verticalScroll
        )
    }
}

@Composable
private fun AddVisaScreenContent(
    state: State,
    onAction: (Action) -> Unit,
    verticalScroll: ScrollState = rememberScrollState(),
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(verticalScroll)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VisaInfoBox(
            visaNumber = state.visaNumber,
            visaType = state.visaType,
            selectedCountry = state.selectedCountry,
            isCountryDropdownExpanded = state.isCountryDropdownExpanded,
            startDate = state.startDate,
            expiryDate = state.expiryDate,
            durationOfStay = state.durationOfStay,
            entries = state.entries,
            notes = state.notes,
            visaNumberError = state.validationErrors.visaNumberError,
            countryError = state.validationErrors.countryError,
            startDateError = state.validationErrors.startDateError,
            expiryDateError = state.validationErrors.expiryDateError,
            durationError = state.validationErrors.durationError,
            updateVisaNumber = { onAction(Action.UpdateVisaNumber(it)) },
            updateVisaType = { onAction(Action.UpdateVisaType(it)) },
            setCountryDropdownExpanded = { onAction(Action.SetCountryDropdownExpanded(it)) },
            updateCountry = { onAction(Action.UpdateCountry(it)) },
            updateStartDate = { onAction(Action.UpdateStartDate(it)) },
            updateExpiryDate = { onAction(Action.UpdateExpiryDate(it)) },
            updateDurationOfStay = { onAction(Action.UpdateDurationOfStay(it)) },
            updateEntries = { onAction(Action.UpdateEntries(it)) },
            updateNotes = { onAction(Action.UpdateNotes(it)) },
            focusManager = focusManager,
        )
        Spacer(Modifier.weight(1f))
        CustomButton(
            text = stringResource(uiR.string.action_save),
            onClick = { onAction(Action.SaveVisa) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally)
        )
    }

    ErrorDialog(
        message = state.error,
        onDismiss = { onAction(Action.DismissError) }
    )

    FullScreenLoadingIndicator(state.isLoading)
}


@LightRUScreenPreview
@DarkENScreenPreview
@Composable
private fun AddVisaScreenPreview1() {
    AppTheme {
        AddVisaScreenContent(
            state = State(visaType = VisaCategory.TYPE_C),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddVisaScreenPreview2() {
    AppTheme {
        AddVisaScreenContent(
            state = State(visaType = VisaCategory.TYPE_D),
            onAction = {},
        )
    }
}