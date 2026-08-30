package ru.nikfirs.android.traveltracker.feature.home.viewmodel

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import ru.nikfirs.android.traveltracker.core.domain.coroutines.MainDispatcherExtension
import ru.nikfirs.android.traveltracker.core.domain.coroutines.TestDispatcherProvider
import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.domain.model.testDaysCalculation
import ru.nikfirs.android.traveltracker.core.domain.model.testTrip
import ru.nikfirs.android.traveltracker.core.domain.model.testVisa
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.CalculateDaysInPeriodUseCase
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.dataStore.GetDateFormatUseCase
import ru.nikfirs.android.traveltracker.feature.home.R
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.GetHomeDataUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.trip.DeleteTripUseCase
import ru.nikfirs.android.traveltracker.feature.home.domain.usecase.visa.DeleteVisaUseCase
import ru.nikfirs.android.traveltracker.feature.home.ui.model.HomeData
import ru.nikfirs.android.traveltracker.feature.home.ui.model.HomeTab
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.HomeContract.Action
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.HomeContract.Effect
import ru.nikfirs.android.traveltracker.feature.home.ui.screens.main.HomeViewModel
import ru.nikfirs.android.traveltracker.feature.home.ui.utils.HomeAction
import ru.nikfirs.android.traveltracker.feature.home.ui.utils.HomeActionModel
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import ru.nikfirs.android.traveltracker.core.ui.R as uiR

class HomeViewModelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    private val getHomeDataUseCase = mockk<GetHomeDataUseCase>()
    private val calculateDaysInPeriodUseCase = mockk<CalculateDaysInPeriodUseCase>()
    private val deleteTripUseCase = mockk<DeleteTripUseCase>()
    private val deleteVisaUseCase = mockk<DeleteVisaUseCase>()
    private val getDateFormatUseCase = mockk<GetDateFormatUseCase>()

    private fun createViewModel(): HomeViewModel = HomeViewModel(
        getHomeDataUseCase,
        calculateDaysInPeriodUseCase,
        deleteTripUseCase,
        deleteVisaUseCase,
        getDateFormatUseCase,
        dispatcherProvider = TestDispatcherProvider(testDispatcher),
    )

    private val homeData = HomeData(
        allVisas = listOf(testVisa()),
        allTrips = listOf(testTrip())
    )

    @BeforeEach
    fun init() {
        val dateFormat = AppDateFormatModel.MM_DD_YYYY
        val homeData = homeData
            .copy(allVisas = listOf(testVisa(), testVisa(id = 1, isActive = false)))

        every { getDateFormatUseCase.invoke() } returns flowOf(dateFormat)
        every { getHomeDataUseCase.invoke(any()) } returns flowOf(homeData)
        coEvery { calculateDaysInPeriodUseCase(any(), any()) } returns testDaysCalculation()
    }

    @Test
    fun `State activeVisas have only active visas after init`() {
        val viewModel = createViewModel()
        assertEquals(listOf(testVisa()), viewModel.state.value.activeVisas)
    }

    @Test
    fun `LoadData action loads dateFormat and homeData`() {
        every { getHomeDataUseCase.invoke(any()) } returns flowOf(homeData)

        val viewModel = createViewModel()
        //  then
        assertEquals(
            "09/01/2026",
            viewModel.state.value.dateFormatter.format(LocalDate.of(2026, 9, 1)),
        )
        assertEquals(homeData.allVisas, viewModel.state.value.visas)
        assertEquals(homeData.allTrips, viewModel.state.value.trips)
        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals(null, viewModel.state.value.error)
    }

    @Test
    fun `LoadData keeps loading until home data arrives`() = runTest {
        val homeDataFlow = MutableSharedFlow<HomeData>()
        every { getHomeDataUseCase.invoke(any()) } returns homeDataFlow

        val viewModel = createViewModel()
        // no data yet
        assertEquals(true, viewModel.state.value.isLoading)

        homeDataFlow.emit(homeData)
        // data are received
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `LoadData action loads homeData with hasOverLimitDay = true`() {
        coEvery {
            calculateDaysInPeriodUseCase.invoke(any(), null)
        } returns testDaysCalculation(totalDaysUsed = 95)
        every { getHomeDataUseCase.invoke(any()) } returns flowOf(homeData)

        val viewModel = createViewModel()
        // then
        coVerify(atLeast = 1) {
            calculateDaysInPeriodUseCase.invoke(any(), null)
        }
        assertEquals(listOf(testTrip(hasOverLimitDay = true)), viewModel.state.value.trips)
    }

    @Test
    fun `LoadData action throws Exception in getHomeUseCase`() {
        every { getHomeDataUseCase.invoke(any()) } throws Exception()

        val viewModel = createViewModel()
        // then
        assertEquals(
            CustomString.Resource(uiR.string.error_loading_data),
            viewModel.state.value.error
        )
    }

    @Test
    fun `LoadData action throws Exception in flow`() {
        every { getHomeDataUseCase.invoke(any()) } returns flow { throw RuntimeException("collect error") }

        val viewModel = createViewModel()
        // then
        assertEquals(
            CustomString.Resource(uiR.string.error_loading_data),
            viewModel.state.value.error
        )
    }

    @Test
    fun `UpdateDaysCalculation action sets daysCalculation`() {
        // when
        val viewModel = createViewModel()
        coEvery { calculateDaysInPeriodUseCase(any(), any()) } returns testDaysCalculation(
            totalDaysUsed = 42
        )
        viewModel.setAction(Action.UpdateDaysCalculation)
        assertEquals(42, viewModel.state.value.daysCalculation?.totalDaysUsed)
    }

    @Test
    fun `UpdateDaysCalculation action throws exception`() {
        // when
        val viewModel = createViewModel()
        coEvery { calculateDaysInPeriodUseCase.invoke(any()) } throws RuntimeException()
        viewModel.setAction(Action.UpdateDaysCalculation)
        //  then
        assertEquals(CustomString.internal(), viewModel.state.value.error)
    }

    @Test
    fun `SelectTab action selects tab`() {
        val tab = HomeTab.VISAS
        val viewModel = createViewModel()
        // when / then
        viewModel.setAction(Action.SelectTab(tab))
        assertEquals(tab, viewModel.state.value.selectedTab)
    }

    @Test
    fun `NavigateToAddVisa action sets effect NavigateToAddVisa`() = runTest {
        val viewModel = createViewModel()
        // when / then
        viewModel.effect.test {
            viewModel.setAction(Action.NavigateToAddVisa)
            assertEquals(Effect.NavigateToAddVisa, awaitItem())
        }
    }

    @Test
    fun `NavigateToAddTrip action sets effect NavigateToAddTrip`() = runTest {
        val viewModel = createViewModel()
        // when / then
        viewModel.effect.test {
            viewModel.setAction(Action.NavigateToAddTrip)
            assertEquals(Effect.NavigateToAddTrip, awaitItem())
        }
    }

    @Test
    fun `NavigateToVisaDetails action sets effect NavigateToVisaDetails`() = runTest {
        val visaId = testVisa().id
        val viewModel = createViewModel()
        // when / then
        viewModel.effect.test {
            viewModel.setAction(Action.NavigateToVisaDetails(visaId))
            assertEquals(Effect.NavigateToVisaDetails(visaId), awaitItem())
        }
    }

    @Test
    fun `NavigateToTripDetails action sets effect NavigateToTripDetails`() = runTest {
        val tripId = testTrip().id
        val viewModel = createViewModel()
        // when / then
        viewModel.effect.test {
            viewModel.setAction(Action.NavigateToTripDetails(tripId))
            assertEquals(Effect.NavigateToTripDetails(tripId), awaitItem())
        }
    }

    @Test
    fun `NavigateToEditVisa action sets effect NavigateToEditVisa`() = runTest {
        val visaId = testVisa().id
        val viewModel = createViewModel()
        // when / then
        viewModel.effect.test {
            viewModel.setAction(Action.NavigateToEditVisa(visaId))
            assertEquals(Effect.NavigateToEditVisa(visaId), awaitItem())
        }
    }

    @Test
    fun `NavigateToEditTrip action sets effect NavigateToEditTrip`() = runTest {
        val tripId = testTrip().id
        val viewModel = createViewModel()
        // when / then
        viewModel.effect.test {
            viewModel.setAction(Action.NavigateToEditTrip(tripId))
            assertEquals(Effect.NavigateToEditTrip(tripId), awaitItem())
        }
    }

    @Test
    fun `DeleteTrip action deletes trip and set effect ShowMessage`() = runTest {
        val trip = testTrip()
        val viewModel = createViewModel()
        // given
        coEvery { deleteTripUseCase.invoke(trip) } returns Unit

        // when / then
        viewModel.effect.test {
            viewModel.setAction(Action.DeleteTrip(trip))
            coVerify(exactly = 1) { deleteTripUseCase(trip) }
            assertEquals(
                expected = Effect.ShowMessage(
                    CustomString.resource(R.string.home_trip_deleted_successfully)
                ),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `DeleteTrip action gets an Exception and sets Error`() {
        val trip = testTrip()
        val viewModel = createViewModel()
        // given
        coEvery { deleteTripUseCase.invoke(trip) } throws RuntimeException("delete error")
        // when
        viewModel.setAction(Action.DeleteTrip(trip))
        // then
        assertEquals(
            expected = CustomString.resource(R.string.home_error_trip_deleting),
            actual = viewModel.state.value.error,
        )
    }

    @Test
    fun `DeleteVisa action deletes visa and set effect ShowMessage`() = runTest {
        val visa = testVisa()
        val viewModel = createViewModel()
        // given
        coEvery { deleteVisaUseCase.invoke(visa) } returns Unit

        // when / then
        viewModel.effect.test {
            viewModel.setAction(Action.DeleteVisa(visa))
            coVerify(exactly = 1) { deleteVisaUseCase(visa) }
            assertEquals(
                expected = Effect.ShowMessage(
                    CustomString.resource(R.string.home_visa_deleted_successfully)
                ),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `DeleteVisa action gets an Exception and sets Error`() {
        val visa = testVisa()
        val viewModel = createViewModel()
        // given
        coEvery { deleteVisaUseCase.invoke(visa) } throws RuntimeException("delete error")
        // when
        viewModel.setAction(Action.DeleteVisa(visa))
        // then
        assertEquals(
            expected = CustomString.resource(R.string.home_error_visa_deleting),
            actual = viewModel.state.value.error,
        )
    }

    @Test
    fun `SetError action sets state error and loading false`() {
        val viewModel = createViewModel()
        // when
        viewModel.setAction(Action.SetError(CustomString.internal()))
        // then
        assertEquals(
            expected = CustomString.internal(),
            actual = viewModel.state.value.error,
        )
        assertEquals(
            expected = false,
            actual = viewModel.state.value.isLoading,
        )
    }

    @Test
    fun `RetryLoadData action loads dateFormat and homeData`() {
        every { getHomeDataUseCase.invoke(any()) } returns flowOf(homeData)

        val viewModel = createViewModel()
        //  then
        assertEquals(
            "09/01/2026",
            viewModel.state.value.dateFormatter.format(LocalDate.of(2026, 9, 1)),
        )
        assertEquals(homeData.allVisas, viewModel.state.value.visas)
        assertEquals(homeData.allTrips, viewModel.state.value.trips)
    }

    @Test
    fun `ShowDeleteVisaDialog action sets dialog and action`() {
        val visa = testVisa()
        val viewModel = createViewModel()
        // when
        viewModel.setAction(Action.ShowDeleteVisaDialog(visa))
        // then
        assertEquals(
            expected = CustomString.resource(R.string.home_visa_dialog_delete),
            actual = viewModel.state.value.dialogText,
        )
        assertEquals(
            expected = HomeActionModel(HomeAction.DELETE_VISA, visa),
            actual = viewModel.state.value.action,
        )
    }

    @Test
    fun `ShowDeleteTripDialog action sets dialog and action`() {
        val trip = testTrip()
        val viewModel = createViewModel()
        // when
        viewModel.setAction(Action.ShowDeleteTripDialog(trip))
        // then
        assertEquals(
            expected = CustomString.resource(R.string.home_trip_dialog_delete),
            actual = viewModel.state.value.dialogText,
        )
        assertEquals(
            expected = HomeActionModel(HomeAction.DELETE_TRIP, trip = trip),
            actual = viewModel.state.value.action,
        )
    }

    @Test
    fun `HideDialog action sets dialog and action to null`() {
        val viewModel = createViewModel()
        // when
        viewModel.setAction(Action.ShowDeleteTripDialog(testTrip()))
        viewModel.setAction(Action.HideDialog)
        // then
        assertEquals(
            expected = null,
            actual = viewModel.state.value.dialogText,
        )
        assertEquals(
            expected = null,
            actual = viewModel.state.value.action,
        )
    }
}