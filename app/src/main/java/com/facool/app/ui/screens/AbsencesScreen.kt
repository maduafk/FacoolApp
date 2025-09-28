package com.facool.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facool.app.viewmodels.AbsencesViewModel

@Composable
fun AbsencesScreen(vm: AbsencesViewModel = viewModel()) {
    val courses by vm.courses.collectAsState(emptyList())
    val absences by vm.absences.collectAsState(emptyList())

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Controle de Faltas", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(courses, key = { it.id }) { c ->
                val count = absences.firstOrNull { it.courseId == c.id }?.count ?: 0
                ElevatedCard {
                    Column(Modifier.padding(12.dp)) {
                        Text(c.name, style = MaterialTheme.typography.titleMedium)
                        Text("Faltas: ${'$'}count")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { vm.updateAbsences(c.id, count + 1) }) { Text("+1 falta") }
                            OutlinedButton(onClick = { vm.updateAbsences(c.id, (count - 1).coerceAtLeast(0)) }) { Text("-1") }
                        }
                    }
                }
            }
        }
    }
}
