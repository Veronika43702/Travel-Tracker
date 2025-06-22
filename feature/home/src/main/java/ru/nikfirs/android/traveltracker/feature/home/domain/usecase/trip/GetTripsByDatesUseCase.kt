package ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip

import ru.nikfirs.android.traveltracker.core.domain.model.Trip
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import java.time.LocalDate
import javax.inject.Inject

class GetTripsByDatesUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    operator suspend fun invoke(startDate: LocalDate, endDate: LocalDate): List<Trip> {
        return tripRepository.getTripsByDates(startDate, endDate)
    }
}