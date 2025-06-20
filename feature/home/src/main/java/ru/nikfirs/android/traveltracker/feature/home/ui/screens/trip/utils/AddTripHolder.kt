package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.utils

import androidx.compose.ui.graphics.Color
import ru.nikfirs.android.traveltracker.feature.home.domain.model.TripSegmentUi
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddTripHolder @Inject constructor() {

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

    companion object {
        private val SEGMENT_COLORS = listOf(
            Color(0xFFFF9800), // Orange
            Color(0xFF9C27B0), // Purple
            Color(0xFF00BCD4), // Cyan
            Color(0xFFF44336), // Red
            Color(0xFF607D8B), // Blue Grey
            Color(0xFFF527FF), // Pink
            Color(0xFF8BC34A), // Light Green
            Color(0xFF2196F3), // Blue
        )
    }

    fun getSegmentColor(): Color {
        return segmentIndex?.let { SEGMENT_COLORS[it % SEGMENT_COLORS.size] }
            ?: segmentList.lastIndex.let { SEGMENT_COLORS[(it + 1) % SEGMENT_COLORS.size] }
    }

    fun getSegmentColorByIndex(index: Int): Color {
        return SEGMENT_COLORS[index % SEGMENT_COLORS.size]
    }

    fun addSegmentToList(segment: TripSegmentUi? = currentSegment) {
        val newList = segmentList.toMutableList()
        currentSegment?.let { newList.remove(it) }
        segment?.let { newList.add(it) }
        segmentList = newList.sortedBy { it.startDate }.mapIndexed { index, item ->
            item.copy(color = getSegmentColorByIndex(index))
        }
    }

    fun getSegmentCities(): String {
        return currentSegment?.cities?.joinToString(", ") ?: ""
    }

    fun deleteSegmentFromList(segment: TripSegmentUi? = currentSegment) {
        segmentList = segmentList.filter { it != segment }.mapIndexed { index, item ->
            item.copy(color = getSegmentColorByIndex(index))
        }
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
        this.segmentList = existingSegments.sortedBy { it.startDate }
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
        this.segmentList = existingSegments.sortedBy { it.startDate }
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