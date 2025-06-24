package ru.nikfirs.android.traveltracker.core.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.data.database.entity.VisaCategory
import ru.nikfirs.android.traveltracker.core.data.database.entity.VisaEntity
import java.time.LocalDate

@Dao
interface VisaDao {
    @Query("SELECT * FROM visas ORDER BY expiryDate DESC")
    fun getAllVisas(): Flow<List<VisaEntity>>

    @Query("""
        SELECT * FROM visas 
        WHERE (:onlyActive = 0 OR isActive = 1)
        AND expiryDate > :startDate 
        ORDER BY startDate DESC
    """)
    fun getVisaFlowByDate(startDate: LocalDate, onlyActive: Boolean): Flow<List<VisaEntity>>

    @Query("SELECT * FROM visas WHERE visaCategory = :category ORDER BY expiryDate DESC")
    fun getVisasByCategory(category: VisaCategory): Flow<List<VisaEntity>>

    @Query("SELECT * FROM visas WHERE id = :visaId")
    suspend fun getVisaById(visaId: Long): VisaEntity?

    @Insert
    suspend fun insertVisa(visa: VisaEntity): Long

    @Update
    suspend fun updateVisa(visa: VisaEntity)

    @Delete
    suspend fun deleteVisa(visa: VisaEntity)

    @Query("UPDATE visas SET isActive = 0 WHERE id = :visaId")
    suspend fun deactivateVisaById(visaId: Long)

    @Query("""
        SELECT COUNT(*) > 0 FROM visas 
        WHERE country = :country 
        AND (visaCategory = 'TYPE_D' OR visaCategory = 'RESIDENCE_PERMIT')
        AND isActive = 1
        AND expiryDate >= :currentDate
    """)
    suspend fun hasExemptionForCountry(country: String, currentDate: LocalDate): Boolean
}