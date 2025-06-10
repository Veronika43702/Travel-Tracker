package ru.nikfirs.android.traveltracker.feature.home.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.nikfirs.android.traveltracker.core.data.database.dao.VisaDao
import ru.nikfirs.android.traveltracker.core.data.mapper.toEntity
import ru.nikfirs.android.traveltracker.core.data.mapper.toModel
import ru.nikfirs.android.traveltracker.core.domain.model.Visa
import ru.nikfirs.android.traveltracker.core.domain.model.VisaCategory
import ru.nikfirs.android.traveltracker.core.domain.repository.VisaRepository
import java.time.LocalDate
import javax.inject.Inject

class VisaRepositoryImpl @Inject constructor(
    private val visaDao: VisaDao
) : VisaRepository {

    override fun getAllVisas(): Flow<List<Visa>> {
        return visaDao.getAllVisas().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getActiveVisas(): Flow<List<Visa>> {
        return visaDao.getActiveVisas().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getVisasByType(type: VisaCategory): Flow<List<Visa>> {
        val entityCategory = when (type) {
            VisaCategory.TYPE_C -> ru.nikfirs.android.traveltracker.core.data.database.entity.VisaCategory.TYPE_C
            VisaCategory.TYPE_D -> ru.nikfirs.android.traveltracker.core.data.database.entity.VisaCategory.TYPE_D
            VisaCategory.RESIDENCE_PERMIT -> ru.nikfirs.android.traveltracker.core.data.database.entity.VisaCategory.RESIDENCE_PERMIT
        }

        return visaDao.getVisasByCategory(entityCategory).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getExemptCountries(): Flow<Set<String>> {
        return visaDao.getExemptCountries(LocalDate.now()).map { it.toSet() }
    }

    override suspend fun getVisaById(visaId: Long): Visa? {
        return visaDao.getVisaById(visaId)?.toModel()
    }

    override suspend fun insertVisa(visa: Visa): Long {
        return visaDao.insertVisa(visa.toEntity())
    }

    override suspend fun updateVisa(visa: Visa) {
        visaDao.updateVisa(visa.toEntity())
    }

    override suspend fun deleteVisa(visa: Visa) {
        visaDao.deleteVisa(visa.toEntity())
    }

    override suspend fun deactivateExpiredVisas() {
        visaDao.deactivateExpiredVisas(LocalDate.now())
    }

    override suspend fun hasExemptionForCountry(country: String): Boolean {
        return visaDao.hasExemptionForCountry(country, LocalDate.now())
    }
}