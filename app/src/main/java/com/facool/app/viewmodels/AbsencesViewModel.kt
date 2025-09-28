package com.facool.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.facool.app.data.Absence
import com.facool.app.data.FacoolRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AbsencesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FacoolRepository(app)

    val courses = repo.courses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val absences = repo.absences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateAbsences(courseId: String, count: Int) = viewModelScope.launch {
        val list = repo.absences.first().toMutableList()
        val idx = list.indexOfFirst { it.courseId == courseId }
        if (idx >= 0) list[idx] = Absence(courseId, count) else list.add(Absence(courseId, count))
        repo.saveAbsences(list)
    }
}
