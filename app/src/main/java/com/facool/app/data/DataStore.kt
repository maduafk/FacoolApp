package com.facool.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore("facool_prefs")

object Keys {
    val USER_NAME = stringPreferencesKey("user_name")
    val COURSES_JSON = stringPreferencesKey("courses_json")
    val ABSENCES_JSON = stringPreferencesKey("absences_json")
    val POMO_FOCUS_MIN = intPreferencesKey("pomo_focus_min")
    val POMO_BREAK_MIN = intPreferencesKey("pomo_break_min")
}

class FacoolRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val userName: Flow<String?> = context.dataStore.data.map { it[Keys.USER_NAME] }
    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[Keys.USER_NAME] = name }
    }

    val courses: Flow<List<Course>> = context.dataStore.data.map {
        it[Keys.COURSES_JSON]?.let { js -> json.decodeFromString(js) } ?: emptyList()
    }

    suspend fun saveCourses(list: List<Course>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.COURSES_JSON] = json.encodeToString(list)
        }
    }

    val absences: Flow<List<Absence>> = context.dataStore.data.map {
        it[Keys.ABSENCES_JSON]?.let { js -> json.decodeFromString(js) } ?: emptyList()
    }

    suspend fun saveAbsences(list: List<Absence>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ABSENCES_JSON] = json.encodeToString(list)
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
}
