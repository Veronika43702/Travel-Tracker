package ru.nikfirs.android.traveltracker.feature.home.domain.usecase

import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import java.time.LocalDate
import javax.inject.Inject

class GetCountryStatisticsUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): Map<String, Int> {
        return tripRepository.getCountryStatistics(startDate, endDate)
    }
}