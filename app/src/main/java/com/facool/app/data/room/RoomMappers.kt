package com.facool.app.data.room

import com.facool.app.data.Absence
import com.facool.app.data.Course
import com.facool.app.data.Task
import com.facool.app.data.Grade
import com.facool.app.data.Exam

// Funções de conversão entre Room Entities e Models

fun CourseEntity.toCourse(): Course {
    return Course(
        id = this.id,
        name = this.name,
        location = this.location,
        dayOfWeek = this.dayOfWeek,
        startTimeMinutes = this.startTimeMinutes
    )
}

fun Course.toCourseEntity(): CourseEntity {
    return CourseEntity(
        id = this.id,
        name = this.name,
        location = this.location,
        dayOfWeek = this.dayOfWeek,
        startTimeMinutes = this.startTimeMinutes
    )
}

fun TaskEntity.toTask(): Task {
    return Task(
        id = this.id,
        courseId = this.courseId,
        title = this.title,
        done = this.done,
        dueDay = this.dueDay
    )
}

fun Task.toTaskEntity(): TaskEntity {
    return TaskEntity(
        id = this.id,
        courseId = this.courseId,
        title = this.title,
        done = this.done,
        dueDay = this.dueDay
    )
}

fun AbsenceEntity.toAbsence(): Absence {
    return Absence(
        courseId = this.courseId,
        count = this.count
    )
}

fun Absence.toAbsenceEntity(): AbsenceEntity {
    return AbsenceEntity(
        courseId = this.courseId,
        count = this.count
    )
}

fun GradeEntity.toGrade(): Grade {
    return Grade(
        id = this.id,
        courseId = this.courseId,
        name = this.name,
        score = this.score,
        maxScore = this.maxScore,
        weight = this.weight,
        date = this.date
    )
}

fun Grade.toGradeEntity(): GradeEntity {
    return GradeEntity(
        id = this.id,
        courseId = this.courseId,
        name = this.name,
        score = this.score,
        maxScore = this.maxScore,
        weight = this.weight,
        date = this.date
    )
}

fun ExamEntity.toExam(): Exam {
    return Exam(
        id = this.id,
        courseId = this.courseId,
        title = this.title,
        date = this.date,
        location = this.location,
        notes = this.notes,
        reminderEnabled = this.reminderEnabled
    )
}

fun Exam.toExamEntity(): ExamEntity {
    return ExamEntity(
        id = this.id,
        courseId = this.courseId,
        title = this.title,
        date = this.date,
        location = this.location,
        notes = this.notes,
        reminderEnabled = this.reminderEnabled
    )
}

