package ru.nikfirs.android.traveltracker.core.domain.model

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import ru.nikfirs.android.traveltracker.core.domain.MAX_STAY_DAYS
import java.time.LocalDate
import kotlin.test.assertEquals

class DaysCalculationTest {
    private fun calculation(totalDaysUsed: Int) = DaysCalculation(
        totalDaysUsed = totalDaysUsed,
        remainingDays = MAX_STAY_DAYS - totalDaysUsed,
        periodStart = LocalDate.of(2026, 1, 1),
        periodEnd = LocalDate.of(2026, 6, 29),
    )

    @ParameterizedTest(name = "isNearLimit is {1} when days used is {0}")
    @CsvSource(
        "0,false",
        "75, false",
        "76, true",
    )
    fun `isNearLimit reflects warning threshold`(
        daysUsed: Int,
        expected: Boolean,
    ) {
        assertEquals(expected, calculation(daysUsed).isNearLimit)
    }

    @ParameterizedTest(name = "isOverLimit is {1} when days used is {0}")
    @CsvSource(
        "0,false",
        "90, false",
        "91, true",
    )
    fun `isOverLimit reflects max stay day`(
        daysUsed: Int,
        expected: Boolean,
    ) {
        assertEquals(expected, calculation(daysUsed).isOverLimit)
    }
}