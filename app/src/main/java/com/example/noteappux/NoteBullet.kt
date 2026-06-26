package com.example.noteappux

data class NoteBullet(
    val id: Int = 0,
    val noteId: Int,
    val bulletText: String,
    val bulletType: String,
    val bulletOrder: Int = 0,
    val isTask: Int = 1,
    val isCompleted: Int = 0,
    val createdAt: Long = 0L
)