package com.facool.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore("facool_prefs")

object Keys {
    val USER_NAME = stringPreferencesKey("user_name")
    val COURSES_JSON = stringPreferencesKey("courses_json")
    val ABSENCES_JSON = stringPreferencesKey("absences_json")
    val TASKS_JSON = stringPreferencesKey("tasks_json")
    val POMO_FOCUS_MIN = intPreferencesKey("pomo_focus_min")
    val POMO_BREAK_MIN = intPreferencesKey("pomo_break_min")
}

class FacoolRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val firebaseService = FirebaseService()
    
    // Estado para sincronizar dados do Firebase
    private val _coursesState = MutableStateFlow<List<Course>>(emptyList())
    private val _absencesState = MutableStateFlow<List<Absence>>(emptyList())
    private val _tasksState = MutableStateFlow<List<Task>>(emptyList())
    private val _userNameState = MutableStateFlow<String?>(null)
    
    private val isAuthenticated: Boolean
        get() = firebaseService.currentUser != null

    val userName: Flow<String?> = combine(
        _userNameState.asStateFlow(),
        context.dataStore.data.map { it[Keys.USER_NAME] }
    ) { firebaseName, localName ->
        if (isAuthenticated) {
            firebaseName ?: localName
        } else {
            localName
        }
    }
    
    suspend fun setUserName(name: String) {
        if (isAuthenticated) {
            firebaseService.setUserName(name)
            _userNameState.value = name
        } else {
            context.dataStore.edit { it[Keys.USER_NAME] = name }
        }
    }
    
    suspend fun loadUserNameFromFirebase() {
        if (isAuthenticated) {
            _userNameState.value = firebaseService.getUserName()
        }
    }

    val courses: Flow<List<Course>> = combine(
        _coursesState.asStateFlow(),
        context.dataStore.data.map {
            it[Keys.COURSES_JSON]?.let { js -> json.decodeFromString<List<Course>>(js) } ?: emptyList()
        }
    ) { firebaseCourses, localCourses ->
        if (isAuthenticated) {
            firebaseCourses.ifEmpty { localCourses }
        } else {
            localCourses
        }
    }
    
    suspend fun loadCoursesFromFirebase() {
        if (isAuthenticated) {
            _coursesState.value = firebaseService.getCourses()
        }
    }

    suspend fun saveCourses(list: List<Course>) {
        if (isAuthenticated) {
            firebaseService.saveCourses(list)
            _coursesState.value = list
        } else {
            context.dataStore.edit { prefs -> 
                prefs[Keys.COURSES_JSON] = json.encodeToString(list) 
            }
        }
    }

    val absences: Flow<List<Absence>> = combine(
        _absencesState.asStateFlow(),
        context.dataStore.data.map {
            it[Keys.ABSENCES_JSON]?.let { js -> json.decodeFromString<List<Absence>>(js) } ?: emptyList()
        }
    ) { firebaseAbsences, localAbsences ->
        if (isAuthenticated) {
            firebaseAbsences.ifEmpty { localAbsences }
        } else {
            localAbsences
        }
    }
    
    suspend fun loadAbsencesFromFirebase() {
        if (isAuthenticated) {
            _absencesState.value = firebaseService.getAbsences()
        }
    }

    suspend fun saveAbsences(list: List<Absence>) {
        if (isAuthenticated) {
            firebaseService.saveAbsences(list)
            _absencesState.value = list
        } else {
            context.dataStore.edit { prefs -> 
                prefs[Keys.ABSENCES_JSON] = json.encodeToString(list) 
            }
        }
    }

    val tasks: Flow<List<Task>> = combine(
        _tasksState.asStateFlow(),
        context.dataStore.data.map {
            it[Keys.TASKS_JSON]?.let { js -> json.decodeFromString<List<Task>>(js) } ?: emptyList()
        }
    ) { firebaseTasks, localTasks ->
        if (isAuthenticated) {
            firebaseTasks.ifEmpty { localTasks }
        } else {
            localTasks
        }
    }
    
    suspend fun loadTasksFromFirebase() {
        if (isAuthenticated) {
            _tasksState.value = firebaseService.getTasks()
        }
    }

    suspend fun saveTasks(list: List<Task>) {
        if (isAuthenticated) {
            firebaseService.saveTasks(list)
            _tasksState.value = list
        } else {
            context.dataStore.edit { prefs -> 
                prefs[Keys.TASKS_JSON] = json.encodeToString(list) 
            }
        }
    }

    val pomodoroFocusMin: Flow<Int> = context.dataStore.data.map { it[Keys.POMO_FOCUS_MIN] ?: 25 }
    val pomodoroBreakMin: Flow<Int> = context.dataStore.data.map { it[Keys.POMO_BREAK_MIN] ?: 5 }
    suspend fun setPomodoro(focus: Int, brk: Int) {
        context.dataStore.edit {
            it[Keys.POMO_FOCUS_MIN] = focus
            it[Keys.POMO_BREAK_MIN] = brk
        }
    }
    
    suspend fun syncAllFromFirebase() {
        if (isAuthenticated) {
            loadUserNameFromFirebase()
            loadCoursesFromFirebase()
            loadAbsencesFromFirebase()
            loadTasksFromFirebase()
            
            // Migrar dados locais para Firebase se houver
            val localCourses = context.dataStore.data.first()[Keys.COURSES_JSON]?.let { 
                json.decodeFromString<List<Course>>(it) 
            } ?: emptyList()
            if (localCourses.isNotEmpty()) {
                val firebaseCourses = firebaseService.getCourses()
                if (firebaseCourses.isEmpty()) {
                    // Se não há dados no Firebase mas há locais, migrar
                    firebaseService.saveCourses(localCourses)
                    _coursesState.value = localCourses
                }
            }
            
            val localAbsences = context.dataStore.data.first()[Keys.ABSENCES_JSON]?.let { 
                json.decodeFromString<List<Absence>>(it) 
            } ?: emptyList()
            if (localAbsences.isNotEmpty()) {
                val firebaseAbsences = firebaseService.getAbsences()
                if (firebaseAbsences.isEmpty()) {
                    firebaseService.saveAbsences(localAbsences)
                    _absencesState.value = localAbsences
                }
            }
            
            val localTasks = context.dataStore.data.first()[Keys.TASKS_JSON]?.let { 
                json.decodeFromString<List<Task>>(it) 
            } ?: emptyList()
            if (localTasks.isNotEmpty()) {
                val firebaseTasks = firebaseService.getTasks()
                if (firebaseTasks.isEmpty()) {
                    firebaseService.saveTasks(localTasks)
                    _tasksState.value = localTasks
                }
            }
        }
    }
}