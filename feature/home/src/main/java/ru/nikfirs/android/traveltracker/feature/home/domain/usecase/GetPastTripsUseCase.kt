package ru.nikfirs.android.traveltracker.feature.home.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import javax.inject.Inject

class GetPastTripsUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    operator fun invoke(): Flow<List<Trip>> {
        return tripRepository.getPastTrips()
    }
}