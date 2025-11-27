package com.facool.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facool.app.R
import com.facool.app.data.Course
import com.facool.app.ui.Dest
import com.facool.app.viewmodels.AbsencesViewModel
import com.facool.app.viewmodels.ScheduleViewModel
import com.facool.app.viewmodels.TasksViewModel
import com.facool.app.viewmodels.UserViewModel
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigate: (Dest) -> Unit,
    userVm: UserViewModel = viewModel(),
    scheduleVm: ScheduleViewModel = viewModel(),
    absVm: AbsencesViewModel = viewModel(),
    tasksVm: TasksViewModel = viewModel()
) {
    val nameState by userVm.userName.collectAsState(initial = null)
    val name = nameState?.trim().orEmpty()
    val greeting = if (name.isNotEmpty()) "Olá, $name!" else "Olá!"

    val courses by scheduleVm.courses.collectAsState(emptyList())
    val absences by absVm.absences.collectAsState(emptyList())
    val tasks by tasksVm.tasks.collectAsState(emptyList())

    val nextClass = remember(courses) { findNextClass(courses) }
    
    val today = LocalDate.now()
    val dateText = today.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM, yyyy"))
    val dayOfWeekText = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))

    var tempName by remember { mutableStateOf("") }
    val showWelcome = name.isEmpty()

    if (showWelcome) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Bem-vindo ao Facool") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Para personalizar sua experiência, digite seu nome.")
                    OutlinedTextField(value = tempName, onValueChange = { tempName = it }, label = { Text("Seu nome") })
                }
            },
            confirmButton = {
                Button(enabled = tempName.isNotBlank(), onClick = { userVm.setUserName(tempName.trim()) }) { Text("Começar") }
            },
            dismissButton = { TextButton(onClick = { userVm.setUserName("") }) { Text("Pular") } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header com logo e saudação
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$dayOfWeekText, $dateText",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Image(
                painter = painterResource(id = R.drawable.logo_facool),
                contentDescription = "Logo Facool",
                modifier = Modifier.size(60.dp)
            )
        }

        // Card da próxima aula
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Próxima aula",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (nextClass != null) {
                    val (c, time) = nextClass
                    val hm = "%02d:%02d".format(time.hour, time.minute)
                    val remaining = Duration.between(LocalDateTime.now(), time).toMinutes().coerceAtLeast(0)
                    val remText = if (remaining >= 60) "em ${remaining/60}h${(remaining%60).toString().padStart(2,'0')}" else "em ${remaining}min"
                    Text(
                        c.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "$hm • $remText",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        c.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        "Sem aulas para os próximos dias.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val todayAbsences = absences.filter { it.courseId == nextClass?.first?.id }.sumOf { it.count }
                    val pendingTasks = tasks.count { !it.done && (it.courseId == nextClass?.first?.id || it.dueDay == today.dayOfWeek.value) }
                    InfoChip("Faltas: $todayAbsences")
                    InfoChip("Tarefas: $pendingTasks")
                }
            }
        }

        Text(
            "Seu estudo, seu ritmo.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Grade de atalhos
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            val shortcuts = listOf(
                Dest.Schedule,
                Dest.Grades,
                Dest.Absences,
                Dest.Exams,
                Dest.Pomodoro,
                Dest.Photos
            )
            items(shortcuts) { dest ->
                ActionCard(dest.label, modifier = Modifier.height(120.dp)) { onNavigate(dest) }
            }
        }
    }
}

private fun findNextClass(courses: List<Course>): Pair<Course, LocalDateTime>? {
    if (courses.isEmpty()) return null
    val now = LocalDateTime.now()
    val candidates = courses.mapNotNull { c ->
        val dow = DayOfWeek.MONDAY.plus((c.dayOfWeek - 1).coerceIn(0,4).toLong())
        var date = now.toLocalDate()
        val delta = ((dow.value - date.dayOfWeek.value) + 7) % 7
        date = date.plusDays(delta.toLong())
        val target = LocalDateTime.of(date, LocalTime.of(c.startTimeMinutes/60, c.startTimeMinutes%60))
        val occurrence = if (target.isAfter(now)) target else target.plusWeeks(1)
        c to occurrence
    }
    return candidates.minByOrNull { it.second }
}


@Composable
private fun ActionCard(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.Medium
        )
    }
}