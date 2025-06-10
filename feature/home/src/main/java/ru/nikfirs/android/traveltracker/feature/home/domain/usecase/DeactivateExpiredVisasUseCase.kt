package ru.nikfirs.android.traveltracker.feature.home.domain.usecase

import ru.nikfirs.android.traveltracker.core.domain.repository.VisaRepository
import javax.inject.Inject

class DeactivateExpiredVisasUseCase @Inject constructor(
    private val visaRepository: VisaRepository
) {
    suspend operator fun invoke() {
        visaRepository.deactivateExpiredVisas()
    }
}