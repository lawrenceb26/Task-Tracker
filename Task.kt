package com.example.task_tracker

data class Task(
    val id: Int,
    val title: String,
    val dueDate: Long,
    var isCompleted: Boolean,
    val alarmType: AlarmType,
    var description: String? = null
)