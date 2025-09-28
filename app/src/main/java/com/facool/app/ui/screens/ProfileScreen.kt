package com.facool.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facool.app.viewmodels.UserViewModel

@Composable
fun ProfileScreen(vm: UserViewModel = viewModel()) {
    val name by vm.userName.collectAsState(initial = "")
    var newName by remember(name) { mutableStateOf(name ?: "") }
    val focus by vm.focus.collectAsState(25)
    val brk by vm.brk.collectAsState(5)

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Perfil & Configurações", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Seu nome") })
        Button(onClick = { vm.setUserName(newName) }) { Text("Salvar nome") }

        Divider()
        Text("Pomodoro")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(focus.toString(), { vm.setPomodoro((it.toIntOrNull() ?: 25), brk) }, label = { Text("Foco (min)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(brk.toString(), { vm.setPomodoro(focus, (it.toIntOrNull() ?: 5)) }, label = { Text("Pausa (min)") }, modifier = Modifier.weight(1f))
        }
    }
}
