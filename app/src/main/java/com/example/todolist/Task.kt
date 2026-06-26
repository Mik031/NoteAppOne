package com.example.todolist

data class Task(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val date: String? = null,
    val time: String? = null,
    val priority: String? = null,
    val reminderTime: Long? = null,
    var isDone: Boolean = false
)