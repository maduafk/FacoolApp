package com.facool.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facool.app.R
import com.facool.app.ui.Dest
import com.facool.app.viewmodels.UserViewModel
import com.facool.app.viewmodels.ScheduleViewModel
import com.facool.app.viewmodels.AbsencesViewModel
import com.facool.app.viewmodels.TasksViewModel
import java.time.*

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
    val greeting = if (name.isNotEmpty()) "Olá, $name!" else "Olá, estudante!"

    val courses by scheduleVm.courses.collectAsState(emptyList())
    val absences by absVm.absences.collectAsState(emptyList())
    val tasks by tasksVm.tasks.collectAsState(emptyList())

    val next = remember(courses) { findNextClass(courses) }
    val nextPendingTasks = remember(tasks, next, courses) {
        next?.let { (c, _) -> 
            tasks.count { 
                it.courseId == c.id && !it.done
            }
        } ?: 0
    }

    // cálculo de resumo do dia
    val todayDow = LocalDate.now().dayOfWeek
    val todayIndex = when (todayDow) {
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        else -> 0
    }
    val todayCourseIds = courses.filter { it.dayOfWeek == todayIndex }.map { it.id }.toSet()
    var todayAbsences = absences.filter { it.courseId in todayCourseIds }.sumOf { it.count }
    val todayPendingTasks = tasks.count { (it.dueDay == todayIndex || it.dueDay == 0) && !it.done && (todayCourseIds.isEmpty() || it.courseId in todayCourseIds) }

    if (todayIndex == 0 || todayCourseIds.isEmpty()) {
        next?.let { (c, _) ->
            todayAbsences = absences.firstOrNull { it.courseId == c.id }?.count ?: 0
        }
    }

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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header com logo e saudação
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_facool),
                contentDescription = "Logo Facool",
                modifier = Modifier.size(100.dp)
            )
            Text(
                "Facool",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                greeting,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                "Seu estudo, seu ritmo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                if (next != null) {
                    val (c, time) = next!!
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
                        "Sem aulas cadastradas para os próximos dias.",
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
                    if (next != null) {
                        InfoChip("Faltas: $todayAbsences")
                        InfoChip("Tarefas: $nextPendingTasks")
                    } else {
                        InfoChip("Faltas: $todayAbsences")
                        InfoChip("Tarefas: $todayPendingTasks")
                    }
                }
            }
        }

        // Cards de ação
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionCard(
                "Pomodoro",
                modifier = Modifier.weight(1f)
            ) { onNavigate(com.facool.app.ui.Dest.Pomodoro) }
            ActionCard(
                "Cronograma",
                modifier = Modifier.weight(1f)
            ) { onNavigate(com.facool.app.ui.Dest.Schedule) }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionCard(
                "Faltas",
                modifier = Modifier.weight(1f)
            ) { onNavigate(com.facool.app.ui.Dest.Absences) }
            ActionCard(
                "Perfil",
                modifier = Modifier.weight(1f)
            ) { onNavigate(com.facool.app.ui.Dest.Profile) }
        }
    }
}

private fun findNextClass(courses: List<com.facool.app.data.Course>): Pair<com.facool.app.data.Course, LocalDateTime>? {
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
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
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