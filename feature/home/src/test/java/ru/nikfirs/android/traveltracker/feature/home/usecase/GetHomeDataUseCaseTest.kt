package ru.nikfirs.android.traveltracker.feature.home.usecase

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ru.nikfirs.android.traveltracker.core.domain.model.testTrip
import ru.nikfirs.android.traveltracker.core.domain.model.testVisa
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.trip.GetTripsFlowByDatesUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.visa.GetVisaFlowByDateUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.GetHomeDataUseCase
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class GetHomeDataUseCaseTest {
    private val getVisaFlow = mockk<GetVisaFlowByDateUseCase>()
    private val getTripsFlow = mockk<GetTripsFlowByDatesUseCase>()
    private val useCase = GetHomeDataUseCase(getVisaFlow, getTripsFlow)

    private val startDate = LocalDate.of(2026, 3, 1)

    @Test
    fun `emits home data combining visas and trips`() = runTest {
        // given
        every { getVisaFlow.invoke(startDate) } returns flowOf(listOf(testVisa()))
        every { getTripsFlow.invoke(startDate) } returns flowOf(listOf(testTrip()))
        // when / then
        useCase(startDate).test {
            val homeData = awaitItem()
            assertEquals(listOf(testVisa()), homeData.allVisas)
            assertEquals(listOf(testTrip()), homeData.allTrips)
            awaitComplete()
        }
    }

    @Test
    fun `trips are sorted by start date then end date`() = runTest {
        val tripMarch1toSep =
            testTrip(startDate = LocalDate.of(2026, 3, 1), endDate = LocalDate.of(2026, 9, 1))
        val tripMarch1toOct =
            testTrip(startDate = LocalDate.of(2026, 3, 1), endDate = LocalDate.of(2026, 10, 1))
        val tripApril1 =
            testTrip(startDate = LocalDate.of(2026, 4, 1), endDate = LocalDate.of(2026, 10, 1))

        // given
        every { getVisaFlow.invoke(startDate) } returns flowOf(listOf(testVisa()))
        every { getTripsFlow.invoke(startDate) } returns flowOf(
            listOf(
                tripApril1,
                tripMarch1toOct,
                tripMarch1toSep
            )
        )
        // when / then
        useCase(startDate).test {
            val homeData = awaitItem()
            assertEquals(listOf(testVisa()), homeData.allVisas)
            assertEquals(listOf(tripMarch1toSep, tripMarch1toOct, tripApril1), homeData.allTrips)
            awaitComplete()
        }
    }
}