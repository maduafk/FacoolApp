package com.facool.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class FirebaseService {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val json = Json { ignoreUnknownKeys = true }

    val currentUser: FirebaseUser? get() = auth.currentUser
    val userId: String? get() = currentUser?.uid

    suspend fun signUp(email: String, password: String, name: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Salvar nome do usuário no Firestore
                db.collection("users").document(user.uid).set(
                    mapOf("name" to name, "email" to email)
                ).await()
                Result.success(user)
            } ?: Result.failure(Exception("Falha ao criar usuário"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { Result.success(it) }
                ?: Result.failure(Exception("Falha ao fazer login"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getUserName(): String? {
        val uid = userId ?: return null
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.getString("name")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun setUserName(name: String) {
        val uid = userId ?: return
        try {
            db.collection("users").document(uid).update("name", name).await()
        } catch (e: Exception) {
            // Se o documento não existe, criar
            db.collection("users").document(uid).set(mapOf("name" to name)).await()
        }
    }

    suspend fun getCourses(): List<Course> {
        val uid = userId ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("courses").get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    Course(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        location = doc.getString("location") ?: "",
                        dayOfWeek = doc.getLong("dayOfWeek")?.toInt() ?: 1,
                        startTimeMinutes = doc.getLong("startTimeMinutes")?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveCourses(courses: List<Course>) {
        val uid = userId ?: return
        try {
            val batch = db.batch()
            // Limpar cursos existentes
            val existing = db.collection("users").document(uid)
                .collection("courses").get().await()
            existing.documents.forEach { batch.delete(it.reference) }
            
            // Adicionar novos cursos
            courses.forEach { course ->
                val docRef = db.collection("users").document(uid)
                    .collection("courses").document(course.id)
                batch.set(docRef, mapOf(
                    "name" to course.name,
                    "location" to course.location,
                    "dayOfWeek" to course.dayOfWeek,
                    "startTimeMinutes" to course.startTimeMinutes
                ))
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getAbsences(): List<Absence> {
        val uid = userId ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("absences").get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    Absence(
                        courseId = doc.getString("courseId") ?: "",
                        count = doc.getLong("count")?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveAbsences(absences: List<Absence>) {
        val uid = userId ?: return
        try {
            val batch = db.batch()
            val existing = db.collection("users").document(uid)
                .collection("absences").get().await()
            existing.documents.forEach { batch.delete(it.reference) }
            
            absences.forEach { absence ->
                val docRef = db.collection("users").document(uid)
                    .collection("absences").document(absence.courseId)
                batch.set(docRef, mapOf(
                    "courseId" to absence.courseId,
                    "count" to absence.count
                ))
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getTasks(): List<Task> {
        val uid = userId ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("tasks").get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    Task(
                        id = doc.id,
                        courseId = doc.getString("courseId") ?: "",
                        title = doc.getString("title") ?: "",
                        done = doc.getBoolean("done") ?: false,
                        dueDay = doc.getLong("dueDay")?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveTasks(tasks: List<Task>) {
        val uid = userId ?: return
        try {
            val batch = db.batch()
            val existing = db.collection("users").document(uid)
                .collection("tasks").get().await()
            existing.documents.forEach { batch.delete(it.reference) }
            
            tasks.forEach { task ->
                val docRef = db.collection("users").document(uid)
                    .collection("tasks").document(task.id)
                batch.set(docRef, mapOf(
                    "courseId" to task.courseId,
                    "title" to task.title,
                    "done" to task.done,
                    "dueDay" to task.dueDay
                ))
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}





