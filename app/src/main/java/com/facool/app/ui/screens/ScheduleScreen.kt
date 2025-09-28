package com.facool.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facool.app.viewmodels.ScheduleViewModel
import com.facool.app.viewmodels.AbsencesViewModel
import com.facool.app.viewmodels.TasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(vm: ScheduleViewModel = viewModel(), absVm: AbsencesViewModel = viewModel(), tasksVm: TasksViewModel = viewModel()) {
    val courses by vm.courses.collectAsState(emptyList())
    val absences by absVm.absences.collectAsState(emptyList())
    val tasks by tasksVm.tasks.collectAsState(emptyList())

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dayIndex by remember { mutableStateOf(0) } // 0..4 => Mon..Fri
    var hour by remember { mutableStateOf(8) }
    var minute by remember { mutableStateOf(0) }
    val days = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta")

    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Cronograma", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(name, { name = it }, label = { Text("Matéria") })
        OutlinedTextField(location, { location = it }, label = { Text("Local") })

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                readOnly = true,
                value = days[dayIndex],
                onValueChange = {},
                label = { Text("Dia (seg-sex)") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                days.forEachIndexed { index, label ->
                    DropdownMenuItem(text = { Text(label) }, onClick = { dayIndex = index; expanded = false })
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(hour.toString(), { hour = it.toIntOrNull() ?: 8 }, label = { Text("Hora 0-23") }, modifier = Modifier.weight(1f))
            OutlinedTextField(minute.toString(), { minute = it.toIntOrNull() ?: 0 }, label = { Text("Min") }, modifier = Modifier.weight(1f))
        }
        Button(onClick = {
            val day = dayIndex + 1 // 1..5
            vm.addCourse(name, location, day, hour*60+minute)
            name = ""; location = ""
        }) { Text("Adicionar") }

        Divider()
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(courses, key = { it.id }) { c ->
                val daysShort = listOf("Seg", "Ter", "Qua", "Qui", "Sex")
                val abs = absences.firstOrNull { it.courseId == c.id }?.count ?: 0
                val cTasks = tasks.filter { it.courseId == c.id }

                ElevatedCard {
                    Column(Modifier.padding(12.dp)) {
                        Text("${c.name} - ${formatTime(c.startTimeMinutes)}")
                        Text(c.location, style = MaterialTheme.typography.bodySmall)
                        Text("Dia da semana: ${daysShort[(c.dayOfWeek-1).coerceIn(0,4)]}")
                        Text("Faltas: $abs", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Text("Tarefas", style = MaterialTheme.typography.titleSmall)
                        var newTask by remember { mutableStateOf("") }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(newTask, { newTask = it }, label = { Text("Adicionar tarefa") }, modifier = Modifier.weight(1f))
                            Button(onClick = { tasksVm.addTask(c.id, newTask, c.dayOfWeek); newTask = "" }) { Text("Add") }
                        }
                        cTasks.forEach { t ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = t.done, onCheckedChange = { tasksVm.toggleDone(t.id) })
                                Text(t.title, modifier = Modifier.weight(1f))
                                TextButton(onClick = { tasksVm.removeTask(t.id) }) { Text("Remover") }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        TextButton(onClick = { vm.removeCourse(c.id) }) { Text("Remover matéria") }
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