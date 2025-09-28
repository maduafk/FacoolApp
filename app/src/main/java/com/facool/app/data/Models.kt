package com.facool.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: String,
    val name: String,
    val location: String,
    val dayOfWeek: Int, // 1-7 (Mon=1)
    val startTimeMinutes: Int, // e.g., 9*60+30
)

@Serializable
data class Absence(
    val courseId: String,
    val count: Int
)
