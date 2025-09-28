package com.facool.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.facool.app.data.Course
import com.facool.app.data.FacoolRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ScheduleViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FacoolRepository(app)

    val courses = repo.courses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addCourse(name: String, location: String, day: Int, startMin: Int) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        val current = repo.courses.first().toMutableList()
        current.add(Course(UUID.randomUUID().toString(), name, location, day.coerceIn(1,7), startMin.coerceIn(0, 24*60-1)))
        repo.saveCourses(current)
    }

    fun removeCourse(id: String) = viewModelScope.launch {
        val current = repo.courses.first().filterNot { it.id == id }
        repo.saveCourses(current)
    }
}
