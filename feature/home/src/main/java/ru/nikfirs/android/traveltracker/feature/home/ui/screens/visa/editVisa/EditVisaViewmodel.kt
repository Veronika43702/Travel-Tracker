package ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.editVisa

import dagger.hilt.android.lifecycle.HiltViewModel
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.domain.model.VisaEntries
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.mvi.launch
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.GetVisaByIdUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.UpdateVisaUseCase
import ru.nikfirs.android.traveltracker.core.ui.R as uiR
import java.time.LocalDate
import javax.inject.Inject
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.editVisa.EditVisaContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.editVisa.EditVisaContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.editVisa.EditVisaContract.Effect
import java.time.temporal.ChronoUnit

@HiltViewModel
class EditVisaViewModel @Inject constructor(
    private val getVisaByIdUseCase: GetVisaByIdUseCase,
    private val updateVisaUseCase: UpdateVisaUseCase,
) : ViewModel<Action, Effect, State>() {

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.LoadData -> loadVisa(action.visaId)
            is Action.UpdateVisaNumber -> updateVisaNumber(action.number)
            is Action.UpdateVisaType -> updateVisaType(action.type)
            is Action.UpdateCountry -> updateCountry(action.country)
            is Action.UpdateStartDate -> updateStartDate(action.date)
            is Action.UpdateExpiryDate -> updateExpiryDate(action.date)
            is Action.UpdateDurationOfStay -> updateDurationOfStay(action.duration)
            is Action.UpdateEntries -> updateEntries(action.entries)
            is Action.UpdateNotes -> updateNotes(action.notes)
            is Action.SetCountryDropdownExpanded -> setCountryDropdownExpanded(action.expanded)
            is Action.UpdateVisa -> updateVisa()
            is Action.SetError -> setError(error = action.error)
        }
    }

    private fun loadVisa(visaId: Long) {
        launch {
            setState { it.copy(isLoading = true) }
            try {
                val visa = getVisaByIdUseCase.invoke(visaId)
                visa?.let {
                    setState {
                        it.copy(
                            isLoading = false,
                            visaId = visa.id,
                            visaNumber = visa.visaNumber,
                            visaType = visa.visaType,
                            selectedCountry = visa.country,
                            startDate = visa.startDate,
                            expiryDate = visa.expiryDate,
                            durationOfStay = visa.durationOfStay.toString(),
                            entries = visa.entries,
                            notes = visa.notes
                        )
                    }
                } ?: setError(
                    CustomString.resource(uiR.string.error_visa_not_found)
                )
            } catch (e: Exception) {
                setError(CustomString.resource(uiR.string.error_loading_data))
            }
        }
    }

    private fun updateVisaNumber(number: String) {
        setState {
            it.copy(
                visaNumber = number,
                validationErrors = currentState.validationErrors.copy(
                    visaNumberError = if (number.isBlank())
                        CustomString.resource(uiR.string.error_visa_number_required)
                    else null
                )
            )
        }
    }

    private fun updateVisaType(type: VisaCategory) {
        setState { it.copy(visaType = type) }
        updateDuration()
    }

    private fun updateDuration() {
        val visaDuration = (ChronoUnit.DAYS.between(
            currentState.startDate,
            currentState.expiryDate
        ) + 1)

        val stayDuration = when (currentState.visaType) {
            VisaCategory.TYPE_C -> (if (visaDuration < 90) visaDuration else 90).toString()
            else -> (ChronoUnit.DAYS.between(
                currentState.startDate,
                currentState.expiryDate
            ) + 1).toString()
        }
        setState { it.copy(durationOfStay = stayDuration) }
    }


    private fun updateCountry(country: String) {
        setState {
            it.copy(
                selectedCountry = country,
                isCountryDropdownExpanded = false,
                validationErrors = currentState.validationErrors.copy(
                    countryError = null
                )
            )
        }
    }

    private fun updateStartDate(date: LocalDate) {
        val expiryDate = if (currentState.expiryDate == LocalDate.now()) {
            when (currentState.visaType) {
                VisaCategory.TYPE_C -> date.plusDays(30)
                else -> date.plusDays(365)
            }
        } else currentState.expiryDate
        setState {
            it.copy(
                startDate = date,
                expiryDate = expiryDate,
                validationErrors = validateDates(date, expiryDate)
            )
        }
        updateDuration()
    }

    private fun updateExpiryDate(date: LocalDate) {
        setState {
            it.copy(
                expiryDate = date,
                validationErrors = validateDates(currentState.startDate, date)
            )
        }
        updateDuration()
    }

    private fun updateDurationOfStay(duration: String) {
        val filteredDuration = duration.filter { it.isDigit() }
        setState {
            it.copy(
                durationOfStay = filteredDuration,
                validationErrors = currentState.validationErrors.copy(
                    durationError = validateDuration(filteredDuration)
                )
            )
        }
    }

    private fun updateEntries(entries: VisaEntries) {
        setState { it.copy(entries = entries) }
    }

    private fun updateNotes(notes: String) {
        setState { it.copy(notes = notes) }
    }

    private fun setCountryDropdownExpanded(expanded: Boolean) {
        setState { it.copy(isCountryDropdownExpanded = expanded) }
    }

    private fun updateVisa() {
        val id = currentState.visaId ?: return
        val validationErrors = validateForm()

        if (validationErrors.isEmpty()) {
            setState { it.copy(isLoading = true) }

            launch {
                try {
                    val visa = Visa(
                        id = id,
                        visaNumber = currentState.visaNumber,
                        visaType = currentState.visaType,
                        country = currentState.selectedCountry,
                        startDate = currentState.startDate,
                        expiryDate = currentState.expiryDate,
                        durationOfStay = currentState.durationOfStay.toIntOrNull() ?: 90,
                        entries = currentState.entries,
                        notes = currentState.notes
                    )

                    updateVisaUseCase.invoke(visa)

                    setEffect { Effect.NavigateBack }

                } catch (e: Exception) {
                    setState {
                        it.copy(
                            isLoading = false,
                            error = CustomString.resource(uiR.string.error_saving_visa)
                        )
                    }
                }
            }
        } else {
            setState { it.copy(validationErrors = validationErrors) }
            setEffect { Effect.ScrollUp }
        }
    }

    private fun validateForm(): EditVisaContract.ValidationErrors {
        return EditVisaContract.ValidationErrors(
            visaNumberError = if (currentState.visaNumber.isBlank())
                CustomString.resource(uiR.string.error_visa_number_required) else null,
            countryError = if (currentState.selectedCountry.isBlank())
                CustomString.resource(uiR.string.error_country_required) else null,
            durationError = validateDuration(currentState.durationOfStay),
            startDateError = null, // Issue date is always valid as it's set by date picker
            expiryDateError = if (currentState.expiryDate <= currentState.startDate)
                CustomString.resource(uiR.string.error_expiry_date_invalid) else null
        )
    }

    private fun validateDates(
        startDate: LocalDate,
        expiryDate: LocalDate
    ): EditVisaContract.ValidationErrors {
        return currentState.validationErrors.copy(
            expiryDateError = if (expiryDate <= startDate)
                CustomString.resource(uiR.string.error_expiry_date_invalid) else null
        )
    }

    private fun validateDuration(duration: String): CustomString? {
        return when {
            duration.isBlank() -> CustomString.resource(uiR.string.error_duration_required)
            duration.toIntOrNull() == null -> CustomString.resource(uiR.string.error_duration_invalid)
            duration.toInt() <= 0 -> CustomString.resource(uiR.string.error_duration_positive)
            else -> null
        }
    }

    private fun setError(error: CustomString?) {
        setState { it.copy(isLoading = false, error = error) }
    }
}