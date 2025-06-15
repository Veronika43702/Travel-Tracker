package ru.nikfirs.android.traveltracker.feature.home.ui.screens.trip.utils

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddTripHolder @Inject constructor() {

    // Данные для передачи в экран редактирования сегмента
    var tripStartDate: LocalDate? = null
        private set

    var tripEndDate: LocalDate? = null
        private set

    var blockedDates: Set<LocalDate> = emptySet()
        private set

    var segmentIndex: Int? = null
        private set

    var existingCountry: String? = null
        private set

    var existingStartDate: LocalDate? = null
        private set

    var existingEndDate: LocalDate? = null
        private set

    var existingCities: String? = null
        private set

    // Результат от экрана редактирования сегмента
    var segmentResult: SegmentResult? = null
        private set

    var deletedSegmentIndex: Int? = null
        private set

    data class SegmentResult(
        val country: String,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val cities: List<String>,
        val isUpdate: Boolean,
        val segmentIndex: Int?
    )

    /**
     * Подготовка данных для экрана добавления нового сегмента
     */
    fun prepareForAddSegment(
        tripStartDate: LocalDate,
        tripEndDate: LocalDate,
        blockedDates: Set<LocalDate>
    ) {
        this.tripStartDate = tripStartDate
        this.tripEndDate = tripEndDate
        this.blockedDates = blockedDates
        this.segmentIndex = null
        clearExistingSegmentData()
        clearResults()
    }

    /**
     * Подготовка данных для экрана редактирования существующего сегмента
     */
    fun prepareForEditSegment(
        tripStartDate: LocalDate,
        tripEndDate: LocalDate,
        blockedDates: Set<LocalDate>,
        segmentIndex: Int,
        existingCountry: String,
        existingStartDate: LocalDate,
        existingEndDate: LocalDate,
        existingCities: String
    ) {
        this.tripStartDate = tripStartDate
        this.tripEndDate = tripEndDate
        this.blockedDates = blockedDates
        this.segmentIndex = segmentIndex
        this.existingCountry = existingCountry
        this.existingStartDate = existingStartDate
        this.existingEndDate = existingEndDate
        this.existingCities = existingCities
        clearResults()
    }

    /**
     * Сохранение результата работы с сегментом
     */
    fun setSegmentResult(
        country: String,
        startDate: LocalDate,
        endDate: LocalDate,
        cities: List<String>,
        isUpdate: Boolean,
        segmentIndex: Int?
    ) {
        this.segmentResult = SegmentResult(
            country = country,
            startDate = startDate,
            endDate = endDate,
            cities = cities,
            isUpdate = isUpdate,
            segmentIndex = segmentIndex
        )
        this.deletedSegmentIndex = null
    }

    /**
     * Сохранение информации об удаленном сегменте
     */
    fun setDeletedSegmentIndex(index: Int) {
        this.deletedSegmentIndex = index
        this.segmentResult = null
    }

    /**
     * Получение и очистка результата
     */
    fun consumeSegmentResult(): SegmentResult? {
        val result = segmentResult
        segmentResult = null
        return result
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
    fun hasSegmentData(): Boolean {
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
        blockedDates = emptySet()
        segmentIndex = null
        clearExistingSegmentData()
        clearResults()
    }

    /**
     * Очистка данных существующего сегмента
     */
    private fun clearExistingSegmentData() {
        existingCountry = null
        existingStartDate = null
        existingEndDate = null
        existingCities = null
    }

    /**
     * Очистка результатов
     */
    private fun clearResults() {
        segmentResult = null
        deletedSegmentIndex = null
    }
}