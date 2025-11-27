package com.facool.app.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val location: String,
    val dayOfWeek: Int, // 1..5 (Mon..Fri)
    val startTimeMinutes: Int
)

