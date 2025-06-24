package ru.nikfirs.android.traveltracker.feature.home.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.nikfirs.android.traveltracker.feature.home.domain.model.HomeData
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.GetTripsFlowByDatesUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.GetVisaFlowByDateUseCase
import java.time.LocalDate
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val getVisaFlowByDateUseCase: GetVisaFlowByDateUseCase,
    private val getTripsFlowByDatesUseCase: GetTripsFlowByDatesUseCase,
) {
    operator fun invoke(startDate: LocalDate): Flow<HomeData> {
        return combine(
            getVisaFlowByDateUseCase.invoke(startDate),
            getTripsFlowByDatesUseCase.invoke(startDate),
        ) { visas, trips ->
            HomeData(
                allVisas = visas,
                allTrips = trips.sortedWith(compareBy({ it.startDate }, { it.endDate }))
            )
        }
    }
}