package ru.nikfirs.android.traveltracker.core.ui.domain.usecase.trip

import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import javax.inject.Inject

class GetTripByIdUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: Long): Trip? {
        return tripRepository.getTripById(tripId)
    }
}