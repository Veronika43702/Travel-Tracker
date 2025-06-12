package ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa

import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.repository.VisaRepository
import javax.inject.Inject

class SaveVisaUseCase @Inject constructor(
    private val visaRepository: VisaRepository
) {
    suspend operator fun invoke(visa: Visa): Long {
        return visaRepository.insertVisa(visa)
    }
}