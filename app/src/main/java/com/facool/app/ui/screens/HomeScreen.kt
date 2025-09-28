package com.facool.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facool.app.ui.Dest
import com.facool.app.viewmodels.UserViewModel

@Composable
fun HomeScreen(onNavigate: (Dest) -> Unit, vm: UserViewModel = viewModel()) {
    val nameState by vm.userName.collectAsState(initial = null)
    val name = nameState?.trim().orEmpty()
    val greeting = if (name.isNotEmpty()) "Olá, $name!" else "Olá, estudante!"

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
                Button(enabled = tempName.isNotBlank(), onClick = { vm.setUserName(tempName.trim()) }) { Text("Começar") }
            },
            dismissButton = { TextButton(onClick = { vm.setUserName("") }) { Text("Pular") } }
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Facool", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(greeting, style = MaterialTheme.typography.titleMedium)
        Text("Seu estudo, seu ritmo.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Próxima aula", style = MaterialTheme.typography.titleMedium)
                Text("Algoritmos - 10:00 - Bloco A, Sala 12")
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

@Composable
private fun ActionCard(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier) {
        Box(Modifier.height(90.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
