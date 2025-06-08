package ru.nikfirs.android.traveltracker.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.domain.model.Visa

interface VisaRepository {
    fun getAllVisas(): Flow<List<Visa>>
    fun getActiveVisas(): Flow<List<Visa>>
    suspend fun getVisaById(visaId: Long): Visa?
    suspend fun insertVisa(visa: Visa): Long
    suspend fun updateVisa(visa: Visa)
    suspend fun deleteVisa(visa: Visa)
    suspend fun deactivateExpiredVisas()
}