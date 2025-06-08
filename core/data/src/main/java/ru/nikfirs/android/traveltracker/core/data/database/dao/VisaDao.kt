package ru.nikfirs.android.traveltracker.core.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.nikfirs.android.traveltracker.core.data.database.entity.VisaEntity
import java.time.LocalDate

@Dao
interface VisaDao {
    @Query("SELECT * FROM visas ORDER BY expiryDate DESC")
    fun getAllVisas(): Flow<List<VisaEntity>>

    @Query("SELECT * FROM visas WHERE isActive = 1 ORDER BY expiryDate DESC")
    fun getActiveVisas(): Flow<List<VisaEntity>>

    @Query("SELECT * FROM visas WHERE id = :visaId")
    suspend fun getVisaById(visaId: Long): VisaEntity?

    @Insert
    suspend fun insertVisa(visa: VisaEntity): Long

    @Update
    suspend fun updateVisa(visa: VisaEntity)

    @Delete
    suspend fun deleteVisa(visa: VisaEntity)

    @Query("UPDATE visas SET isActive = 0 WHERE expiryDate < :currentDate")
    suspend fun deactivateExpiredVisas(currentDate: LocalDate)
}