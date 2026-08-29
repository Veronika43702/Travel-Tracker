package ru.nikfirs.android.traveltracker.core.data.mapper

import ru.nikfirs.android.traveltracker.core.data.database.entity.TripEntity
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripSegmentEntity
import ru.nikfirs.android.traveltracker.core.domain.model.testTrip
import ru.nikfirs.android.traveltracker.core.domain.model.testTripSegment
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripPurpose as EntityPurpose

class TripMapperTest {

    @Test
    fun `toEntity maps all trip fields`() {
        // given
        val expected = TripEntity(
            id = 1,
            visaId = 2,
            startDate = LocalDate.of(2026, 8, 29),
            endDate = LocalDate.of(2026, 9, 10),
            purpose = EntityPurpose.EDUCATION,
            notes = "some notes for tests",
            createdAt = LocalDate.of(2026, 8, 1),
        )
        // when
        val result = testTrip(id = 1).toEntity()
        // then
        assertEquals(expected, result)
    }

    @Test
    fun `toEntity joins segment cities with comma`() {
        // given
        val expected = TripSegmentEntity(
            tripId = 3,
            country = "SLO",
            isExempt = false,
            startDate = LocalDate.of(2026, 8, 30),
            endDate = LocalDate.of(2026, 9, 2),
            cities = "Ljubljana, Koper",
        )
        // when
        val result = testTripSegment().toEntity(3)
        // then
        assertEquals(expected, result)
    }

    @Test
    fun `toEntity maps empty segment cities to null`() {
        // given
        val expected = TripSegmentEntity(
            tripId = 3,
            country = "SLO",
            isExempt = false,
            startDate = LocalDate.of(2026, 8, 30),
            endDate = LocalDate.of(2026, 9, 2),
            cities = null,
        )
        // when
        val result = testTripSegment(cities = emptyList()).toEntity(3)
        // then
        assertEquals(expected, result)
    }
}