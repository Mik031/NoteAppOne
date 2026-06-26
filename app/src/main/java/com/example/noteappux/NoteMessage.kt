package com.example.noteappux

data class NoteMessage(
    val id: Int = 0,
    val noteId: Int,
    val messageType: String,
    val textContent: String = "",
    val imageUri: String = "",
    val fileName: String = "",
    val fileUri: String = "",
    val linkUrl: String = "",
    val createdAt: Long = 0L,
    val messageOrder: Int = 0
)