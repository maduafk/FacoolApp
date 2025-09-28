package com.facool.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: String,
    val name: String,
    val location: String,
    val dayOfWeek: Int, // 1..5 (Mon..Fri)
    val startTimeMinutes: Int
)

@Serializable
data class Absence(
    val courseId: String,
    val count: Int
)

@Serializable
data class Task(
    val id: String,
    val courseId: String,
    val title: String,
    val done: Boolean = false,
    val dueDay: Int = 0 // 1..5 or 0 (any)
)