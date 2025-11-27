package com.facool.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.facool.app.data.FacoolRepository
import com.facool.app.data.Exam
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ExamsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FacoolRepository(app)

    val exams = repo.exams.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addExam(courseId: String, title: String, date: Long, location: String = "", notes: String = "") = viewModelScope.launch {
        if (title.isBlank()) return@launch
        val exam = Exam(
            id = UUID.randomUUID().toString(),
            courseId = courseId,
            title = title.trim(),
            date = date,
            location = location.trim(),
            notes = notes.trim(),
            reminderEnabled = true
        )
        repo.addExam(exam)
    }

    fun updateExam(exam: Exam) = viewModelScope.launch {
        repo.updateExam(exam)
    }

    fun deleteExam(examId: String) = viewModelScope.launch {
        repo.deleteExam(examId)
    }

    fun getUpcomingExams(): List<Exam> {
        val now = System.currentTimeMillis()
        return exams.value.filter { it.date >= now }.sortedBy { it.date }
    }
}

