package com.facool.app.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val title: String,
    val date: Long,
    val location: String = "",
    val notes: String = "",
    val reminderEnabled: Boolean = true
)

