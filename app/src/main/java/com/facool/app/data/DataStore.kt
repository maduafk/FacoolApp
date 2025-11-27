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
import com.facool.app.data.room.FacoolDatabase
import com.facool.app.data.room.toCourse
import com.facool.app.data.room.toCourseEntity
import com.facool.app.data.room.toTask
import com.facool.app.data.room.toTaskEntity
import com.facool.app.data.room.toAbsence
import com.facool.app.data.room.toAbsenceEntity
import com.facool.app.data.room.toGrade
import com.facool.app.data.room.toGradeEntity
import com.facool.app.data.room.toExam
import com.facool.app.data.room.toExamEntity

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
    
    // Room Database
    private val database = FacoolDatabase.getDatabase(context)
    private val courseDao = database.courseDao()
    private val taskDao = database.taskDao()
    private val absenceDao = database.absenceDao()
    private val gradeDao = database.gradeDao()
    private val examDao = database.examDao()
    
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
        courseDao.getAllCourses().map { entities -> entities.map { it.toCourse() } }
    ) { firebaseCourses, roomCourses ->
        if (isAuthenticated) {
            firebaseCourses.ifEmpty { roomCourses }
        } else {
            roomCourses
        }
    }
    
    suspend fun loadCoursesFromFirebase() {
        if (isAuthenticated) {
            _coursesState.value = firebaseService.getCourses()
        }
    }

    suspend fun saveCourses(list: List<Course>) {
        // Salvar no Room (banco local)
        courseDao.insertAllCourses(list.map { it.toCourseEntity() })
        
        // Se autenticado, sincronizar com Firebase
        if (isAuthenticated) {
            firebaseService.saveCourses(list)
            _coursesState.value = list
        }
    }
    
    suspend fun addCourse(course: Course) {
        courseDao.insertCourse(course.toCourseEntity())
        if (isAuthenticated) {
            val currentCourses = courseDao.getAllCourses().first().map { it.toCourse() }
            firebaseService.saveCourses(currentCourses + course)
        }
    }
    
    suspend fun updateCourse(course: Course) {
        courseDao.updateCourse(course.toCourseEntity())
        if (isAuthenticated) {
            val currentCourses = courseDao.getAllCourses().first().map { it.toCourse() }
            val updated = currentCourses.map { if (it.id == course.id) course else it }
            firebaseService.saveCourses(updated)
        }
    }
    
    suspend fun deleteCourse(courseId: String) {
        courseDao.deleteCourseById(courseId)
        if (isAuthenticated) {
            val currentCourses = courseDao.getAllCourses().first().map { it.toCourse() }
            val filtered = currentCourses.filter { it.id != courseId }
            firebaseService.saveCourses(filtered)
        }
    }

    val absences: Flow<List<Absence>> = combine(
        _absencesState.asStateFlow(),
        absenceDao.getAllAbsences().map { entities -> entities.map { it.toAbsence() } }
    ) { firebaseAbsences, roomAbsences ->
        if (isAuthenticated) {
            firebaseAbsences.ifEmpty { roomAbsences }
        } else {
            roomAbsences
        }
    }
    
    suspend fun loadAbsencesFromFirebase() {
        if (isAuthenticated) {
            _absencesState.value = firebaseService.getAbsences()
        }
    }

    suspend fun saveAbsences(list: List<Absence>) {
        // Salvar no Room (banco local)
        absenceDao.insertAllAbsences(list.map { it.toAbsenceEntity() })
        
        // Se autenticado, sincronizar com Firebase
        if (isAuthenticated) {
            firebaseService.saveAbsences(list)
            _absencesState.value = list
        }
    }
    
    suspend fun addAbsence(absence: Absence) {
        absenceDao.insertAbsence(absence.toAbsenceEntity())
        if (isAuthenticated) {
            val currentAbsences = absenceDao.getAllAbsences().first().map { it.toAbsence() }
            firebaseService.saveAbsences(currentAbsences + absence)
        }
    }
    
    suspend fun updateAbsence(absence: Absence) {
        absenceDao.updateAbsence(absence.toAbsenceEntity())
        if (isAuthenticated) {
            val currentAbsences = absenceDao.getAllAbsences().first().map { it.toAbsence() }
            val updated = currentAbsences.map { if (it.courseId == absence.courseId) absence else it }
            firebaseService.saveAbsences(updated)
        }
    }
    
    suspend fun deleteAbsence(courseId: String) {
        absenceDao.deleteAbsenceByCourseId(courseId)
        if (isAuthenticated) {
            val currentAbsences = absenceDao.getAllAbsences().first().map { it.toAbsence() }
            val filtered = currentAbsences.filter { it.courseId != courseId }
            firebaseService.saveAbsences(filtered)
        }
    }

    val tasks: Flow<List<Task>> = combine(
        _tasksState.asStateFlow(),
        taskDao.getAllTasks().map { entities -> entities.map { it.toTask() } }
    ) { firebaseTasks, roomTasks ->
        if (isAuthenticated) {
            firebaseTasks.ifEmpty { roomTasks }
        } else {
            roomTasks
        }
    }
    
    suspend fun loadTasksFromFirebase() {
        if (isAuthenticated) {
            _tasksState.value = firebaseService.getTasks()
        }
    }

    suspend fun saveTasks(list: List<Task>) {
        // Salvar no Room (banco local)
        taskDao.insertAllTasks(list.map { it.toTaskEntity() })
        
        // Se autenticado, sincronizar com Firebase
        if (isAuthenticated) {
            firebaseService.saveTasks(list)
            _tasksState.value = list
        }
    }
    
    suspend fun addTask(task: Task) {
        taskDao.insertTask(task.toTaskEntity())
        if (isAuthenticated) {
            val currentTasks = taskDao.getAllTasks().first().map { it.toTask() }
            firebaseService.saveTasks(currentTasks + task)
        }
    }
    
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toTaskEntity())
        if (isAuthenticated) {
            val currentTasks = taskDao.getAllTasks().first().map { it.toTask() }
            val updated = currentTasks.map { if (it.id == task.id) task else it }
            firebaseService.saveTasks(updated)
        }
    }
    
    suspend fun deleteTask(taskId: String) {
        taskDao.deleteTaskById(taskId)
        if (isAuthenticated) {
            val currentTasks = taskDao.getAllTasks().first().map { it.toTask() }
            val filtered = currentTasks.filter { it.id != taskId }
            firebaseService.saveTasks(filtered)
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
            // Carregar dados do Firebase
            loadUserNameFromFirebase()
            val firebaseCourses = firebaseService.getCourses()
            val firebaseAbsences = firebaseService.getAbsences()
            val firebaseTasks = firebaseService.getTasks()
            
            // Carregar dados locais do Room
            val localCourses = courseDao.getAllCourses().first().map { it.toCourse() }
            val localAbsences = absenceDao.getAllAbsences().first().map { it.toAbsence() }
            val localTasks = taskDao.getAllTasks().first().map { it.toTask() }
            
            // Se Firebase tem dados, usar Firebase (prioridade)
            // Se Firebase está vazio mas Room tem dados, manter Room e sincronizar com Firebase
            if (firebaseCourses.isNotEmpty()) {
                // Firebase tem dados - substituir local
                courseDao.deleteAllCourses()
                courseDao.insertAllCourses(firebaseCourses.map { it.toCourseEntity() })
                _coursesState.value = firebaseCourses
            } else if (localCourses.isNotEmpty()) {
                // Firebase vazio mas Room tem dados - sincronizar Room -> Firebase
                firebaseService.saveCourses(localCourses)
                _coursesState.value = localCourses
            }
            
            if (firebaseAbsences.isNotEmpty()) {
                absenceDao.deleteAllAbsences()
                absenceDao.insertAllAbsences(firebaseAbsences.map { it.toAbsenceEntity() })
                _absencesState.value = firebaseAbsences
            } else if (localAbsences.isNotEmpty()) {
                firebaseService.saveAbsences(localAbsences)
                _absencesState.value = localAbsences
            }
            
            if (firebaseTasks.isNotEmpty()) {
                taskDao.deleteAllTasks()
                taskDao.insertAllTasks(firebaseTasks.map { it.toTaskEntity() })
                _tasksState.value = firebaseTasks
            } else if (localTasks.isNotEmpty()) {
                firebaseService.saveTasks(localTasks)
                _tasksState.value = localTasks
            }
        }
    }
    
    // Grades (Notas)
    val grades: Flow<List<Grade>> = gradeDao.getAllGrades().map { entities -> entities.map { it.toGrade() } }
    
    suspend fun addGrade(grade: Grade) {
        gradeDao.insertGrade(grade.toGradeEntity())
    }
    
    suspend fun updateGrade(grade: Grade) {
        gradeDao.updateGrade(grade.toGradeEntity())
    }
    
    suspend fun deleteGrade(gradeId: String) {
        gradeDao.deleteGradeById(gradeId)
    }
    
    // Exams (Provas)
    val exams: Flow<List<Exam>> = examDao.getAllExams().map { entities -> entities.map { it.toExam() } }
    
    suspend fun addExam(exam: Exam) {
        examDao.insertExam(exam.toExamEntity())
    }
    
    suspend fun updateExam(exam: Exam) {
        examDao.updateExam(exam.toExamEntity())
    }
    
    suspend fun deleteExam(examId: String) {
        examDao.deleteExamById(examId)
    }
    
    // Limpar todos os dados locais (usado no logout)
    suspend fun clearAllLocalData() {
        courseDao.deleteAllCourses()
        taskDao.deleteAllTasks()
        absenceDao.deleteAllAbsences()
        gradeDao.deleteAllGrades()
        examDao.deleteAllExams()
        _coursesState.value = emptyList()
        _tasksState.value = emptyList()
        _absencesState.value = emptyList()
        _userNameState.value = null
    }
}