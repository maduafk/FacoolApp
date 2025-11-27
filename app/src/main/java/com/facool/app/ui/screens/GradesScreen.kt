package com.facool.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facool.app.data.Course
import com.facool.app.data.Grade
import com.facool.app.viewmodels.GradesViewModel
import com.facool.app.viewmodels.ScheduleViewModel
import java.text.DecimalFormat

@Composable
fun GradesScreen(
    gradesVm: GradesViewModel = viewModel(),
    scheduleVm: ScheduleViewModel = viewModel()
) {
    val courses by scheduleVm.courses.collectAsState(emptyList())
    val grades by gradesVm.grades.collectAsState(emptyList())
    var selectedCourseId by remember { mutableStateOf<String?>(null) }

    val course = courses.find { it.id == selectedCourseId }

    if (course != null) {
        CourseGradesDetail(
            course = course,
            grades = grades.filter { it.courseId == course.id },
            gradesVm = gradesVm,
            onBack = { selectedCourseId = null }
        )
    } else {
        CoursesList(
            courses = courses,
            grades = grades,
            gradesVm = gradesVm,
            onCourseClick = { selectedCourseId = it.id }
        )
    }
}

@Composable
fun CoursesList(
    courses: List<Course>,
    grades: List<Grade>,
    gradesVm: GradesViewModel,
    onCourseClick: (Course) -> Unit
) {
    val df = remember { DecimalFormat("#.##") }
    val overallAverage = remember(grades) { gradesVm.calculateOverallAverage() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            "Notas e Médias",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Card de média geral
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Média Geral", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(df.format(overallAverage), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        Text("Médias por Disciplina", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (courses.isEmpty()) {
                item {
                    Text(
                        "Nenhuma disciplina cadastrada. Adicione disciplinas na tela de Cronograma.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(courses, key = { it.id }) { course ->
                    val courseAverage = remember(grades) { gradesVm.calculateAverage(course.id) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCourseClick(course) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(course.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(df.format(courseAverage), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseGradesDetail(
    course: Course,
    grades: List<Grade>,
    gradesVm: GradesViewModel,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val df = remember { DecimalFormat("#.##") }
    val courseAverage = remember(grades) { gradesVm.calculateAverage(course.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Nota")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Média da Disciplina", style = MaterialTheme.typography.titleLarge)
                        Text(df.format(courseAverage), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Avaliações", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            }

            if (grades.isEmpty()) {
                item {
                    Text(
                        "Nenhuma nota adicionada para esta disciplina.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            items(grades, key = { it.id }) { grade ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(grade.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Nota: ${df.format(grade.score)} / ${df.format(grade.maxScore)}", style = MaterialTheme.typography.bodyMedium)
                            Text("Peso: ${df.format(grade.weight)}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { gradesVm.deleteGrade(grade.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGradeDialog(
            courseName = course.name,
            onDismiss = { showAddDialog = false },
            onAdd = { name, score, maxScore, weight ->
                gradesVm.addGrade(course.id, name, score, maxScore, weight)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGradeDialog(
    courseName: String,
    onDismiss: () -> Unit,
    onAdd: (String, Double, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var scoreText by remember { mutableStateOf("") }
    var maxScoreText by remember { mutableStateOf("10.0") }
    var weightText by remember { mutableStateOf("1.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Nota em $courseName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da avaliação (ex: Prova 1)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = scoreText,
                    onValueChange = { newText -> scoreText = newText.filter { it.isDigit() || it == '.' } },
                    label = { Text("Nota obtida") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxScoreText,
                    onValueChange = { newText -> maxScoreText = newText.filter { it.isDigit() || it == '.' } },
                    label = { Text("Nota máxima (padrão 10)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { newText -> weightText = newText.filter { it.isDigit() || it == '.' } },
                    label = { Text("Peso (padrão 1.0)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && scoreText.isNotBlank(),
                onClick = {
                    val score = scoreText.toDoubleOrNull() ?: 0.0
                    val maxScore = maxScoreText.toDoubleOrNull() ?: 10.0
                    val weight = weightText.toDoubleOrNull() ?: 1.0
                    onAdd(name, score, maxScore, weight)
                }
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
