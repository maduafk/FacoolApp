package com.facool.app.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
    @Query("SELECT * FROM grades WHERE courseId = :courseId ORDER BY date DESC")
    fun getGradesByCourse(courseId: String): Flow<List<GradeEntity>>
    
    @Query("SELECT * FROM grades ORDER BY date DESC")
    fun getAllGrades(): Flow<List<GradeEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: GradeEntity)
    
    @Update
    suspend fun updateGrade(grade: GradeEntity)
    
    @Delete
    suspend fun deleteGrade(grade: GradeEntity)
    
    @Query("DELETE FROM grades WHERE id = :id")
    suspend fun deleteGradeById(id: String)
    
    @Query("DELETE FROM grades WHERE courseId = :courseId")
    suspend fun deleteGradesByCourse(courseId: String)
    
    @Query("DELETE FROM grades")
    suspend fun deleteAllGrades()
}

