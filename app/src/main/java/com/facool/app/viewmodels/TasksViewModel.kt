package com.facool.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.facool.app.data.FacoolRepository
import com.facool.app.data.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class TasksViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FacoolRepository(app)

    val tasks = repo.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addTask(courseId: String, title: String, dueDay: Int = 0) = viewModelScope.launch {
        if (title.isBlank()) return@launch
        val list = repo.tasks.first().toMutableList()
        list.add(Task(UUID.randomUUID().toString(), courseId, title.trim(), false, dueDay.coerceIn(0,5)))
        repo.saveTasks(list)
    }

    fun toggleDone(id: String) = viewModelScope.launch {
        val list = repo.tasks.first().toMutableList()
        val i = list.indexOfFirst { it.id == id }
        if (i >= 0) {
            val t = list[i]
            list[i] = t.copy(done = !t.done)
            repo.saveTasks(list)
        }
    }

    fun removeTask(id: String) = viewModelScope.launch {
        val list = repo.tasks.first().filterNot { it.id == id }
        repo.saveTasks(list)
    }
}