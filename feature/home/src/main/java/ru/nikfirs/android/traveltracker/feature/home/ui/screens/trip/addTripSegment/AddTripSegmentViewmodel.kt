package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import ru.nikfirs.android.traveltracker.core.data.model.TRANSIT
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.SchengenCountries
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.dataStore.GetDateFormatUseCase
import ru.nikfirs.android.traveltracker.core.ui.mvi.ViewModel
import ru.nikfirs.android.traveltracker.core.ui.ui.model.DateRangeSelection
import ru.nikfirs.android.traveltracker.feature.home.R
import ru.nikfirs.android.traveltracker.feature.home.ui.model.TripSegmentUi
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.addTripSegment.AddTripSegmentContract.State
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.common.AddTripCommonContract
import java.time.LocalDate
import javax.inject.Inject
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

@HiltViewModel
class AddTripSegmentViewModel @Inject constructor(
    private val getDateFormatUseCase: GetDateFormatUseCase,
) : ViewModel<Action, Effect, State>() {

    override fun createInitialState(): State = State()

    override fun handleAction(action: Action) {
        when (action) {
            is Action.LoadData -> loadData(action.commonState)
            is Action.SetCountryDropdownExpanded -> setCountryDropdownExpanded(action.expanded)
            is Action.UpdateCountryText -> updateCountryText(
                action.value,
                action.language,
                action.resetCountry
            )

            is Action.UpdateCountry -> updateCountry(action.country)
            is Action.ShowDatePicker -> setState { it.copy(showCalendar = action.value) }
            is Action.UpdateDateRange -> updateDateRange(action.dateRange)
            is Action.OnDateRangeComplete -> onDateRangeComplete(action.startDate, action.endDate)
            is Action.UpdateCities -> updateCities(action.cities)
            is Action.SaveSegment -> saveSegment()
            is Action.DeleteSegment -> deleteSegment()
            is Action.SetError -> setError(action.error)
        }
    }

    /**
     * Loads data concerning segment and trip and saves to State().
     */
    private fun loadData(commonState: AddTripCommonContract.State) {
        launchIO {
            try {
                getDateFormatUseCase.invoke().collectLatest { dateFormat ->
                    setState { it.copy(dateFormatter = dateFormat.getFormatter()) }
                }
            } catch (exception: Exception) {
                Log.e("AddTripSegmentViewModel", "loadTrip, date format", exception)
            }
        }

        val tripStartDate = commonState.tripStartDate
        val tripEndDate = commonState.tripEndDate
        if (tripStartDate == null || tripEndDate == null) {
            setError(CustomString.resource(uiR.string.error_loading_data))
            return
        }

        val editedSegment = commonState.editedSegment
        val isEditMode = commonState.isEditMode

        val startDate = when {
            isEditMode -> editedSegment?.startDate
            commonState.segments.isEmpty() -> tripStartDate
            else -> null
        }
        val endDate = when {
            isEditMode -> editedSegment?.endDate
            commonState.segments.isEmpty() -> tripEndDate
            else -> null
        }

        setState {
            it.copy(
                countryListFull = SchengenCountries.countries,
                countryListToShow = SchengenCountries.countries,
                tripStartDate = tripStartDate,
                tripEndDate = tripEndDate,
                segmentList = commonState.segments.filter { segment ->
                    segment.uid != editedSegment?.uid
                },
                isEditMode = isEditMode,
                country = editedSegment?.country ?: "",
                startDate = startDate,
                endDate = endDate,
                selectedDateRange = DateRangeSelection(startDate, endDate),
                cities = commonState.editedSegmentCities,
                visaExemptCountry = commonState.visaExemptCountry,
            )
        }
    }

    /**
     * Expands dropdown menu with country list
     */
    private fun setCountryDropdownExpanded(expanded: Boolean) {
        setState { it.copy(isCountryDropdownExpanded = expanded) }
    }

    /**
     * Updates country list to view and sets value for text field.
     * Text value for country is formed according to language.
     * @param resetCountry true if country (value for addTripHolder) is need to be reset.
     * If true country value is set to be Blank,
     * if false - get values-ru-ru-ru from [currentState.country][currentState]
     */
    private fun updateCountryText(value: String, language: String, resetCountry: Boolean) {
        val list = currentState.countryListFull.filter {
            if (resetCountry) {
                if (language == "ru") {
                    it.nameRu.lowercase().startsWith(value.lowercase())
                } else {
                    it.nameEn.lowercase().startsWith(value.lowercase())
                }
            } else true
        }

        setState {
            it.copy(
                countryText = value,
                country = if (resetCountry) "" else currentState.country,
                isCountryDropdownExpanded = resetCountry,
                countryListToShow = list,
            )
        }
    }

    /**
     * Updates country for segment info, close dropdown menu and drop country error
     */
    private fun updateCountry(country: String) {
        setState {
            it.copy(
                countryText = country,
                country = country,
                countryListToShow = currentState.countryListFull,
                isCountryDropdownExpanded = false,
                validationErrors = currentState.validationErrors.copy(countryEmptyError = null)
            )
        }
    }

    /**
     * Updates date range in calendar but not for segment info to be able to cancel range changes
     */
    private fun updateDateRange(dateRange: DateRangeSelection) {
        setState { it.copy(selectedDateRange = dateRange) }
    }

    /**
     * Updates date range for segment info and drop date error
     */
    private fun onDateRangeComplete(startDate: LocalDate, endDate: LocalDate) {
        setState {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                validationErrors = currentState.validationErrors.copy(datesError = null)
            )
        }
    }

    /**
     * Updates cities for segment info
     */
    private fun updateCities(cities: String) {
        setState { it.copy(cities = cities) }
    }

    /**
     * Validates the form and, if valid, emits the built segment for the common ViewModel to save.
     */
    private fun saveSegment() {
        val validationErrors = validateForm()

        if (validationErrors.isEmpty()) {
            val citiesList = currentState.cities
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val dateRange = currentState.selectedDateRange
            val startDate = dateRange.startDate
            val endDate = dateRange.endDate

            if (startDate != null && endDate != null) {
                val segment = TripSegmentUi(
                    country = currentState.country,
                    startDate = startDate,
                    endDate = endDate,
                    cities = citiesList,
                    isExempt = currentState.country == currentState.visaExemptCountry,
                )
                setEffect { Effect.SegmentSaved(segment) }
            }
        } else {
            setState { it.copy(validationErrors = validationErrors) }
        }
    }

    private fun validateForm(): AddTripSegmentContract.ValidationErrors {
        val country = currentState.country
        val dateRange = currentState.selectedDateRange

        return AddTripSegmentContract.ValidationErrors(
            countryEmptyError = if (country.isBlank()) {
                CustomString.resource(R.string.home_error_trip_segment_country_required)
            } else null,
            countryNotFromListError = if (
                !(country == TRANSIT || currentState.countryListFull
                    .find { it.code == country } != null)
            ) {
                CustomString.resource(R.string.home_error_trip_segment_country_invalid)
            } else null,
            datesError = if (!dateRange.isComplete) {
                CustomString.resource(R.string.home_error_trip_segment_dates_required)
            } else null
        )
    }

    /**
     * Requests the common ViewModel to delete the currently edited segment.
     */
    private fun deleteSegment() {
        setEffect { Effect.SegmentDeleted }
    }

    private fun setError(error: CustomString?) {
        setState { it.copy(error = error) }
    }
}