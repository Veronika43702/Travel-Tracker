package ru.nikfirs.android.traveltracker.feature.home.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeData
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.GetAllTripsUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.GetAllVisasUseCase
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val getAllVisasUseCase: GetAllVisasUseCase,
    private val getAllTripsUseCase: GetAllTripsUseCase,
    private val getExemptCountriesUseCase: GetExemptCountriesUseCase
) {
    operator fun invoke(): Flow<HomeData> {
        return combine(
            getAllVisasUseCase(),
            getAllTripsUseCase(),
            getExemptCountriesUseCase()
        ) { visas, trips, exemptCountries ->
            HomeData(
                allVisas = visas,
                allTrips = trips,
                exemptCountries = exemptCountries
            )
        }
    }
}