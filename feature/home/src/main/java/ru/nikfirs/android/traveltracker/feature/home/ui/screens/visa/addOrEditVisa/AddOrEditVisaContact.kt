package ru.nikfirs.android.traveltracker.feature.home.ui.screens.visa.addOrEditVisa

import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.model.Country
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.domain.model.VisaEntries
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviAction
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviEffect
import ru.nikfirs.android.traveltracker.core.ui.mvi.MviState
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentContract.Action
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class AddOrEditVisaContract {
    data class State(
        val isLoading: Boolean = false,
        val visaId: Long? = null,
        val visaNumber: String = "",
        val visaType: VisaCategory = VisaCategory.TYPE_C,
        val countryText: String = "",
        val selectedCountry: String = "",
        val countryListToShow: List<Country> = emptyList(),
        val startDate: LocalDate = LocalDate.now(),
        val expiryDate: LocalDate = LocalDate.now(),
        val durationOfStay: String = "1",
        val entries: VisaEntries = VisaEntries.MULTI,
        val notes: String = "",
        val isCountryDropdownExpanded: Boolean = false,
        val error: CustomString? = null,
        val validationErrors: ValidationErrors = ValidationErrors(),
        val dateFormatter: DateTimeFormatter = AppDateFormatModel.getDefault().getFormatter(),
    ) : MviState

    data class ValidationErrors(
        val visaNumberError: CustomString? = null,
        val countryError: CustomString? = null,
        val startDateError: CustomString? = null,
        val expiryDateError: CustomString? = null,
        val durationError: CustomString? = null
    ) {
        fun isEmpty(): Boolean = visaNumberError == null &&
                countryError == null &&
                startDateError == null &&
                expiryDateError == null &&
                durationError == null
    }

    sealed class Action : MviAction {
        data class LoadData(val visaId: Long?) : Action()
        data class UpdateVisaNumber(val number: String) : Action()
        data class UpdateVisaType(val type: VisaCategory) : Action()
        data class UpdateCountry(val country: String) : Action()
        data class UpdateCountryText(
            val value: String,
            val language: String,
            val resetCountry: Boolean = true,
        ) : Action()
        data class UpdateStartDate(val date: LocalDate) : Action()
        data class UpdateExpiryDate(val date: LocalDate) : Action()
        data class UpdateDurationOfStay(val duration: String) : Action()
        data class UpdateEntries(val entries: VisaEntries) : Action()
        data class UpdateNotes(val notes: String) : Action()
        data class SetCountryDropdownExpanded(val expanded: Boolean) : Action()
        data object SaveOrUpdateVisa : Action()
        data class SetError(val error: CustomString? = null) : Action()
    }

    sealed class Effect : MviEffect {
        data object NavigateBack : Effect()
        data object ScrollUp : Effect()
    }
}