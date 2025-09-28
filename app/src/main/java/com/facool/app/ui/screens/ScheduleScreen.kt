package com.facool.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facool.app.viewmodels.ScheduleViewModel

@Composable
fun ScheduleScreen(vm: ScheduleViewModel = viewModel()) {
    val courses by vm.courses.collectAsState(emptyList())
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var day by remember { mutableStateOf(1) }
    var hour by remember { mutableStateOf(8) }
    var minute by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Cronograma", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(name, { name = it }, label = { Text("Matéria") })
        OutlinedTextField(location, { location = it }, label = { Text("Local") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(day.toString(), { day = it.toIntOrNull() ?: 1 }, label = { Text("Dia (1-7)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(hour.toString(), { hour = it.toIntOrNull() ?: 8 }, label = { Text("Hora 0-23") }, modifier = Modifier.weight(1f))
            OutlinedTextField(minute.toString(), { minute = it.toIntOrNull() ?: 0 }, label = { Text("Min") }, modifier = Modifier.weight(1f))
        }
        Button(onClick = { vm.addCourse(name, location, day, hour*60+minute); name=""; location="" }) { Text("Adicionar") }

        Divider()
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(courses, key = { it.id }) { c ->
                ElevatedCard {
                    Column(Modifier.padding(12.dp)) {
                        Text("${c.name} - ${formatTime(c.startTimeMinutes)}")
                        Text(c.location, style = MaterialTheme.typography.bodySmall)
                        Text("Dia da semana: ${c.dayOfWeek}")
                        TextButton(onClick = { vm.removeCourse(c.id) }) { Text("Remover") }
                    }
                }
            }
        }
    }
}

private fun formatTime(mins: Int): String {
    val h = mins / 60
    val m = mins % 60
    return "%02d:%02d".format(h, m)
}
