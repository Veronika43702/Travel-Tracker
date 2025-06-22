package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.utils

import androidx.compose.ui.graphics.Color
import ru.nikfirs.android.traveltracker.feature.home.domain.model.TripSegmentUi
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddTripHolder @Inject constructor() {

//    var segmentsUpdated = false

    var tripStartDate: LocalDate? = null
        private set

    var tripEndDate: LocalDate? = null
        private set

    var segmentList: List<TripSegmentUi> = emptyList()
        private set

    var visaExemptCountry: String? = null

    var currentSegment: TripSegmentUi? = null
        private set

    var segmentIndex: Int? = null
        private set

    fun getSegmentColor(): Color {
        return segmentIndex?.let { getTripSegmentColorByIndex(it) }
            ?: getTripSegmentColorByIndex(segmentList.lastIndex + 1)
    }

    fun addSegmentToList(segment: TripSegmentUi? = currentSegment) {
        val newList = segmentList.toMutableList()
        currentSegment?.let { newList.remove(it) }
        segment?.let { newList.add(it) }
        segmentList = newList
            .sortedWith(compareBy({ it.startDate }, { it.endDate }))
            .mapIndexed { index, item ->
                item.copy(color = getTripSegmentColorByIndex(index))
            }
//        segmentsUpdated = true
    }

    fun getSegmentCities(): String {
        return currentSegment?.cities?.joinToString(", ") ?: ""
    }

    fun deleteSegmentFromList(segment: TripSegmentUi? = currentSegment) {
        segmentList = segmentList.filter { it != segment }.mapIndexed { index, item ->
            item.copy(color = getTripSegmentColorByIndex(index))
        }
//        segmentsUpdated = true
    }

    /**
     * Preparing holder data during trip editing
     */
    fun prepareHolderForTripEdit(
        tripStartDate: LocalDate?,
        tripEndDate: LocalDate?,
        existingSegments: List<TripSegmentUi>,
        exemptCountry: String? = null,
    ) {
        tripStartDate ?: return
        tripEndDate ?: return

        this.tripStartDate = tripStartDate
        this.tripEndDate = tripEndDate
        this.segmentList = existingSegments.sortedWith(compareBy({ it.startDate }, { it.endDate }))
        this.visaExemptCountry = exemptCountry
    }

    /**
     * Preparing data for adding new trip segment
     */
    fun prepareForAddSegment(
        tripStartDate: LocalDate?,
        tripEndDate: LocalDate?,
        existingSegments: List<TripSegmentUi>,
        exemptCountry: String? = null,
    ) {
        tripStartDate ?: return
        tripEndDate ?: return

        this.tripStartDate = tripStartDate
        this.tripEndDate = tripEndDate
        this.segmentList = existingSegments.sortedWith(compareBy({ it.startDate }, { it.endDate }))
        this.visaExemptCountry = exemptCountry
        this.segmentIndex = null
        this.currentSegment = null
    }

    /**
     * Preparing data for editing existing trip segment
     */
    fun prepareForEditSegment(
        tripStartDate: LocalDate?,
        tripEndDate: LocalDate?,
        existingSegments: List<TripSegmentUi>,
        exemptCountry: String? = null,
        segmentIndex: Int,
        segment: TripSegmentUi,
    ) {
        tripStartDate ?: return
        tripEndDate ?: return

        this.tripStartDate = tripStartDate
        this.tripEndDate = tripEndDate
        this.segmentList = existingSegments.sortedWith(compareBy({ it.startDate }, { it.endDate }))
        this.visaExemptCountry = exemptCountry
        this.segmentIndex = segmentIndex
        this.currentSegment = segment
    }

    /**
     * Проверка наличия данных для редактирования
     */
    fun hasTripData(): Boolean {
        return tripStartDate != null && tripEndDate != null
    }

    /**
     * Check whether segment is edited. Returns true when [segmentIndex] is null.
     */
    fun isEditMode(): Boolean {
        return segmentIndex != null
    }

    /**
     * Clears all holder data
     */
    fun clear() {
        tripStartDate = null
        tripEndDate = null
        segmentList = emptyList()
        visaExemptCountry = null
        segmentIndex = null
        currentSegment = null
    }
}