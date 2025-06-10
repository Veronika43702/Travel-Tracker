package ru.nikfirs.android.traveltracker.feature.home.domain.usecase

import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.repository.VisaRepository
import javax.inject.Inject

class DeleteVisaUseCase @Inject constructor(
    private val visaRepository: VisaRepository
) {
    suspend operator fun invoke(visa: Visa) {
        visaRepository.deleteVisa(visa)
    }
}