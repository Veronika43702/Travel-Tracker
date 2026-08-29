package ru.nikfirs.android.traveltracker.feature.settings.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.extension.RegisterExtension
import ru.nikfirs.android.traveltracker.core.domain.coroutines.MainDispatcherExtension
import ru.nikfirs.android.traveltracker.core.domain.coroutines.TestDispatcherProvider
import ru.nikfirs.android.traveltracker.core.domain.model.AppDateFormatModel
import ru.nikfirs.android.traveltracker.core.domain.model.AppThemeModel
import ru.nikfirs.android.traveltracker.core.domain.model.CustomString
import ru.nikfirs.android.traveltracker.core.ui.R as uiR
import ru.nikfirs.android.traveltracker.core.ui.domain.usecase.dataStore.GetDateFormatUseCase
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.GetLanguageUseCase
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.GetThemeUseCase
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.SaveDateFormatUseCase
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.SaveLanguageUseCase
import ru.nikfirs.android.traveltracker.feature.settings.domain.usecase.SaveThemeUseCase
import ru.nikfirs.android.traveltracker.feature.settings.ui.settings.SettingsContract.Action
import ru.nikfirs.android.traveltracker.feature.settings.ui.settings.SettingsViewmodel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsViewmodelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    private val getLanguageUseCase = mockk<GetLanguageUseCase>()
    private val saveLanguageUseCase = mockk<SaveLanguageUseCase>()
    private val getThemeUseCase = mockk<GetThemeUseCase>()
    private val saveThemeUseCase = mockk<SaveThemeUseCase>()
    private val getDateFormatUseCase = mockk<GetDateFormatUseCase>()
    private val saveDateFormatUseCase = mockk<SaveDateFormatUseCase>()


    private fun createViewModel(): SettingsViewmodel = SettingsViewmodel(
        getLanguageUseCase,
        saveLanguageUseCase,
        getThemeUseCase,
        saveThemeUseCase,
        getDateFormatUseCase,
        saveDateFormatUseCase,
        dispatcherProvider = TestDispatcherProvider(testDispatcher),
    )

    @Test
    fun `LoadData action loads language, theme and dateFormat`() = runTest {
        val language = "EN"
        val theme = AppThemeModel.DARK
        val dateFormat = AppDateFormatModel.MM_DD_YYYY

        every { getLanguageUseCase.invoke() } returns flowOf(language)
        every { getThemeUseCase.invoke() } returns flowOf(theme)
        every { getDateFormatUseCase.invoke() } returns flowOf(dateFormat)

        val viewModel = createViewModel()
        // when / then
        viewModel.setAction(Action.LoadData)
        assertEquals(language, viewModel.state.value.selectedLanguageInDialog)
        assertEquals(theme, viewModel.state.value.selectedThemeInDialog)
        assertEquals(dateFormat, viewModel.state.value.selectedDateFormatInDialog)

    }

    @Test
    fun `LoadData sets error when use case fails`() = runTest {
        every { getLanguageUseCase.invoke() } throws RuntimeException("db error")
        every { getThemeUseCase.invoke() } throws RuntimeException("db error")
        every { getDateFormatUseCase.invoke() } throws RuntimeException("db error")

        val viewModel = createViewModel()
        // when / then
        viewModel.setAction(Action.LoadData)
        assertEquals(
            CustomString.Resource(uiR.string.error_loading_data),
            viewModel.state.value.error
        )
    }

    @Test
    fun `ShowLanguageDialog action updates state`() = runTest {
        val viewModel = createViewModel()
        viewModel.setAction(Action.ShowLanguageDialog(true))
        assertTrue(viewModel.state.value.showLanguageDialog)
    }

    @Test
    fun `SelectThemeInDialog action calls saveThemeUseCase`() = runTest {
        val viewModel = createViewModel()
        val newTheme = AppThemeModel.DARK

        coEvery { saveThemeUseCase(newTheme) } returns Unit
        viewModel.setAction(Action.SelectThemeInDialog(newTheme))
        coVerify(exactly = 1) { saveThemeUseCase(newTheme) }
    }


}