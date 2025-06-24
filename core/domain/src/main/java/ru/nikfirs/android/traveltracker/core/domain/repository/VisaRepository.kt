package ru.nikfirs.android.traveltracker.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import java.time.LocalDate

interface VisaRepository {
    fun getAllVisas(): Flow<List<Visa>>
    fun getVisaFlowByDate(startDate: LocalDate, onlyActive: Boolean): Flow<List<Visa>>
    fun getAvailableVisasByDate(startDate: LocalDate): List<Visa>
    fun getVisasByType(type: VisaCategory): Flow<List<Visa>>
    suspend fun getVisaById(visaId: Long): Visa?
    suspend fun insertVisa(visa: Visa): Long
    suspend fun updateVisa(visa: Visa)
    suspend fun deleteVisa(visa: Visa)
    suspend fun deactivateVisaById(visaId: Long)
    suspend fun hasExemptionForCountry(country: String): Boolean
}