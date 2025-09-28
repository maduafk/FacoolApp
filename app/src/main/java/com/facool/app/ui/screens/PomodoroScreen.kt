package com.facool.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facool.app.viewmodels.PomodoroViewModel

@Composable
fun PomodoroScreen(vm: PomodoroViewModel = viewModel()) {
    val ui by vm.uiState.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Pomodoro", style = MaterialTheme.typography.headlineSmall)
        Text(ui.phaseLabel)

        Text(text = ui.formattedTime, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { vm.toggle() }) { Text(if (ui.isRunning) "Pausar" else "Iniciar") }
            OutlinedButton(onClick = { vm.reset() }) { Text("Resetar") }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Configurações rápidas")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Foco (min): ")
                    NumberAdjust(ui.focusMinutes) { vm.updateFocus(it) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Pausa (min): ")
                    NumberAdjust(ui.breakMinutes) { vm.updateBreak(it) }
                }
            }
        }
    }
}

@Composable
private fun NumberAdjust(value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { onChange((value - 1).coerceAtLeast(1)) }) { Text("-") }
        Text(value.toString())
        OutlinedButton(onClick = { onChange(value + 1) }) { Text("+") }
    }
}