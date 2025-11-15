package com.facool.app.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Person
import com.facool.app.data.FacoolRepository
import com.facool.app.data.FirebaseService
import com.facool.app.ui.screens.*
import kotlinx.coroutines.launch

enum class Dest(val label: String, val icon: ImageVector) {
    Home("Início", Icons.Filled.Home),
    Schedule("Cronograma", Icons.Filled.Schedule),
    Absences("Faltas", Icons.Filled.List),
    Pomodoro("Pomodoro", Icons.Filled.Timer),
    Profile("Perfil", Icons.Filled.Person),
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FacoolApp() {
    val context = LocalContext.current
    val firebaseService = remember { FirebaseService() }
    val repository = remember { FacoolRepository(context) }
    
    var isAuthenticated by remember { mutableStateOf(firebaseService.currentUser != null) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) { 
        createPomodoroChannel(context)
        // Verificar autenticação inicial
        isAuthenticated = firebaseService.currentUser != null
        if (isAuthenticated) {
            repository.syncAllFromFirebase()
        }
    }
    
    // Observar mudanças no estado de autenticação
    LaunchedEffect(firebaseService.currentUser) {
        isAuthenticated = firebaseService.currentUser != null
        if (isAuthenticated) {
            repository.syncAllFromFirebase()
        }
    }

    if (!isAuthenticated) {
        AuthScreen(
            onAuthSuccess = {
                isAuthenticated = true
                scope.launch {
                    repository.syncAllFromFirebase()
                }
            },
            firebaseService = firebaseService
        )
    } else {
        val tabs = remember { Dest.values().toList() }
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val current = pagerState.currentPage
                    tabs.forEachIndexed { index, dest ->
                        NavigationBarItem(
                            selected = current == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
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
                    Dest.Profile -> ProfileScreen(
                        onSignOut = {
                            firebaseService.signOut()
                            isAuthenticated = false
                        }
                    )
                }
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