package com.facool.app.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val title: String,
    val done: Boolean = false,
    val dueDay: Int = 0 // 1..5 or 0 (any)
)

