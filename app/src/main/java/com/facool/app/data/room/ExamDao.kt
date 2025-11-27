package com.facool.app.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams WHERE courseId = :courseId ORDER BY date ASC")
    fun getExamsByCourse(courseId: String): Flow<List<ExamEntity>>
    
    @Query("SELECT * FROM exams ORDER BY date ASC")
    fun getAllExams(): Flow<List<ExamEntity>>
    
    @Query("SELECT * FROM exams WHERE date >= :startDate ORDER BY date ASC")
    fun getUpcomingExams(startDate: Long): Flow<List<ExamEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity)
    
    @Update
    suspend fun updateExam(exam: ExamEntity)
    
    @Delete
    suspend fun deleteExam(exam: ExamEntity)
    
    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: String)
    
    @Query("DELETE FROM exams WHERE courseId = :courseId")
    suspend fun deleteExamsByCourse(courseId: String)
    
    @Query("DELETE FROM exams")
    suspend fun deleteAllExams()
}

