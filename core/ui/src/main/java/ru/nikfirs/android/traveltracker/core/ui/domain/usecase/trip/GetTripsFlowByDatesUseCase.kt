package ru.nikfirs.android.traveltracker.core.ui.domain.usecase.trip

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import java.time.LocalDate
import javax.inject.Inject

class GetTripsFlowByDatesUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    operator fun invoke(startDate: LocalDate): Flow<List<Trip>> {
        return tripRepository.getTripsFlowByDates(startDate, null)
    }
}