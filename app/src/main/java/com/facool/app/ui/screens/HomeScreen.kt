package com.facool.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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

    val next by remember(courses) { mutableStateOf(findNextClass(courses)) }

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

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Image(
            painter = painterResource(id = R.drawable.logo_facool),
            contentDescription = "Logo Facool",
            modifier = Modifier.size(80.dp).align(Alignment.CenterHorizontally)
        )

        Text("Facool", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(greeting, style = MaterialTheme.typography.titleMedium)
        Text("Seu estudo, seu ritmo.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Próxima aula", style = MaterialTheme.typography.titleMedium)
                if (next != null) {
                    val (c, time) = next!!
                    val hm = "%02d:%02d".format(time.hour, time.minute)
                    val remaining = Duration.between(LocalDateTime.now(), time).toMinutes().coerceAtLeast(0)
                    val remText = if (remaining >= 60) "em ${remaining/60}h${(remaining%60).toString().padStart(2,'0')}" else "em ${remaining}min"
                    val absCount = absences.firstOrNull { it.courseId == c.id }?.count ?: 0
                    Text("${c.name} - ${hm} - ${c.location}")
                    Text(remText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                } else {
                    Text("Sem aulas cadastradas para os próximos dias.")
                }
                Divider()
                Text("Resumo de hoje: faltas $todayAbsences • tarefas pendentes $todayPendingTasks", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ActionCard("Pomodoro", modifier = Modifier.weight(1f)) { onNavigate(com.facool.app.ui.Dest.Pomodoro) }
            ActionCard("Cronograma", modifier = Modifier.weight(1f)) { onNavigate(com.facool.app.ui.Dest.Schedule) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ActionCard("Faltas", modifier = Modifier.weight(1f)) { onNavigate(com.facool.app.ui.Dest.Absences) }
            ActionCard("Perfil", modifier = Modifier.weight(1f)) { onNavigate(com.facool.app.ui.Dest.Profile) }
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
    Card(onClick = onClick, modifier = modifier) {
        Box(Modifier.height(90.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}