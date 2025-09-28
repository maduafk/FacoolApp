package com.facool.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

enum class Dest(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Início", Icons.Filled.Home),
    Schedule("schedule", "Cronograma", Icons.Filled.Schedule),
    Absences("absences", "Faltas", Icons.Filled.List),
    Pomodoro("pomodoro", "Pomodoro", Icons.Filled.Timer),
    Profile("profile", "Perfil", Icons.Filled.Person),
}
