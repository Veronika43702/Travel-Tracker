package ru.nikfirs.android.traveltracker.feature.home.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeData
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.GetAllTripsUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.GetActiveVisasUseCase
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val getActiveVisasUseCase: GetActiveVisasUseCase,
    private val getAllTripsUseCase: GetAllTripsUseCase,
    private val getExemptCountriesUseCase: GetExemptCountriesUseCase
) {
    operator fun invoke(): Flow<HomeData> {
        return combine(
            getActiveVisasUseCase(),
            getAllTripsUseCase(),
            getExemptCountriesUseCase()
        ) { visas, trips, exemptCountries ->
            HomeData(
                activeVisas = visas,
                allTrips = trips,
                exemptCountries = exemptCountries
            )
        }
    }
}