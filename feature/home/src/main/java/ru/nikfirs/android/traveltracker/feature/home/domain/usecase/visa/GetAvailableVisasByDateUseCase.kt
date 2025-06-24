package ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa

import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.repository.VisaRepository
import java.time.LocalDate
import javax.inject.Inject

class GetAvailableVisasByDateUseCase @Inject constructor(
    private val visaRepository: VisaRepository
) {
    operator fun invoke(startDate: LocalDate): List<Visa> {
        return visaRepository.getAvailableVisasByDate(startDate)
    }
}