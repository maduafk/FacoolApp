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

@Serializable
data class Grade(
    val id: String,
    val courseId: String,
    val name: String, // Nome da avaliação (ex: "Prova 1", "Trabalho")
    val score: Double, // Nota (0.0 a 10.0 ou 0.0 a 100.0)
    val maxScore: Double = 10.0, // Nota máxima (padrão 10.0)
    val weight: Double = 1.0, // Peso da avaliação
    val date: Long = System.currentTimeMillis() // Data da avaliação
)

@Serializable
data class Exam(
    val id: String,
    val courseId: String,
    val title: String,
    val date: Long, // Timestamp da data/hora da prova
    val location: String = "",
    val notes: String = "",
    val reminderEnabled: Boolean = true
)