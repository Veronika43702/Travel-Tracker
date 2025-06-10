package ru.nikfirs.android.traveltracker.feature.home.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.repository.VisaRepository
import javax.inject.Inject

class GetExemptCountriesUseCase @Inject constructor(
    private val visaRepository: VisaRepository
) {
    operator fun invoke(): Flow<Set<String>> {
        return visaRepository.getExemptCountries()
    }
}