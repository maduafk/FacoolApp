package com.facool.app.viewmodels

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.facool.app.R
import com.facool.app.data.FacoolRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PomodoroUi(
    val isRunning: Boolean = false,
    val remainingSeconds: Int = 25 * 60,
    val focusMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val phaseLabel: String = "Foco"
) {
    val formattedTime: String get() {
        val m = remainingSeconds / 60
        val s = remainingSeconds % 60
        return "%02d:%02d".format(m, s)
    }
}

class PomodoroViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FacoolRepository(app)

    private val _isRunning = MutableStateFlow(false)
    private val _remaining = MutableStateFlow(25 * 60)
    private val _isBreak = MutableStateFlow(false)
    private var ticker: Job? = null

    private val focus = repo.pomodoroFocusMin.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), 25)
    private val brk = repo.pomodoroBreakMin.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), 5)

    val uiState = combine(_isRunning, _remaining, _isBreak, focus, brk) { running, rem, isBreak, f, b ->
        PomodoroUi(running, rem, f, b, if (isBreak) "Pausa" else "Foco")
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, PomodoroUi())

    fun updateFocus(v: Int) = viewModelScope.launch { repo.setPomodoro(v.coerceIn(5, 120), uiState.value.breakMinutes) }
    fun updateBreak(v: Int) = viewModelScope.launch { repo.setPomodoro(uiState.value.focusMinutes, v.coerceIn(1, 60)) }

    fun toggle() { if (_isRunning.value) pause() else start() }

    private fun start() {
        _isRunning.value = true
        if (_remaining.value <= 0) _remaining.value = uiState.value.focusMinutes * 60
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (_isRunning.value) {
                delay(1000)
                _remaining.value = (_remaining.value - 1).coerceAtLeast(0)
                if (_remaining.value == 0) onPhaseEnd()
            }
        }
    }

    private fun pause() { _isRunning.value = false; ticker?.cancel() }

    fun reset() {
        pause()
        _isBreak.value = false
        _remaining.value = uiState.value.focusMinutes * 60
    }

    private fun onPhaseEnd() {
        val context = getApplication<Application>().applicationContext
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "pomodoro_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Facool")
            .setContentText("${'$'}{uiState.value.phaseLabel} finalizado!")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()
        mgr.notify(1001, notification)

        try {
            val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vib.vibrate(android.os.VibrationEffect.createOneShot(300, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {}

        if (_isBreak.value) {
            _isBreak.value = false
            _remaining.value = uiState.value.focusMinutes * 60
        } else {
            _isBreak.value = true
            _remaining.value = uiState.value.breakMinutes * 60
        }
    }
}
