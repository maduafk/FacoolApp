package com.facool.app.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "absences")
data class AbsenceEntity(
    @PrimaryKey
    val courseId: String,
    val count: Int
)

