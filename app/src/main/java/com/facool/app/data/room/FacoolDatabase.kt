package com.facool.app.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CourseEntity::class, TaskEntity::class, AbsenceEntity::class, GradeEntity::class, ExamEntity::class],
    version = 2,
    exportSchema = false
)
abstract class FacoolDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun taskDao(): TaskDao
    abstract fun absenceDao(): AbsenceDao
    abstract fun gradeDao(): GradeDao
    abstract fun examDao(): ExamDao
    
    companion object {
        @Volatile
        private var INSTANCE: FacoolDatabase? = null
        
        fun getDatabase(context: Context): FacoolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FacoolDatabase::class.java,
                    "facool_database"
                )
                .fallbackToDestructiveMigration() // Para desenvolvimento - em produção usar Migration
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

