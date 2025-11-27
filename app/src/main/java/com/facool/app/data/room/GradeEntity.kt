package com.facool.app.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grades")
data class GradeEntity(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val name: String,
    val score: Double,
    val maxScore: Double = 10.0,
    val weight: Double = 1.0,
    val date: Long = System.currentTimeMillis()
)

