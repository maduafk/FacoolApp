package com.facool.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.facool.app.data.FacoolRepository
import com.facool.app.data.Grade
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class GradesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FacoolRepository(app)

    val grades = repo.grades.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addGrade(courseId: String, name: String, score: Double, maxScore: Double = 10.0, weight: Double = 1.0) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        val grade = Grade(
            id = UUID.randomUUID().toString(),
            courseId = courseId,
            name = name.trim(),
            score = score.coerceIn(0.0, maxScore),
            maxScore = maxScore,
            weight = weight.coerceIn(0.1, 10.0),
            date = System.currentTimeMillis()
        )
        repo.addGrade(grade)
    }

    fun updateGrade(grade: Grade) = viewModelScope.launch {
        repo.updateGrade(grade)
    }

    fun deleteGrade(gradeId: String) = viewModelScope.launch {
        repo.deleteGrade(gradeId)
    }

    fun calculateAverage(courseId: String): Double {
        val courseGrades = grades.value.filter { it.courseId == courseId }
        if (courseGrades.isEmpty()) return 0.0
        
        val totalWeighted = courseGrades.sumOf { (it.score / it.maxScore) * 10.0 * it.weight }
        val totalWeight = courseGrades.sumOf { it.weight }
        
        return if (totalWeight > 0) (totalWeighted / totalWeight) else 0.0
    }

    fun calculateOverallAverage(): Double {
        val allCourses = grades.value.map { it.courseId }.distinct()
        if (allCourses.isEmpty()) return 0.0
        
        val averages = allCourses.map { calculateAverage(it) }
        return averages.average()
    }
}

