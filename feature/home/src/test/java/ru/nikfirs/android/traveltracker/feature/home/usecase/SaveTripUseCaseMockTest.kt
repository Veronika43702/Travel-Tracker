package ru.nikfirs.android.traveltracker.feature.home.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ru.nikfirs.android.traveltracker.core.domain.model.testTrip
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.SaveTripUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveTripUseCaseMockTest {
    private val repository = mockk<TripRepository>()
    private val useCase = SaveTripUseCase(repository)

    @Test
    fun `invoke returns id from repository`() = runTest {
        // given
        coEvery { repository.insertTrip(any()) } returns 42L
        // when
        val result = useCase(testTrip())
        // then
        assertEquals(42L, result)
    }

    @Test
    fun `invoke passes trip to repository`() = runTest {
        // given
        coEvery { repository.insertTrip(any()) } returns 1L
        // when
        useCase(testTrip())
        // then
        coVerify(exactly = 1) { repository.insertTrip(testTrip()) }
    }
}