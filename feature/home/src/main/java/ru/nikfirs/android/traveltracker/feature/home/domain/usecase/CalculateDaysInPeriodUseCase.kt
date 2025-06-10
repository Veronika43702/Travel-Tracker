package ru.nikfirs.android.traveltracker.feature.home.domain.usecase

import ru.nikfirs.android.traveltracker.core.domain.model.DaysCalculation
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import java.time.LocalDate
import javax.inject.Inject

class CalculateDaysInPeriodUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(
        periodEnd: LocalDate = LocalDate.now(),
        exemptCountries: Set<String> = emptySet()
    ): DaysCalculation {
        return tripRepository.calculateDaysInPeriod(periodEnd, exemptCountries)
    }
}