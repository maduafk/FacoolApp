package com.facool.app.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import com.facool.app.ui.screens.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import kotlinx.coroutines.launch

enum class Dest(val label: String, val icon: ImageVector) {
    Home("Início", androidx.compose.material.icons.Icons.Filled.Home),
    Schedule("Cronograma", androidx.compose.material.icons.Icons.Filled.Schedule),
    Absences("Faltas", androidx.compose.material.icons.Icons.Filled.List),
    Pomodoro("Pomodoro", androidx.compose.material.icons.Icons.Filled.Timer),
    Profile("Perfil", androidx.compose.material.icons.Icons.Filled.Person),
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun FacoolApp() {
    val context = LocalContext.current
    LaunchedEffect(Unit) { createPomodoroChannel(context) }

    val tabs = remember { Dest.values().toList() }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val current = pagerState.currentPage
                tabs.forEachIndexed { index, dest ->
                    NavigationBarItem(
                        selected = current == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { padding ->
        HorizontalPager(state = pagerState, modifier = Modifier.padding(padding)) { page ->
            when (tabs[page]) {
                Dest.Home -> HomeScreen(onNavigate = { dest ->
                    val target = tabs.indexOf(dest)
                    if (target >= 0) scope.launch { pagerState.animateScrollToPage(target) }
                })
                Dest.Schedule -> ScheduleScreen()
                Dest.Absences -> AbsencesScreen()
                Dest.Pomodoro -> PomodoroScreen()
                Dest.Profile -> ProfileScreen()
            }
        }
    }
}

private fun createPomodoroChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "pomodoro_channel", "Pomodoro", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Alertas de ciclos Pomodoro" }
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.createNotificationChannel(channel)
    }
}