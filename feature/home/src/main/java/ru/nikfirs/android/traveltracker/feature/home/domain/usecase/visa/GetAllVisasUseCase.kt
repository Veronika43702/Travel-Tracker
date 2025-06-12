package ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.repository.VisaRepository
import javax.inject.Inject

class GetAllVisasUseCase @Inject constructor(
    private val visaRepository: VisaRepository
) {
    operator fun invoke(): Flow<List<Visa>> {
        return visaRepository.getAllVisas()
    }
}