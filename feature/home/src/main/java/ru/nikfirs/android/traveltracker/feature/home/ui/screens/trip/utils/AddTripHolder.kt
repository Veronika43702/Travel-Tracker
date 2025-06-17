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

    var visaExemptCountry: String? = null

    var segmentList: List<TripSegmentUi> = emptyList()
        private set

    var currentSegment: TripSegmentUi? = null
        private set

    var segmentIndex: Int? = null
        private set

    var deletedSegmentIndex: Int? = null
        private set

    companion object {
        // Предустановленные цвета для сегментов
        private val SEGMENT_COLORS = listOf(
            Color(0xFF2196F3), // Blue
            Color(0xFF4CAF50), // Green
            Color(0xFFFF9800), // Orange
            Color(0xFF9C27B0), // Purple
            Color(0xFFF44336), // Red
            Color(0xFF00BCD4), // Cyan
            Color(0xFF8BC34A), // Light Green
            Color(0xFFFF5722), // Deep Orange
            Color(0xFF673AB7), // Deep Purple
            Color(0xFF607D8B), // Blue Grey
        )

        fun getSegmentColor(index: Int): Color {
            return SEGMENT_COLORS[index % SEGMENT_COLORS.size]
        }
    }

    fun getSegmentColor(): Color {
        return segmentIndex?.let { SEGMENT_COLORS[it % SEGMENT_COLORS.size] } ?: Color.Blue
    }

    fun addSegmentToList(segment: TripSegmentUi? = currentSegment) {
        val newList = segmentList.toMutableList()
        segment?.let { newList.add(it) }
        segmentList = newList
    }

    fun getSegmentCities(): String {
        return currentSegment?.cities?.joinToString(", ") ?: ""
    }

    fun deleteSegmentFromList(segment: TripSegmentUi? = currentSegment) {
        segmentList = segmentList.filter { it != segment }
    }

    /**
     * Подготовка данных для экрана добавления нового сегмента
     */
    fun prepareForAddSegment(
        tripStartDate: LocalDate,
        tripEndDate: LocalDate,
        existingSegments: List<TripSegmentUi>
    ) {
        this.tripStartDate = tripStartDate
        this.tripEndDate = tripEndDate
        this.segmentList = existingSegments
        this.segmentIndex = null
        clearCurrentSegmentData()
        clearResults()
    }

    /**
     * Подготовка данных для экрана редактирования существующего сегмента
     */
    fun prepareForEditSegment(
        tripStartDate: LocalDate,
        tripEndDate: LocalDate,
        existingSegments: List<TripSegmentUi>,
        segmentIndex: Int,
        segment: TripSegmentUi,
    ) {
        this.tripStartDate = tripStartDate
        this.tripEndDate = tripEndDate
        this.segmentList = existingSegments
        this.segmentIndex = segmentIndex
        this.currentSegment = segment
        clearResults()
    }

    /**
     * Получение и очистка информации об удаленном сегменте
     */
    fun consumeDeletedSegmentIndex(): Int? {
        val index = deletedSegmentIndex
        deletedSegmentIndex = null
        return index
    }

    /**
     * Проверка наличия данных для редактирования
     */
    fun hasTripData(): Boolean {
        return tripStartDate != null && tripEndDate != null
    }

    /**
     * Проверка режима редактирования
     */
    fun isEditMode(): Boolean {
        return segmentIndex != null
    }

    /**
     * Полная очистка всех данных
     */
    fun clear() {
        tripStartDate = null
        tripEndDate = null
        segmentList = emptyList()
        segmentIndex = null
        clearCurrentSegmentData()
        clearResults()
    }

    /**
     * Очистка данных существующего сегмента
     */
    private fun clearCurrentSegmentData() {
        currentSegment = null
    }

    /**
     * Очистка результатов
     */
    private fun clearResults() {
        currentSegment = null
        deletedSegmentIndex = null
    }
}