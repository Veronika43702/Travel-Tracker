package ru.nikfirs.android.traveltracker.core.ui.domain.usecase.visa

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.repository.VisaRepository
import java.time.LocalDate
import javax.inject.Inject

class GetVisaFlowByDateUseCase @Inject constructor(
    private val visaRepository: VisaRepository
) {
    operator fun invoke(startDate: LocalDate, onlyActive: Boolean = false): Flow<List<Visa>> {
        return visaRepository.getVisaFlowByDate(startDate, onlyActive)
    }
}