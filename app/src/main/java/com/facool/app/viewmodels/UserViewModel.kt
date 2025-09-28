package com.facool.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.facool.app.data.FacoolRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FacoolRepository(app)

    val userName = repo.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val focus = repo.pomodoroFocusMin.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 25)
    val brk = repo.pomodoroBreakMin.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 5)

    fun setUserName(name: String) = viewModelScope.launch { repo.setUserName(name) }
    fun setPomodoro(focus: Int, brk: Int) = viewModelScope.launch { repo.setPomodoro(focus, brk) }
}
