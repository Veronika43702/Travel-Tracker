package ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip

import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import javax.inject.Inject

class SaveTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(trip: Trip): Long {
        return tripRepository.insertTrip(trip)
    }
}