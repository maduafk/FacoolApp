package com.facool.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facool.app.ui.Dest
import com.facool.app.viewmodels.UserViewModel
import com.facool.app.viewmodels.ScheduleViewModel
import java.time.*

@Composable
fun HomeScreen(onNavigate: (Dest) -> Unit, userVm: UserViewModel = viewModel(), scheduleVm: ScheduleViewModel = viewModel()) {
    val nameState by userVm.userName.collectAsState(initial = null)
    val name = nameState?.trim().orEmpty()
    val greeting = if (name.isNotEmpty()) "Olá, $name!" else "Olá, estudante!"

    val courses by scheduleVm.courses.collectAsState(emptyList())

    // calcula próxima aula real
    val next by remember(courses) { mutableStateOf(findNextClass(courses)) }

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
            painter = painterResource(id = com.facool.app.R.drawable.logo_facool),
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
                    Text("${c.name} - ${hm} - ${c.location}")
                    Text(remText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                } else {
                    Text("Sem aulas cadastradas para os próximos dias.")
                }
                Divider()
                Text("Resumo de hoje: faltas 0 • 2 tarefas", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ActionCard("Pomodoro", modifier = Modifier.weight(1f)) { onNavigate(Dest.Pomodoro) }
            ActionCard("Cronograma", modifier = Modifier.weight(1f)) { onNavigate(Dest.Schedule) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ActionCard("Faltas", modifier = Modifier.weight(1f)) { onNavigate(Dest.Absences) }
            ActionCard("Perfil", modifier = Modifier.weight(1f)) { onNavigate(Dest.Profile) }
        }
    }
}

private fun findNextClass(courses: List<com.facool.app.data.Course>): Pair<com.facool.app.data.Course, LocalDateTime>? {
    if (courses.isEmpty()) return null
    val now = LocalDateTime.now()
    val candidates = courses.mapNotNull { c ->
        val dow = DayOfWeek.MONDAY.plus((c.dayOfWeek - 1).coerceIn(0,4).toLong())
        var date = now.toLocalDate()
        // advance to target DOW
        val currentDow = date.dayOfWeek
        val delta = ((dow.value - currentDow.value) + 7) % 7
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