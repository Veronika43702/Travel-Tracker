package ru.nikfirs.android.traveltracker.core.ui.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import ru.nikfirs.android.traveltracker.core.data.database.dao.TripDao
import ru.nikfirs.android.traveltracker.core.data.database.dao.TripSegmentDao
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripSegmentEntity
import ru.nikfirs.android.traveltracker.core.data.entity.testTripEntity
import ru.nikfirs.android.traveltracker.core.data.entity.testTripWithSegmentsEntity
import ru.nikfirs.android.traveltracker.core.data.model.CountryStatistics
import ru.nikfirs.android.traveltracker.core.domain.model.DaysCalculation
import ru.nikfirs.android.traveltracker.core.domain.model.testTrip
import ru.nikfirs.android.traveltracker.core.domain.model.testTripSegment
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import ru.nikfirs.android.traveltracker.core.ui.data.TripRepositoryImpl
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class TripRepositoryImplTest {

    private val tripDao = mockk<TripDao>()
    private val tripSegmentDao = mockk<TripSegmentDao>()

    private val repository: TripRepository = TripRepositoryImpl(
        tripDao, tripSegmentDao
    )

    private val periodStart = LocalDate.of(2026, 1, 1)
    private val periodEnd = LocalDate.of(2026, 6, 29)

    private val trips = listOf(testTrip(id = 0), testTrip(id = 1))
    private val tripsWithSegments = listOf(
        testTripWithSegmentsEntity(id = 0),
        testTripWithSegmentsEntity(id = 1)
    )

    private val startDate = LocalDate.of(2026, 8, 30)
    private val endDate = LocalDate.of(2026, 9, 30)

    @Test
    fun `getAllTrips returns flow of list of Trip`() = runTest {
        every { tripDao.getAllTripsWithSegments() } returns flowOf(tripsWithSegments)

        repository.getAllTrips().test {
            assertEquals(trips, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getTripById return Trip model`() = runTest {
        coEvery { tripDao.getTripByIdWithSegments(0) } returns testTripWithSegmentsEntity(0)

        val result = repository.getTripById(0)
        assertEquals(testTrip(id = 0), result)
    }

    @Test
    fun `getTripsFlowByDates returns flow of list of Trip`() = runTest {
        every { tripDao.getTripsByDatesFlow(startDate, null) } returns flowOf(tripsWithSegments)

        repository.getTripsFlowByDates(startDate, null).test {
            assertEquals(trips, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getTripsByDates returns list of Trip`() = runTest {
        every { tripDao.getTripsByDates(startDate, endDate) } returns tripsWithSegments

        val result = repository.getTripsByDates(startDate, endDate)
        assertEquals(trips, result)
    }

    @Test
    fun `insertTrip inserts segments to trip and return tripId`() = runTest {
        // given
        val newTrip = testTrip(id = 0, segments = listOf(testTripSegment()))
        val savedSegments = slot<List<TripSegmentEntity>>()

        coEvery { tripDao.insertTrip(any()) } returns 42L
        coEvery { tripSegmentDao.insertSegments(capture(savedSegments)) } returns Unit

        // when
        val result = repository.insertTrip(newTrip)

        // then
        assertEquals(42L, result)
        assertEquals(42L, savedSegments.captured.single().tripId)
    }

    @Test
    fun `updateTrip deletes old segments and inserts new segments to trip`() = runTest {
        // given
        val trip = testTrip(id = 7, segments = listOf(testTripSegment()))
        val savedSegments = slot<List<TripSegmentEntity>>()

        coEvery { tripDao.updateTrip(any()) } returns Unit
        coEvery { tripSegmentDao.deleteSegmentsByTripId(7) } returns Unit
        coEvery { tripSegmentDao.insertSegments(capture(savedSegments)) } returns Unit

        // when
        repository.updateTrip(trip)

        // then
        coVerifyOrder {
            tripSegmentDao.deleteSegmentsByTripId(7)
            tripSegmentDao.insertSegments(any())
        }
        assertEquals(7L, savedSegments.captured.single().tripId)
    }

    @Test
    fun `calculateDaysInPeriod builds 180 day window ending at period end`() = runTest {
        coEvery {
            tripDao.getDaysCountInPeriodWithExemptions(
                periodStart = periodStart,
                periodEnd = periodEnd,
                tripId = null,
            )
        } returns 30

        val result = repository.calculateDaysInPeriod(periodEnd, null)
        val expected = DaysCalculation(
            totalDaysUsed = 30,
            remainingDays = 60,
            periodStart = periodStart,
            periodEnd = periodEnd,
        )

        coVerify(exactly = 1) {
            tripDao.getDaysCountInPeriodWithExemptions(
                periodStart = periodStart,
                periodEnd = periodEnd,
                tripId = null,
            )
        }
        assertEquals(expected, result)
    }

    @Test
    fun `deleteTrip calls tripDao deleteTrip`() = runTest {
        coEvery { tripDao.deleteTrip(testTripEntity()) } returns Unit

        repository.deleteTrip(testTrip())
        coVerify(exactly = 1) { tripDao.deleteTrip(testTripEntity()) }
    }

    @Test
    fun `calculateDaysInPeriod leaves zero remaining days when 90 days used`() = runTest {
        coEvery {
            tripDao.getDaysCountInPeriodWithExemptions(
                periodStart = periodStart,
                periodEnd = periodEnd,
                tripId = null,
            )
        } returns 90

        val result = repository.calculateDaysInPeriod(periodEnd, null)
        val expected = DaysCalculation(
            totalDaysUsed = 90,
            remainingDays = 0,
            periodStart = periodStart,
            periodEnd = periodEnd,
        )
        assertEquals(expected, result)
    }

    @Test
    fun `calculateDaysInPeriod passes excluded trip id to dao`() = runTest {
        coEvery {
            tripDao.getDaysCountInPeriodWithExemptions(
                periodStart = periodStart,
                periodEnd = periodEnd,
                tripId = 1,
            )
        } returns 90

        repository.calculateDaysInPeriod(periodEnd, 1)
        coVerify(exactly = 1) {
            tripDao.getDaysCountInPeriodWithExemptions(
                periodStart = periodStart,
                periodEnd = periodEnd,
                tripId = 1,
            )
        }
    }

    @ParameterizedTest(name = "available = {1} when dao returns {0} conflicts")
    @CsvSource("0, true", "1, false", "5, false")
    fun `checkIfDatesAvailable reflects conflict count`(conflicts: Int, expected: Boolean) =
        runTest {
            val startDate = LocalDate.of(2026, 4, 1)
            val endDate = LocalDate.of(2026, 9, 1)
            val segments = listOf(
                testTripSegment(
                    startDate = LocalDate.of(2026, 6, 1),
                    endDate = endDate,
                ),
                testTripSegment(
                    startDate = LocalDate.of(2026, 6, 1),
                    endDate = LocalDate.of(2026, 7, 1),
                ),
                testTripSegment(
                    startDate = startDate,
                    endDate = LocalDate.of(2026, 7, 1),
                ),
            )

            coEvery {
                tripDao.checkDatesAvailability(
                    startDate = startDate,
                    endDate = endDate,
                    exemptCountries = listOf("FRA"),
                    excludeTripId = 1,
                )
            } returns conflicts

            val result = repository.checkIfDatesAvailable(segments, setOf("FRA"), 1)

            coVerify(exactly = 1) {
                tripDao.checkDatesAvailability(
                    startDate = startDate,
                    endDate = endDate,
                    exemptCountries = listOf("FRA"),
                    excludeTripId = 1,
                )
            }
            assertEquals(expected, result)
        }

    @Test
    fun `getCountryStatistics maps dao rows to country days map`() = runTest {
        coEvery {
            tripDao.getCountryStatistics(
                periodStart = periodStart,
                periodEnd = periodEnd,
            )
        } returns listOf(CountryStatistics("SLO", 3))

        val result = repository.getCountryStatistics(periodStart, periodEnd)
        assertEquals(
            mapOf("SLO" to 3),
            result,
        )
    }
}