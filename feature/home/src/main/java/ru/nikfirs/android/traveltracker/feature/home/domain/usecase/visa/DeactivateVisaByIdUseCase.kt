package ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa

import ru.nikfirs.android.traveltracker.core.domain.repository.VisaRepository
import javax.inject.Inject

class DeactivateVisaByIdUseCase @Inject constructor(
    private val visaRepository: VisaRepository
) {
    suspend operator fun invoke(visaId: Long) {
        visaRepository.deactivateVisaById(visaId)
    }
}