package com.facool.app.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AbsenceDao {
    @Query("SELECT * FROM absences")
    fun getAllAbsences(): Flow<List<AbsenceEntity>>
    
    @Query("SELECT * FROM absences WHERE courseId = :courseId")
    suspend fun getAbsenceByCourseId(courseId: String): AbsenceEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbsence(absence: AbsenceEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAbsences(absences: List<AbsenceEntity>)
    
    @Update
    suspend fun updateAbsence(absence: AbsenceEntity)
    
    @Delete
    suspend fun deleteAbsence(absence: AbsenceEntity)
    
    @Query("DELETE FROM absences WHERE courseId = :courseId")
    suspend fun deleteAbsenceByCourseId(courseId: String)
    
    @Query("DELETE FROM absences")
    suspend fun deleteAllAbsences()
}

